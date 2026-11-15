// 比較のkeys / comparesの列（"old:new"または"列"）を選んだ順に持つ。選択はold側の列名で判定する

// "old:new"のold側。":"が無ければ両方で同じ名前
export function oldColumnOf(entry: string): string {
	return entry.split(":")[0];
}

export function selectedOldColumns(entries: string[]): Set<string> {
	return new Set(entries.map(oldColumnOf));
}

// 選ぶ列は未選択のものだけ末尾に足し（選んだ順が複合キー・比較列の順になる）、外す列は対応ごと除く
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
