import { useCallback } from "react";
import { useEnviroment } from "../context/EnviromentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import { type OperationResult, fetchData, handleFetchError } from "../utils/fetchUtils";

export const useTemplateLoadContent = () => {
	const { apiUrl } = useEnviroment();
	return useCallback(async (name: string): Promise<string> => {
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
			handleFetchError((e as Error).message, params);
			return "";
		}
	}, [apiUrl]);
};

export const useDeleteTemplate = () => {
	const { apiUrl } = useEnviroment();
	const setResourcesSettings = useSetResourcesSettings();
	return useCallback(async (name: string): Promise<OperationResult> => {
		const params = {
			endpoint: `${apiUrl}template/delete`,
			options: {
				method: "POST",
				headers: { "Content-Type": "text/plain" },
				body: name,
			},
		};
		try {
			const response = await fetchData(params);
			const settings = (await response.json()) as string[];
			setResourcesSettings((current) => current.with({ templateFiles: settings }));
			return "success";
		} catch (e) {
			handleFetchError((e as Error).message, params);
			return "failed";
		}
	}, [apiUrl, setResourcesSettings]);
};

export const useTemplateSaveContent = () => {
	const { apiUrl } = useEnviroment();
	const setResourcesSettings = useSetResourcesSettings();
	return useCallback(async (name: string, content: string): Promise<OperationResult> => {
		const params = {
			endpoint: `${apiUrl}template/save`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ name, input: { content } }),
			},
		};
		try {
			const response = await fetchData(params);
			const settings = (await response.json()) as string[];
			setResourcesSettings((current) => current.with({ templateFiles: settings }));
			return "success";
		} catch (e) {
			handleFetchError((e as Error).message, params);
			return "failed";
		}
	}, [apiUrl, setResourcesSettings]);
};
