import { useResourceContent, useUpdateResourceFiles } from "./useResourceFiles";

export const useTemplateData = (name: string) =>
	useResourceContent("template", name);

export const useParameterizeTemplateData = (name: string) =>
	useResourceContent("parameterize/template", name);

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
