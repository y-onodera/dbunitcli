import { fetch } from "@tauri-apps/plugin-http";

type ErrorInfo = {
	message: string;
	endpoint: string;
	method: string;
	status: string;
	details: string;
	requestBody: string;
};

const createErrorInfo = (
	// biome-ignore lint/suspicious/noExplicitAny: <explanation>
	ex: any,
	endpoint?: string,
	options?: RequestInit,
): ErrorInfo => ({
	message: ex.message || "An unknown error occurred",
	endpoint: endpoint || "N/A",
	method: options?.method || "GET",
	status: ex.response?.status || "N/A",
	details: ex.toString(),
	requestBody: options?.body ? JSON.stringify(options.body, null, 2) : "",
});
const createErrorMessage = (errorInfo: ErrorInfo): string => {
	return `
An error occurred
Message: ${errorInfo.message}
Endpoint: ${errorInfo.endpoint}
Method: ${errorInfo.method}
Status: ${errorInfo.status}
Request Body: ${errorInfo.requestBody}
Details: ${errorInfo.details}
    `.trim();
};
export const handleFetchError = (
	// biome-ignore lint/suspicious/noExplicitAny: <explanation>
	ex: any,
	endpoint?: string,
	options?: RequestInit,
) => {
	const errorInfo = createErrorInfo(ex, endpoint, options);
	const errorMessage = createErrorMessage(errorInfo);

	alert(errorMessage);
	console.error("Fetch Error:", errorInfo);
};

export const fetchData = async (endpoint: string, options: RequestInit) => {
	try {
		const response = await fetch(endpoint, options);
		if (!response.ok) {
			throw new Error(
				createErrorMessage(
					createErrorInfo(
						{ message: response.statusText || "Fetch request failed" },
						endpoint,
						options,
					),
				),
			);
		}
		return response;
	} catch (ex) {
		handleFetchError(ex, endpoint, options);
		throw ex;
	}
};
