import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import {
	fetchAndUpdate,
	fetchData,
	getErrorMessage,
	handleFetchError,
	type OperationResult,
} from "../utils/fetchUtils";

export const useTemplateData = (name: string): { content: string; loading: boolean } => {
	const { apiUrl } = useEnvironment();
	const [content, setContent] = useState("");
	const [loading, setLoading] = useState(name !== "");

	useEffect(() => {
		if (!name) {
			setContent("");
			setLoading(false);
			return;
		}
		let isMounted = true;
		setLoading(true);
		loadTemplateContent(apiUrl, name).then((result) => {
			if (isMounted) {
				setContent(result);
				setLoading(false);
			}
		});
		return () => {
			isMounted = false;
		};
	}, [name, apiUrl]);

	return { content, loading };
};

export const useDeleteTemplate = () => {
	const { apiUrl } = useEnvironment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string): Promise<OperationResult> =>
		fetchAndUpdate<string[]>(
			{
				endpoint: `${apiUrl}template/delete`,
				options: { method: "POST", headers: { "Content-Type": "text/plain" }, body: name },
			},
			(settings) => setResourcesSettings((current) => current.with({ templateFiles: settings })),
		);
};

export const useTemplateSaveContent = () => {
	const { apiUrl } = useEnvironment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string, content: string): Promise<OperationResult> =>
		fetchAndUpdate<string[]>(
			{
				endpoint: `${apiUrl}template/save`,
				options: { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ name, input: { content } }) },
			},
			(settings) => setResourcesSettings((current) => current.with({ templateFiles: settings })),
		);
};

async function loadTemplateContent(apiUrl: string, name: string): Promise<string> {
	const params = {
		endpoint: `${apiUrl}template/load`,
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
