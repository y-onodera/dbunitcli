/**
 * データソースの種類を表す型
 */
export type QueryDatasourceType = "csvq" | "sql" | "table";

/**
 * データソースの情報を表す型
 */
export type QueryDatasource = {
	/**
	 * データソースの種類
	 */
	type: QueryDatasourceType;

	/**
	 * ファイル名
	 */
	name: string;

	/**
	 * ファイルの内容
	 */
	contents: string;
};

/**
 * データソースがSQL関連のタイプ（sql, table, csvq）かどうかを判定する
 * @param type 判定するデータソースの種類
 * @returns SQL関連のデータソースの場合はtrue、それ以外はfalse
 */
export function isSqlRelatedType(type: string): type is QueryDatasourceType {
	return type === "sql" || type === "table" || type === "csvq";
}
