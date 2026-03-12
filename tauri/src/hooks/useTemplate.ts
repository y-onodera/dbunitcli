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
