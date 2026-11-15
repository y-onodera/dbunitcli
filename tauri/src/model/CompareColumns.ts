// generateのcompareセクションのkeys / comparesの値（"old:new"または"列"のカンマ区切り）をoldTableの列で選ぶための変換

export function parseCompareColumns(value: string): string[] {
	return value
		.split(",")
		.map((entry) => entry.trim())
		.filter((entry) => entry !== "");
}

// "old:new"のold側。":"が無ければ両方で同じ名前
export function oldColumnOf(entry: string): string {
	const separator = entry.indexOf(":");
	return separator < 0 ? entry : entry.slice(0, separator);
}

export function selectedOldColumns(entries: string[]): Set<string> {
	return new Set(entries.map(oldColumnOf));
}

// 選んでいれば外し、選んでいなければ末尾に足す（選んだ順が複合キー・比較列の順になる）
export function toggleCompareColumn(
	entries: string[],
	column: string,
): string[] {
	return entries.some((entry) => oldColumnOf(entry) === column)
		? entries.filter((entry) => oldColumnOf(entry) !== column)
		: [...entries, column];
}

export function toggleCompareColumns(
	entries: string[],
	columns: string[],
	checked: boolean,
): string[] {
	if (!checked) {
		return entries.filter((entry) => !columns.includes(oldColumnOf(entry)));
	}
	const selected = selectedOldColumns(entries);
	return [...entries, ...columns.filter((column) => !selected.has(column))];
}
