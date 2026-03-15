import { useEnviroment } from "../context/EnviromentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import { fetchData, handleFetchError } from "../utils/fetchUtils";

type OperationResult = "success" | "failed";

export const useTemplateLoadContent = () => {
	const { apiUrl } = useEnviroment();
	return async (name: string): Promise<string> => {
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
	};
};

export const useDeleteTemplate = () => {
	const { apiUrl } = useEnviroment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string): Promise<OperationResult> => {
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
	};
};

export const useTemplateSaveContent = () => {
	const { apiUrl } = useEnviroment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string, content: string): Promise<void> => {
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
		} catch (e) {
			handleFetchError((e as Error).message, params);
		}
	};
};
