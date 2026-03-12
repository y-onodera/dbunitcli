import { useEnviroment } from "../context/EnviromentProvider";
import { fetchData, handleFetchError } from "../utils/fetchUtils";

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

export const useTemplateSaveContent = () => {
	const { apiUrl } = useEnviroment();
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
			await fetchData(params);
		} catch (e) {
			handleFetchError((e as Error).message, params);
		}
	};
};
