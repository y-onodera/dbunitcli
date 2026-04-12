import { useCallback, useEffect, useState } from "react";
import { useEnviroment } from "../context/EnviromentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import {
	fetchData,
	getErrorMessage,
	handleFetchError,
	type OperationResult,
} from "../utils/fetchUtils";

export const useTemplateData = (name: string): { content: string; loading: boolean } => {
	const { apiUrl } = useEnviroment();
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
	const { apiUrl } = useEnviroment();
	const setResourcesSettings = useSetResourcesSettings();
	return useCallback(
		async (name: string): Promise<OperationResult> => {
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
				setResourcesSettings((current) =>
					current.with({ templateFiles: settings }),
				);
				return "success";
			} catch (e) {
				handleFetchError(getErrorMessage(e), params);
				return "failed";
			}
		},
		[apiUrl, setResourcesSettings],
	);
};

export const useTemplateSaveContent = () => {
	const { apiUrl } = useEnviroment();
	const setResourcesSettings = useSetResourcesSettings();
	return useCallback(
		async (name: string, content: string): Promise<OperationResult> => {
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
				setResourcesSettings((current) =>
					current.with({ templateFiles: settings }),
				);
				return "success";
			} catch (e) {
				handleFetchError(getErrorMessage(e), params);
				return "failed";
			}
		},
		[apiUrl, setResourcesSettings],
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
