export type InsertResult = { value: string; cursor: number };

export function insertAtCursor(
	value: string,
	text: string,
	start: number,
	end: number,
): InsertResult {
	const before = value.slice(0, start);
	const after = value.slice(end);
	const prefix = before === "" || before.endsWith("\n") ? "" : "\n";
	const suffix = after.startsWith("\n") ? "" : "\n";
	const inserted = `${prefix}${text}${suffix}`;
	return {
		value: before + inserted + after,
		cursor: before.length + inserted.length,
	};
}

// 行頭の字下げと、つなぎ（AND / OR / ON / ,）の後ろまでを、その行の要素の位置とする
const ITEM_POSITION = /^\s*(?:(?:AND|OR|ON)\s+|,\s*)?/i;

// 改行を足さずにカーソル位置へ挿入する（条件の右辺など行の途中に入れるとき）。
// 2行目以降は、カーソルのある行の要素の位置に揃える
export function insertInlineAtCursor(
	value: string,
	lines: string[],
	start: number,
	end: number,
): InsertResult {
	const before = value.slice(0, start);
	const after = value.slice(end);
	const currentLine = before.slice(before.lastIndexOf("\n") + 1);
	const position = " ".repeat(
		(ITEM_POSITION.exec(currentLine)?.[0] ?? "").length,
	);
	const [first = "", ...rest] = lines;
	const inserted = [first, ...indentLines(rest, position)].join("\n");
	return {
		value: before + inserted + after,
		cursor: before.length + inserted.length,
	};
}

export type TableUsage =
	| "select"
	| "insert"
	| "update"
	| "delete"
	| "leftJoin"
	| "innerJoin"
	| "crossJoin"
	| "columns";

// 句の中身の字下げ。JOINはFROMの中身と同じ位置に置き、結合先テーブルとONはさらに下げる
const INDENT = "    ";
const JOIN_TABLE_INDENT = INDENT.repeat(2);
const ON_INDENT = `${JOIN_TABLE_INDENT}  `;

export function isJoin(usage: TableUsage): boolean {
	return usage === "leftJoin" || usage === "innerJoin" || usage === "crossJoin";
}

// INSERT INTO の対象に別名を付けられないDBがあるため、insertでは別名を使わない
export function usesTableAlias(usage: TableUsage): boolean {
	return usage !== "insert";
}

// スキーマ修飾は除き、スネークケースの各語の先頭文字をつなげる（USER_ACCOUNT → UA）
export function defaultTableAlias(table: string): string {
	const name = table.slice(table.lastIndexOf(".") + 1);
	return name
		.split("_")
		.filter((word) => word !== "")
		.map((word) => word[0])
		.join("")
		.toUpperCase();
}

// csvqでCSVファイルをテーブルとして読む式。パスはSQLの文字列リテラルにするため ' を重ね、区切りは / に揃える
export function csvReadExpression(path: string, charset: string): string {
	const literal = (value: string) => `'${value.replace(/'/g, "''")}'`;
	return `CSVREAD(${literal(path.replace(/\\/g, "/"))},null,${literal(`charset=${charset.trim()}`)})`;
}

// ファイル名（拡張子を除く）から defaultTableAlias と同じ規則で別名を作る
export function csvFileAlias(path: string): string {
	const fileName = path.slice(
		Math.max(path.lastIndexOf("/"), path.lastIndexOf("\\")) + 1,
	);
	const dot = fileName.lastIndexOf(".");
	return defaultTableAlias(dot > 0 ? fileName.slice(0, dot) : fileName);
}

// テーブルに続けて列を選び、文全体を組み立てる用途
const STATEMENT_COLUMN_USAGE = {
	select: "select",
	insert: "insert",
	update: "update",
	leftJoin: "on",
	innerJoin: "on",
} as const satisfies Partial<Record<TableUsage, ColumnUsage>>;

export type StatementUsage = keyof typeof STATEMENT_COLUMN_USAGE;

export function isStatementUsage(usage: TableUsage): usage is StatementUsage {
	return usage in STATEMENT_COLUMN_USAGE;
}

