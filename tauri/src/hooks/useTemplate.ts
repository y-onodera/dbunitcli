import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import {
	fetchAndUpdate,
	fetchData,
	getErrorMessage,
	handleFetchError,
	type OperationResult,
} from "../utils/fetchUtils";
import { useUpdateResourceFiles } from "./useResourceFiles";

export const useTemplateData = (name: string) =>
	useResourceContent("template", name);

export const useParameterizeTemplateData = (name: string) =>
	useResourceContent("parameterize/template", name);

const useResourceContent = (
	resource: string,
	name: string,
): { content: string; loading: boolean } => {
	const { apiUrl } = useEnvironment();
	const [content, setContent] = useState("");
	const [loading, setLoading] = useState(name !== "");

	useEffect(() => {
		if (!name) {
			setContent("");
			setLoading(false);
			return;
		}
		const controller = new AbortController();
		setLoading(true);
		loadTemplateContent(apiUrl, resource, name).then((result) => {
			if (!controller.signal.aborted) {
				setContent(result);
				setLoading(false);
			}
		});
		return () => {
			controller.abort();
		};
	}, [resource, name, apiUrl]);

	return { content, loading };
};

// parameterize/template の保存応答(ファイル一覧)は画面状態に保持しないため反映しない
export const useParameterizeTemplateSaveContent = () => {
	const { apiUrl } = useEnvironment();
	return async (name: string, content: string): Promise<OperationResult> =>
		fetchAndUpdate<string[]>(
			{
				endpoint: `${apiUrl}parameterize/template/save`,
				options: {
					method: "POST",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify({ name, input: { content } }),
				},
			},
			() => {},
		);
};

export const useTemplateSaveContent = () => {
	const updateResourceFiles = useUpdateResourceFiles("templateFiles");
	return (name: string, content: string) =>
		updateResourceFiles(
			"template/save",
			JSON.stringify({ name, input: { content } }),
		);
};

async function loadTemplateContent(
	apiUrl: string,
	resource: string,
	name: string,
): Promise<string> {
	const params = {
		endpoint: `${apiUrl}${resource}/load`,
		options: {
			method: "POST",
			headers: { "Content-Type": "text/plain" },
			body: name,
		},
	};
	try {
		const response = await fetchData(params);
		const data = (await response.json()) as { content?: string };
		return data.content ?? "";
	} catch (e) {
		handleFetchError(getErrorMessage(e), params);
		return "";
	}
}
