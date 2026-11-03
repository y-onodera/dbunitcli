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

const TABLE_KEYWORDS: Record<TableUsage, string> = {
	select: "from",
	insert: "insert into",
	update: "update",
	delete: "delete from",
	leftJoin: "left join",
	innerJoin: "inner join",
	crossJoin: "cross join",
};

export function isJoin(usage: TableUsage): boolean {
	return usage === "leftJoin" || usage === "innerJoin" || usage === "crossJoin";
}

// insert into の対象に別名を付けられないDBがあるため、insertでは別名を使わない
export function usesTableAlias(usage: TableUsage): boolean {
	return usage !== "insert";
}

// スキーマ修飾は除き、スネークケースの各語の先頭文字をつなげる（USER_ACCOUNT → ua）
export function defaultTableAlias(table: string): string {
	const name = table.slice(table.lastIndexOf(".") + 1);
	return name
		.split("_")
		.filter((word) => word !== "")
		.map((word) => word[0])
		.join("")
		.toLowerCase();
}

export function tableClause(
	table: string,
	usage: TableUsage,
	alias: string,
): string {
	const trimmed = usesTableAlias(usage) ? alias.trim() : "";
	const named = trimmed === "" ? table : `${table} ${trimmed}`;
	const clause = `${TABLE_KEYWORDS[usage]} ${named}`;
	return usage === "leftJoin" || usage === "innerJoin"
		? `${clause} on`
		: clause;
}

export type ColumnUsage =
	| "select"
	| "orderBy"
	| "condition"
	| "update"
	| "insert";

export function columnSeparator(usage: ColumnUsage): string {
	return usage === "condition" ? "and" : ",";
}

export function usesColumnQualifier(usage: ColumnUsage): boolean {
	return usage !== "insert";
}

// aliasは空なら修飾しない。columnAsはselectのときだけ使う。continuedなら1行目にも区切りを付ける（既存の列の後ろに続けるとき）
export function columnLines(
	columns: string[],
	usage: ColumnUsage,
	options: { alias: string; columnAs: boolean; continued?: boolean },
): string[] {
	const alias = usesColumnQualifier(usage) ? options.alias : "";
	const separator = `${columnSeparator(usage)} `;
	return columns.map((column, i) => {
		const body = columnBody(
			qualifyColumn(column, alias),
			column,
			usage,
			options,
		);
		return i === 0 && !options.continued ? body : `${separator}${body}`;
	});
}

function columnBody(
	qualified: string,
	column: string,
	usage: ColumnUsage,
	options: { columnAs: boolean },
): string {
	if (usage === "select" && options.columnAs) {
		return `${qualified} as ${column}`;
	}
	if (usage === "condition" || usage === "update") {
		return `${qualified} = `;
	}
	return qualified;
}

export function qualifyColumn(column: string, alias: string): string {
	const trimmed = alias.trim();
	return trimmed === "" ? column : `${trimmed}.${column}`;
}
