import { fetch } from "@tauri-apps/plugin-http";
import { API_CONTEXT_PATH } from "./apiPath";

type ErrorInfo = {
	message: string;
	endpoint: string;
	method: string;
	requestBody: string;
};

export type FetchParams = {
	endpoint: string;
	options: RequestInit;
	signal?: AbortSignal;
	timeoutMs?: number;
	// 実行中のログを受け取る（sidecarの @Interruptible エンドポイントのみログが流れる）
	onLog?: (line: string) => void;
};

const createErrorInfo = (
	message: string,
	endpoint: string,
	options: RequestInit,
): ErrorInfo => ({
	message: message || "An unknown error occurred",
	endpoint: endpoint || "N/A",
	method: options.method || "GET",
	requestBody: options.body ? JSON.stringify(options.body, null, 2) : "",
});
const createErrorMessage = (errorInfo: ErrorInfo): string => {
	return `
An error occurred
Message: ${errorInfo.message}
Endpoint: ${errorInfo.endpoint}
Method: ${errorInfo.method}
Request Body: ${errorInfo.requestBody}
    `.trim();
};

export const handleFetchError = (
	message: string,
	{ endpoint, options }: FetchParams,
) => {
	const errorInfo = createErrorInfo(message, endpoint, options);
	const errorMessage = createErrorMessage(errorInfo);

	alert(errorMessage);
	console.error("Fetch Error:", errorInfo);
};

export const getErrorMessage = (error: unknown): string =>
	error instanceof Error ? error.message : String(error);

export type OperationResult = "success" | "failed";

export async function saveOnSuccess(
	saveFn: () => Promise<OperationResult>,
	onSuccess: () => void,
): Promise<void> {
	const result = await saveFn();
	if (result === "success") {
		onSuccess();
	}
}

export const isAbortError = (error: unknown): boolean =>
	error instanceof DOMException && error.name === "AbortError";

export async function fetchAndUpdate<T>(
	fetchParams: FetchParams,
	onSuccess: (data: T) => void,
): Promise<OperationResult> {
	return fetchData(fetchParams)
		.then((r) => r.json())
		.then((data: T) => {
			onSuccess(data);
			return "success" as OperationResult;
		})
		.catch((ex) => {
			handleFetchError(getErrorMessage(ex), fetchParams);
			return "failed" as OperationResult;
		});
}

const fetchWithRetry = async (endpoint: string, options: RequestInit) => {
	let response: Response;
	try {
		response = await fetch(endpoint, options);
	} catch (ex) {
		if (isAbortError(ex)) {
			throw ex;
		}
		// reqwest のコネクションプールに残った stale keep-alive 接続が原因で
		// 接続エラーになる場合があるため、1 回だけリトライする
		response = await fetch(endpoint, options);
	}
	if (!response.ok) {
		throw new Error(
			`Status: ${response.status}\nDetails: ${response.statusText || "Fetch request failed"}`,
		);
	}
	return response;
};

const JOB_HEADER = "X-Job-Id";
const JOB_LOG_HEADER = "X-Job-Log";

// 処理完了後、末尾のログが届くのを待ってからログの購読を打ち切るまでの猶予
const LOG_DRAIN_MS = 1000;

// SSEの1イベント分（空行区切りのブロック）からdata行を取り出す
const parseSseData = (block: string): string | null => {
	const data = block
		.split("\n")
		.filter((line) => line.startsWith("data:"))
		.map((line) => line.slice("data:".length).trimStart());
	return data.length > 0 ? data.join("\n") : null;
};

// ログは補助情報のため、購読の失敗は処理結果に影響させない
const streamJobLogs = async (
	url: string,
	signal: AbortSignal,
	onLog: (line: string) => void,
) => {
	try {
		const response = await fetch(url, {
			method: "GET",
			headers: { Accept: "text/event-stream" },
			signal,
		});
		if (!response.ok || !response.body) {
			return;
		}
		const reader = response.body.getReader();
		signal.addEventListener("abort", () => {
			reader.cancel().catch(() => undefined);
		});
		const decoder = new TextDecoder();
		let buffer = "";
		for (;;) {
			const { done, value } = await reader.read();
			if (done || signal.aborted) {
				return;
			}
			buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, "\n");
			let separator = buffer.indexOf("\n\n");
			while (separator >= 0) {
				const line = parseSseData(buffer.slice(0, separator));
				buffer = buffer.slice(separator + 2);
				// 中断後に届いた行は渡さない
				if (line !== null && !signal.aborted) {
					onLog(line);
				}
				separator = buffer.indexOf("\n\n");
			}
		}
	} catch {
		// 何もしない
	}
};

// jobIdを発行して X-Job-Id 付きで呼ぶ。onLogがあればログを購読し、abort時はsidecarへcancelを送る。
// 中断・ログの対象にするかはsidecarが @Interruptible で決め、対象外のエンドポイントはヘッダを無視する
const fetchWithJob = async (
	endpoint: string,
	options: RequestInit,
	signal: AbortSignal | undefined,
	onLog: ((line: string) => void) | undefined,
): Promise<Response> => {
	const jobId = crypto.randomUUID();
	const jobUrl = new URL(`${API_CONTEXT_PATH}job/${jobId}/`, endpoint);
	const headers = new Headers(options.headers);
	headers.set(JOB_HEADER, jobId);
	let logs: AbortController | null = null;
	if (onLog) {
		logs = new AbortController();
		headers.set(JOB_LOG_HEADER, "true");
		streamJobLogs(new URL("events", jobUrl).href, logs.signal, onLog);
	}
	const handleAbort = () => {
		logs?.abort();
		fetchWithRetry(new URL("cancel", jobUrl).href, { method: "POST" }).catch(
			() => undefined,
		);
	};
	signal?.addEventListener("abort", handleAbort, { once: true });
	try {
		return await fetchWithRetry(endpoint, { ...options, headers, signal });
	} finally {
		signal?.removeEventListener("abort", handleAbort);
		if (logs) {
			// 正常時はsidecarが購読を閉じる。処理まで届かなかった場合に備えて購読を残さない
			setTimeout(() => logs.abort(), LOG_DRAIN_MS);
		}
	}
};

export const fetchData = async ({
	endpoint,
	options,
	signal,
	timeoutMs,
	onLog,
}: FetchParams) => {
	let controller: AbortController | null = null;
	let timeoutId: ReturnType<typeof setTimeout> | null = null;
	if (!signal && timeoutMs !== undefined) {
		controller = new AbortController();
		timeoutId = setTimeout(() => controller!.abort(), timeoutMs);
	}
	const mergedSignal =
		signal ?? controller?.signal ?? options.signal ?? undefined;
	try {
		// 中断できる（signalがある）か、ログが欲しい呼び出しだけジョブとして扱う
		if (mergedSignal || onLog) {
			return await fetchWithJob(endpoint, options, mergedSignal, onLog);
		}
		return await fetchWithRetry(endpoint, { ...options, signal: mergedSignal });
	} finally {
		if (timeoutId !== null) {
			clearTimeout(timeoutId);
		}
	}
};