export function statementColumnUsage(usage: StatementUsage): ColumnUsage {
	return STATEMENT_COLUMN_USAGE[usage];
}

// columnsはcolumnLinesで statementColumnUsage の用途に整え、キーワード行を付けていない行。StatementUsage以外では使わない
export function tableLines(
	table: string,
	usage: Exclude<TableUsage, "columns">,
	alias: string,
	columns: string[],
): string[] {
	const named = namedTable(table, usesTableAlias(usage) ? alias : "");
	switch (usage) {
		case "select":
			return ["SELECT", ...columns, "FROM", `${INDENT}${named}`];
		case "insert":
			return [
				"INSERT INTO",
				`${INDENT}${named}`,
				"(",
				...columns,
				")",
				"VALUES",
				"(",
				")",
			];
		case "update":
			return ["UPDATE", `${INDENT}${named}`, "SET", ...columns];
		case "delete":
			return ["DELETE FROM", `${INDENT}${named}`];
		default:
			return [
				`${INDENT}${JOIN_KEYWORDS[usage]}`,
				`${JOIN_TABLE_INDENT}${named}`,
				...joinColumns(usage, columns),
			];
	}
}

function namedTable(table: string, alias: string): string {
	const trimmed = alias.trim();
	return trimmed === "" ? table : `${table} ${trimmed}`;
}

function indentLines(lines: string[], indent: string = INDENT): string[] {
	return lines.map((line) => `${indent}${line}`);
}

// CROSS JOIN は ON を持たない
function joinColumns(
	usage: Exclude<DerivedTableUsage, "select">,
	columns: string[],
): string[] {
	return usage === "crossJoin" ? [] : columns;
}

// テーブルの代わりにサブクエリを置ける用途
export type DerivedTableUsage = Extract<
	TableUsage,
	"select" | "leftJoin" | "innerJoin" | "crossJoin"
>;

export function usesDerivedTable(
	usage: TableUsage,
): usage is DerivedTableUsage {
	return usage === "select" || isJoin(usage);
}

const JOIN_KEYWORDS: Record<Exclude<DerivedTableUsage, "select">, string> = {
	leftJoin: "LEFT JOIN",
	innerJoin: "INNER JOIN",
	crossJoin: "CROSS JOIN",
};

// 括弧はFROM / JOINの行末で開き、閉じ括弧の後ろにサブクエリの別名を置く。
// subqueryはsubqueryLinesの行、columnsはtableLinesと同じく外側の列の行
export function derivedTableLines(
	usage: DerivedTableUsage,
	subquery: string[],
	alias: string,
	columns: string[],
): string[] {
	const close = `) ${alias.trim()}`;
	if (usage === "select") {
		return ["SELECT", ...columns, "FROM (", ...indentLines(subquery), close];
	}
	return [
		`${INDENT}${JOIN_KEYWORDS[usage]} (`,
		...indentLines(subquery, JOIN_TABLE_INDENT),
		`${INDENT}${close}`,
		...joinColumns(usage, columns),
	];
}

export type ColumnUsage =
	| "select"
	| "orderBy"
	| "groupBy"
	| "where"
	| "on"
	| "update"
	| "insert"
	| "scalar";

// keywordは列の前の行に置く句。first/nextは1行目・2行目以降の行頭
type ColumnFormat = { keyword: string | null; first: string; next: string };

const LIST = { first: INDENT, next: "  , " };
const COLUMN_FORMATS: Record<ColumnUsage, ColumnFormat> = {
	select: { keyword: null, ...LIST },
	orderBy: { keyword: "ORDER BY", ...LIST },
	groupBy: { keyword: "GROUP BY", ...LIST },
	where: { keyword: "WHERE", first: INDENT, next: "AND " },
	on: { keyword: null, first: `${ON_INDENT}ON  `, next: `${ON_INDENT}AND ` },
	update: { keyword: "SET", ...LIST },
	insert: { keyword: null, ...LIST },
	scalar: { keyword: null, ...LIST },
};

export function columnKeyword(usage: ColumnUsage): string | null {
	return COLUMN_FORMATS[usage].keyword;
}

