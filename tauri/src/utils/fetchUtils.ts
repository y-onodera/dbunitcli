import { fetch } from "@tauri-apps/plugin-http";

type ErrorInfo = {
	message: string;
	endpoint: string;
	method: string;
	requestBody: string;
};

// sidecarの「<endpoint>/job」で処理を仮想スレッドのジョブとして開始し、SSEでログと結果を受け取る
export type JobOptions = {
	apiUrl: string;
	onLog?: (line: string) => void;
};

export type FetchParams = {
	endpoint: string;
	options: RequestInit;
	signal?: AbortSignal;
	timeoutMs?: number;
	job?: JobOptions;
};

type JobEvent = {
	type: "log" | "done";
	message?: string;
	status?: "SUCCEEDED" | "FAILED" | "CANCELLED";
	httpStatus?: number;
	reason?: string;
	result?: string;
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

const toStatusError = (status: number, reason: string | undefined) =>
	new Error(`Status: ${status}\nDetails: ${reason || "Fetch request failed"}`);

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
		throw toStatusError(response.status, response.statusText);
	}
	return response;
};

// SSEの1イベント分（空行区切りのブロック）からdata行を取り出してJSONとして解釈する
const parseSseBlock = (block: string): JobEvent | null => {
	const data = block
		.split("\n")
		.filter((line) => line.startsWith("data:"))
		.map((line) => line.slice("data:".length).trimStart())
		.join("\n");
	return data ? (JSON.parse(data) as JobEvent) : null;
};

const readDoneEvent = async (
	reader: ReadableStreamDefaultReader<Uint8Array>,
	onLog: ((line: string) => void) | undefined,
): Promise<JobEvent | null> => {
	const decoder = new TextDecoder();
	let buffer = "";
	for (;;) {
		const { done, value } = await reader.read();
		if (done) {
			return null;
		}
		buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, "\n");
		let separator = buffer.indexOf("\n\n");
		while (separator >= 0) {
			const event = parseSseBlock(buffer.slice(0, separator));
			buffer = buffer.slice(separator + 2);
			if (event?.type === "done") {
				return event;
			}
			if (event?.type === "log") {
				onLog?.(event.message ?? "");
			}
			separator = buffer.indexOf("\n\n");
		}
	}
};

const abortError = () => new DOMException("Aborted", "AbortError");

// ジョブを開始してSSEで完了まで待ち、結果を通常のレスポンスと同じ形で返す。
// 中断（signalのabort）時はsidecarへcancelを送り、AbortErrorを投げる
const fetchJob = async (
	endpoint: string,
	options: RequestInit,
	signal: AbortSignal | undefined,
	{ apiUrl, onLog }: JobOptions,
): Promise<Response> => {
	const started = await fetchWithRetry(`${endpoint}/job`, {
		...options,
		signal,
	});
	const { jobId } = (await started.json()) as { jobId: string };
	const events = await fetchWithRetry(`${apiUrl}job/${jobId}/events`, {
		method: "GET",
		headers: { Accept: "text/event-stream" },
		signal,
	});
	if (!events.body) {
		throw new Error("Job event stream is not available");
	}
	const reader = events.body.getReader();
	const handleAbort = () => {
		fetch(`${apiUrl}job/${jobId}/cancel`, { method: "POST" }).catch(
			() => undefined,
		);
		reader.cancel().catch(() => undefined);
	};
	if (signal?.aborted) {
		handleAbort();
		throw abortError();
	}
	signal?.addEventListener("abort", handleAbort, { once: true });
	try {
		const done = await readDoneEvent(reader, onLog);
		if (signal?.aborted) {
			throw abortError();
		}
		if (!done) {
			throw new Error("Job event stream closed before completion");
		}
		if (done.status !== "SUCCEEDED") {
			throw toStatusError(done.httpStatus ?? 500, done.reason);
		}
		return new Response(done.result ?? "", { status: 200 });
	} catch (ex) {
		if (signal?.aborted) {
			throw abortError();
		}
		throw ex;
	} finally {
		signal?.removeEventListener("abort", handleAbort);
	}
};

export const fetchData = async ({
	endpoint,
	options,
	signal,
	timeoutMs,
	job,
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
		if (job) {
			return await fetchJob(endpoint, options, mergedSignal, job);
		}
		return await fetchWithRetry(endpoint, { ...options, signal: mergedSignal });
	} finally {
		if (timeoutId !== null) {
			clearTimeout(timeoutId);
		}
	}
};
