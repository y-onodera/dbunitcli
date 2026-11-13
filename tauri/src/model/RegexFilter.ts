// -regInclude・-regExclude（ファイルパス）と -regTableInclude・-regTableExclude（setting適用前のテーブル名）を
// 一覧から選んだ値で組み立てる。sidecarのdataset-setting/table-sourcesの応答を使う

// sidecarのDatasetTableSourceDto。filePathはsrcがディレクトリならsrcからの相対パス（区切りは/）
export type TableSource = {
	tableName: string;
	filePath: string;
	sourceTableName: string;
};

export type RegexFilterKind = "path" | "table";

// 一覧の1行: 正規表現を当てる値と、その値から読み込まれる最終的なテーブル名
export type RegexFilterRow = { key: string; tableNames: string[] };

export function escapeRegex(value: string): string {
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

export function filterKey(kind: RegexFilterKind, source: TableSource): string {
	return kind === "path" ? source.filePath : source.sourceTableName;
}

// 正規表現を当てる値ごとにまとめる（並びは応答の順）
export function toFilterRows(
	kind: RegexFilterKind,
	sources: TableSource[],
): RegexFilterRow[] {
	const rows = new Map<string, string[]>();
	for (const source of sources) {
		const key = filterKey(kind, source);
		const tableNames = rows.get(key) ?? [];
		if (!tableNames.includes(source.tableName)) {
			tableNames.push(source.tableName);
		}
		rows.set(key, tableNames);
	}
	return [...rows].map(([key, tableNames]) => ({ key, tableNames }));
}

// 入力中の正規表現に一致する値（初期のチェックに使う）。JavaScriptで解釈できなければ何も選ばない。
// パスはsidecarがsrcを付けたパスに当てるため、相対パスとsrcを付けたパスのどちらかに一致すれば選ぶ
export function matchedKeys(
	kind: RegexFilterKind,
	regex: string,
	keys: string[],
	src: string,
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
	const base = src.replace(/[\\/]+$/, "");
	return keys.filter(
		(key) =>
			pattern.test(key) ||
			(kind === "path" && base !== "" && pattern.test(`${base}/${key}`)),
	);
}
