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

export const fetchData = async ({ endpoint, options, signal }: FetchParams) => {
	const mergedOptions = { ...options, signal: signal ?? options.signal };
	let response: Response;
	try {
		response = await fetch(endpoint, mergedOptions);
	} catch (ex) {
		if (isAbortError(ex)) {
			throw ex;
		}
		// reqwest のコネクションプールに残った stale keep-alive 接続が原因で
		// 接続エラーになる場合があるため、1 回だけリトライする
		response = await fetch(endpoint, mergedOptions);
	}
	if (!response.ok) {
		throw new Error(
			`Status: ${response.status}\nDetails: ${response.statusText || "Fetch request failed"}`,
		);
	}
	return response;
};