export function columnSeparator(usage: ColumnUsage): string {
	return COLUMN_FORMATS[usage].next.trim();
}

export function usesColumnQualifier(usage: ColumnUsage): boolean {
	return usage !== "insert";
}

// aliasは空なら修飾しない。columnAsはselectのときだけ使う。
// continuedなら1行目にも区切りを付け（既存の列の後ろに続けるとき）、キーワード行は付けない
export function columnLines(
	columns: string[],
	usage: ColumnUsage,
	options: {
		alias: string;
		columnAs: boolean;
		continued?: boolean;
		keyword?: boolean;
	},
): string[] {
	const format = COLUMN_FORMATS[usage];
	const alias = usesColumnQualifier(usage) ? options.alias : "";
	const lines = columns.map((column, i) => {
		const head = i === 0 && !options.continued ? format.first : format.next;
		return `${head}${columnBody(qualifyColumn(column, alias), column, usage, options)}`;
	});
	if (options.keyword && !options.continued && format.keyword !== null) {
		return [format.keyword, ...lines];
	}
	return lines;
}

function columnBody(
	qualified: string,
	column: string,
	usage: ColumnUsage,
	options: { columnAs: boolean },
): string {
	if (usage === "select" && options.columnAs) {
		return `${qualified} AS ${column}`;
	}
	if (usage === "where" || usage === "on" || usage === "update") {
		return `${qualified} = `;
	}
	return qualified;
}

export function qualifyColumn(column: string, alias: string): string {
	const trimmed = alias.trim();
	return trimmed === "" ? column : `${trimmed}.${column}`;
}

export function isConditionUsage(usage: ColumnUsage): usage is ConditionUsage {
	return usage === "where" || usage === "on";
}

export type ConditionUsage = Extract<ColumnUsage, "where" | "on">;
export type ConditionOperator = "equals" | "isNull" | "in" | "exists";
export type Conjunction = "AND" | "OR";

// 複数の列を条件にできる演算子。INは1列、EXISTSは列を使わない
export function isMultiColumnOperator(operator: ConditionOperator): boolean {
	return operator === "equals" || operator === "isNull";
}

// 先頭の条件として = を AND でつなぐ（WHERE の行付き）。サブクエリの WHERE などはこれを元に変える
export const DEFAULT_CONDITION_OPTIONS: ConditionOptions = {
	operator: "equals",
	not: false,
	conjunction: "AND",
	group: false,
	connector: null,
	keyword: true,
	inValues: "",
	subquery: null,
};

export type ConditionOptions = {
	operator: ConditionOperator;
	not: boolean;
	// 列どうしのつなぎ
	conjunction: Conjunction;
	// 列の条件を () でまとめる（isMultiColumnOperatorのときだけ使う）
	group: boolean;
	// 既存の条件の後ろに続けるときのつなぎ。nullなら先頭の条件として WHERE / ON から始める
	connector: Conjunction | null;
	keyword: boolean;
	// IN の値（1行にまとめて入れる）。subqueryがあればそちらを使う
	inValues: string;
	// IN / EXISTS のサブクエリ（subqueryLinesの行）
	subquery: string[] | null;
};

const CONDITION_BASE: Record<ConditionUsage, string> = {
	where: "",
	on: ON_INDENT,
};

// 行頭のつなぎは4文字幅に揃え、条件の位置を合わせる
function connectorHead(base: string, connector: string): string {
	return `${base}${connector.padEnd(INDENT.length)}`;
}

// 1つの条件。サブクエリを含むときは複数行になり、2行目以降は条件の位置からの字下げで返す
function conditionExpression(
	column: string,
	options: ConditionOptions,
): string[] {
	const not = options.not ? "NOT " : "";
	switch (options.operator) {
		case "equals":
			return [options.not ? `${column} != ` : `${column} = `];
		case "isNull":
			return [`${column} IS ${not}NULL`];
		case "in":
			if (options.subquery === null) {
				return [`${column} ${not}IN (${options.inValues.trim()})`];
			}
			return [`${column} ${not}IN (`, ...indentLines(options.subquery), ")"];
		case "exists":
			return [`${not}EXISTS (`, ...indentLines(options.subquery ?? []), ")"];
	}
}

