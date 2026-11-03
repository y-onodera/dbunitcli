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

export type TableUsage =
	| "select"
	| "insert"
	| "update"
	| "delete"
	| "leftJoin"
	| "innerJoin"
	| "crossJoin";

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

// テーブルに続けて列を選び、文全体を組み立てる用途
export type StatementUsage = Exclude<TableUsage, "delete" | "crossJoin">;

export function isStatementUsage(usage: TableUsage): usage is StatementUsage {
	return usage !== "delete" && usage !== "crossJoin";
}

const STATEMENT_COLUMN_USAGE: Record<StatementUsage, ColumnUsage> = {
	select: "select",
	insert: "insert",
	update: "update",
	leftJoin: "on",
	innerJoin: "on",
};

export function statementColumnUsage(usage: StatementUsage): ColumnUsage {
	return STATEMENT_COLUMN_USAGE[usage];
}

// columnsはcolumnLinesで statementColumnUsage の用途に整え、キーワード行を付けていない行。StatementUsage以外では使わない
export function tableLines(
	table: string,
	usage: TableUsage,
	alias: string,
	columns: string[],
): string[] {
	const trimmed = usesTableAlias(usage) ? alias.trim() : "";
	const named = trimmed === "" ? table : `${table} ${trimmed}`;
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
		case "leftJoin":
			return [`${INDENT}LEFT JOIN`, `${JOIN_TABLE_INDENT}${named}`, ...columns];
		case "innerJoin":
			return [
				`${INDENT}INNER JOIN`,
				`${JOIN_TABLE_INDENT}${named}`,
				...columns,
			];
		case "crossJoin":
			return [`${INDENT}CROSS JOIN`, `${JOIN_TABLE_INDENT}${named}`];
	}
}

export type ColumnUsage =
	| "select"
	| "orderBy"
	| "groupBy"
	| "where"
	| "on"
	| "update"
	| "insert";

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
