// -regInclude・-regExclude（ファイルパス）と -regTableInclude・-regTableExclude（setting適用前のテーブル名）を
// 一覧から選んだ値で組み立てる。sidecarのdataset-setting/table-sourcesの応答を使う

// sidecarのDatasetTableSourceDto
export type TableSource = {
	tableName: string;
	filePath: string;
	matchPath: string;
	sourceTableName: string;
};

export type RegexFilterKind = "path" | "table";

// 一覧の1行: 正規表現を当てる値と、その値から読み込まれる最終的なテーブル名
export type RegexFilterRow = { key: string; tableNames: string[] };

function escapeRegex(value: string): string {
	return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

const PATH_SEPARATOR = "[\\\\/]";

// Javaはパスを部分一致（find）で当てるため、パスの区切りまたは先頭から末尾までの一致にする。区切りはWindowsの\にも合わせる
function pathRegex(paths: string[]): string {
	const alternatives = paths.map((path) =>
		path.split("/").map(escapeRegex).join(PATH_SEPARATOR),
	);
	return `(?:^|${PATH_SEPARATOR})(?:${alternatives.join("|")})$`;
}

function tableNameRegex(names: string[]): string {
	return `^(?:${names.map(escapeRegex).join("|")})$`;
}

export function buildFilterRegex(
	kind: RegexFilterKind,
	keys: string[],
): string {
	if (keys.length === 0) {
		return "";
	}
	return kind === "path" ? pathRegex(keys) : tableNameRegex(keys);
}

function keyOf(kind: RegexFilterKind, source: TableSource): string {
	return kind === "path" ? source.filePath : source.sourceTableName;
}

// 正規表現を当てる値ごとにまとめる（並びは応答の順）
export function toFilterRows(
	kind: RegexFilterKind,
	sources: TableSource[],
): RegexFilterRow[] {
	const rows = new Map<string, string[]>();
	for (const source of sources) {
		const key = keyOf(kind, source);
		const tableNames = rows.get(key) ?? [];
		if (!tableNames.includes(source.tableName)) {
			tableNames.push(source.tableName);
		}
		rows.set(key, tableNames);
	}
	return [...rows].map(([key, tableNames]) => ({ key, tableNames }));
}

// 入力中の正規表現に一致する値（初期のチェックに使う）。JavaScriptで解釈できなければ何も選ばない
export function matchedKeys(
	kind: RegexFilterKind,
	regex: string,
	sources: TableSource[],
): string[] {
	if (regex === "") {
		return [];
	}
	let pattern: RegExp;
	try {
		pattern = new RegExp(regex);
	} catch {
		return [];
	}
	return sources
		.filter((source) =>
			pattern.test(kind === "path" ? source.matchPath : source.sourceTableName),
		)
		.map((source) => keyOf(kind, source));
}
