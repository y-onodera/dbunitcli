import type { QueryDatasourceType } from "../model/QueryDatasource";
import { useResourceContent, useUpdateResourceFiles } from "./useResourceFiles";

export const useQueryDatasourceData = (
	type: QueryDatasourceType,
	name: string,
) => useResourceContent(type, name);

export const useSaveQueryDatasource = (type: QueryDatasourceType) => {
	const updateResourceFiles = useUpdateResourceFiles(type);
	return (name: string, content: string) =>
		updateResourceFiles("save", JSON.stringify({ name, input: { content } }));
};
