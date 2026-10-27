import { fetch } from "@tauri-apps/plugin-http";

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

// 名前で判定する（signal.reasonのDOMExceptionは実行環境によって別realmのクラスになり、instanceofで判定できない）
export const isAbortError = (error: unknown): boolean =>
	typeof error === "object" &&
	error !== null &&
	(error as { name?: unknown }).name === "AbortError";

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

// sidecarのオプション検証エラー。optionsは違反に関わる入力欄のname（-src.src等）
export type OptionViolation = { options: string[]; message: string };

export class OptionValidationError extends Error {
	readonly violations: OptionViolation[];

	constructor(message: string, violations: OptionViolation[]) {
		super(message);
		this.name = "OptionValidationError";
		this.violations = violations;
	}
}

// sidecarがオプション検証エラーの応答に付ける理由句
const OPTION_VALIDATION_ERROR_REASON = "Invalid Option";

// 検証エラーの応答（{message, violations}）なら取り出す。それ以外の本文ならnull
const parseOptionValidationError = (
	body: string,
): OptionValidationError | null => {
	try {
		const json = JSON.parse(body) as {
			message?: string;
			violations?: OptionViolation[];
		};
		if (Array.isArray(json.violations)) {
			return new OptionValidationError(json.message ?? "", json.violations);
		}
	} catch {
		// JSONでない本文は検証エラーではない
	}
	return null;
};

const ensureOk = async (response: Response) => {
	if (!response.ok) {
		if (response.statusText === OPTION_VALIDATION_ERROR_REASON) {
			const validationError = parseOptionValidationError(await response.text());
			if (validationError) {
				throw validationError;
			}
		}
		throw new Error(
			`Status: ${response.status}\nDetails: ${response.statusText || "Fetch request failed"}`,
		);
	}
	return response;
};

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
	return ensureOk(response);
};

// 中断・ログの対象（sidecarの @Interruptible）ならSSEで、対象外なら通常の応答で返る。
// 対象外のエンドポイントが406にならないよう、ワイルドカードを低い優先度で併記する
const JOB_ACCEPT = "text/event-stream, */*;q=0.1";
// 実行中のログを求めるときだけ付ける（無ければsidecarはログを整形・送信しない）
const JOB_LOG_HEADER = "X-Job-Log";

type SseEvent = { name: string | null; data: string | null };

// SSEの1イベント分（空行区切りのブロック）からevent名とdata行を取り出す（コメント行は読み飛ばす）
const parseSseEvent = (block: string): SseEvent => {
	const lines = block.split("\n");
	const name = lines.find((line) => line.startsWith("event:"));
	const data = lines
		.filter((line) => line.startsWith("data:"))
		.map((line) => line.slice("data:".length).trimStart());
	return {
		name: name ? name.slice("event:".length).trim() : null,
		data: data.length > 0 ? data.join("\n") : null,
	};
};

type JobResult = { status: number; reason: string | null; body: string };

// ジョブのSSEを読み、logはonLogへ渡し、最後のresultから通常の応答を組み立て直す
const readJobStream = async (
	body: ReadableStream<Uint8Array>,
	signal: AbortSignal | undefined,
	onLog: ((line: string) => void) | undefined,
): Promise<Response> => {
	const reader = body.getReader();
	const handleAbort = () => {
		reader.cancel().catch(() => undefined);
	};
	signal?.addEventListener("abort", handleAbort, { once: true });
	const decoder = new TextDecoder();
	let buffer = "";
	try {
		for (;;) {
			const { done, value } = await reader.read();
			signal?.throwIfAborted();
			if (done) {
				throw new Error("Job stream closed without result");
			}
			buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, "\n");
			let separator = buffer.indexOf("\n\n");
			while (separator >= 0) {
				const event = parseSseEvent(buffer.slice(0, separator));
				buffer = buffer.slice(separator + 2);
				if (event.name === "result" && event.data !== null) {
					const result = JSON.parse(event.data) as JobResult;
					return new Response(result.body, {
						status: result.status,
						statusText: result.reason ?? "",
					});
				}
				if (event.name === "log" && event.data !== null) {
					onLog?.(event.data);
				}
				separator = buffer.indexOf("\n\n");
			}
		}
	} catch (ex) {
		// 中断で読み取りが失敗した場合は、通常の呼び出しと同じくAbortErrorとして扱う
		signal?.throwIfAborted();
		throw ex;
	} finally {
		signal?.removeEventListener("abort", handleAbort);
	}
};

// ジョブとして呼ぶ。ログと結果は1本のSSEで届き、中断はsignalによる切断でsidecarへ伝わる
const fetchWithJob = async (
	endpoint: string,
	options: RequestInit,
	signal: AbortSignal | undefined,
	onLog: ((line: string) => void) | undefined,
): Promise<Response> => {
	const headers = new Headers(options.headers);
	headers.set("Accept", JOB_ACCEPT);
	if (onLog) {
		headers.set(JOB_LOG_HEADER, "true");
	}
	const response = await fetchWithRetry(endpoint, {
		...options,
		headers,
		signal,
	});
	const contentType = response.headers.get("Content-Type") ?? "";
	if (!contentType.startsWith("text/event-stream") || !response.body) {
		return response;
	}
	return ensureOk(await readJobStream(response.body, signal, onLog));
};

export const fetchData = async ({
	endpoint,
	options,
	signal,
	timeoutMs,
	onLog,
}: FetchParams) => {
	let timeoutSignal: AbortSignal | undefined;
	let timeoutId: ReturnType<typeof setTimeout> | null = null;
	if (timeoutMs !== undefined) {
		const controller = new AbortController();
		timeoutSignal = controller.signal;
		timeoutId = setTimeout(() => controller.abort(), timeoutMs);
	}
	// 呼び出し側の中断とタイムアウトのどちらでも中断する
	const callerSignal = signal ?? options.signal ?? undefined;
	const mergedSignal =
		callerSignal && timeoutSignal
			? AbortSignal.any([callerSignal, timeoutSignal])
			: (callerSignal ?? timeoutSignal);
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
