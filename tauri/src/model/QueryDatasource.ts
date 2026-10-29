export type QueryDatasourceType = "csvq" | "sql" | "table";

export function isSqlRelatedType(type: string): type is QueryDatasourceType {
	return type === "sql" || type === "table" || type === "csvq";
}
