/**
 * データソースの種類を表す型
 */
export type DataSourceType =
	| "none"
	| "csv"
	| "csvq"
	| "fixed"
	| "regex"
	| "xls"
	| "xlsx"
	| "sql"
	| "table"
	| "file"
	| "dir";

/**
 * データソースの情報を表す型
 */
export type DataSource = {
	/**
	 * データソースの種類
	 */
	type: DataSourceType;

	/**
	 * ファイル名
	 */
	fileName: string;

	/**
	 * ファイルの内容
	 */
	contents: string;
};