// columnsは修飾済みの列。EXISTSでは使わない
export function conditionLines(
	columns: string[],
	usage: ConditionUsage,
	options: ConditionOptions,
): string[] {
	const base = CONDITION_BASE[usage];
	const firstConnector = options.connector ?? (usage === "on" ? "ON" : "");
	const firstHead = connectorHead(base, firstConnector);
	const position = " ".repeat(firstHead.length);
	const targets = options.operator === "exists" ? [""] : columns;
	const lines =
		isMultiColumnOperator(options.operator) && options.group
			? groupedLines(targets, firstHead, position, options)
			: targets.flatMap((column, i) => {
					const [first, ...rest] = conditionExpression(column, options);
					const head =
						i === 0 ? firstHead : connectorHead(base, options.conjunction);
					return [`${head}${first}`, ...indentLines(rest, position)];
				});
	if (usage === "where" && options.keyword && options.connector === null) {
		return ["WHERE", ...lines];
	}
	return lines;
}

function groupedLines(
	columns: string[],
	firstHead: string,
	position: string,
	options: ConditionOptions,
): string[] {
	const inner = `${position}${INDENT}`;
	return [
		`${firstHead}(`,
		...columns.map((column, i) => {
			const head =
				i === 0 ? inner : connectorHead(position, options.conjunction);
			return `${head}${conditionExpression(column, options)[0]}`;
		}),
		`${position})`,
	];
}

// サブクエリのSELECT文。selectListとwhereは字下げ済みの行（whereは空なら付けない）
export function subqueryLines(
	table: string,
	alias: string,
	selectList: string[],
	where: string[],
): string[] {
	return [
		"SELECT",
		...selectList,
		"FROM",
		`${INDENT}${namedTable(table, alias)}`,
		...where,
	];
}

// 外側の別名と重ならないよう、重なるときは数字を付ける（T → T2）
export function uniqueAlias(alias: string, used: string[]): string {
	if (alias === "" || !used.includes(alias)) {
		return alias;
	}
	let n = 2;
	while (used.includes(`${alias}${n}`)) {
		n++;
	}
	return `${alias}${n}`;
}

export type ScalarSource = "column" | "case" | "subquery";
export type ScalarPlace = "select" | "orderBy" | "condition";

// 式の行。columnは修飾済みの列（subqueryでは使わない）、subqueryはsubqueryLinesの行
export function scalarExpression(
	source: ScalarSource,
	column: string,
	options: { whenCount: number; subquery: string[] | null },
): string[] {
	switch (source) {
		case "column":
			return [column];
		case "case":
			return [
				"CASE",
				...Array.from(
					{ length: Math.max(1, options.whenCount) },
					() => `${INDENT}WHEN ${column} =  THEN `,
				),
				`${INDENT}ELSE `,
				"END",
			];
		case "subquery":
			return ["(", ...indentLines(options.subquery ?? []), ")"];
	}
}

// 1行の式は1行にまとめ、複数行の式はCOALESCEの括弧を外側で開いて字下げする
export function withCoalesce(expression: string[], fallback: string): string[] {
	const value = fallback.trim();
	if (expression.length === 1) {
		return [`COALESCE(${expression[0]}, ${value})`];
	}
	return ["COALESCE(", ...indentLines(expression), `  , ${value}`, ")"];
}

// SELECT / ORDER BY の列の位置に置く。continuedなら1行目を「  , 」で始める。asNameはSELECTのときだけ付ける
export function placeScalar(
	expression: string[],
	place: Exclude<ScalarPlace, "condition">,
	options: { continued: boolean; asName: string },
): string[] {
	const head = options.continued ? LIST.next : LIST.first;
	const asName = options.asName.trim();
	const last = expression.length - 1;
	return expression.map((line, i) => {
		const suffix =
			place === "select" && i === last && asName !== "" ? ` AS ${asName}` : "";
		return `${i === 0 ? head : INDENT}${line}${suffix}`;
	});
}
