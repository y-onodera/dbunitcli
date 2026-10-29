import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import {
	fetchData,
	getErrorMessage,
	handleFetchError,
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

export const useSaveParameterizeTemplate = () => {
	const updateResourceFiles = useUpdateResourceFiles("parameterizeTemplate");
	return (name: string, content: string) =>
		updateResourceFiles("save", JSON.stringify({ name, input: { content } }));
};

export const useSaveTemplate = () => {
	const updateResourceFiles = useUpdateResourceFiles("template");
	return (name: string, content: string) =>
		updateResourceFiles("save", JSON.stringify({ name, input: { content } }));
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
