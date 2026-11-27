// 比較のkeys / comparesの列（"old:new"または"列"）を選んだ順に持つ。選択はold側の列名で判定する

// "old:new"のold側。":"が無ければ両方で同じ名前
export function oldColumnOf(entry: string): string {
	return entry.split(":")[0];
}

// "old:new"のnew側。":"が無ければold側と同じ名前
export function newColumnOf(entry: string): string {
	const [oldColumn, newColumn] = entry.split(":");
	return newColumn ?? oldColumn;
}

// 同名なら":"を付けない
function compareEntry(oldColumn: string, newColumn: string): string {
	return oldColumn === newColumn ? oldColumn : `${oldColumn}:${newColumn}`;
}

export function selectedOldColumns(entries: string[]): Set<string> {
	return new Set(entries.map(oldColumnOf));
}

// 完全一致を優先し、無ければ大文字小文字を無視して一致する列
export function matchNewColumn(
	oldColumn: string,
	newColumns: string[],
): string | undefined {
	if (newColumns.includes(oldColumn)) {
		return oldColumn;
	}
	const lower = oldColumn.toLowerCase();
	return newColumns.find((column) => column.toLowerCase() === lower);
}

// 選ぶ列は未選択のものだけ末尾に足し（選んだ順が複合キー・比較列の順になる）、外す列は対応ごと除く。
// 足す列のnew側はnewColumnsで一致した列にし、一致が無ければ同名のままにする
export function toggleCompareColumns(
	entries: string[],
	columns: string[],
	checked: boolean,
	newColumns: string[] = [],
): string[] {
	if (!checked) {
		return entries.filter((entry) => !columns.includes(oldColumnOf(entry)));
	}
	const selected = selectedOldColumns(entries);
	return [
		...entries,
		...columns
			.filter((column) => !selected.has(column))
			.map((column) =>
				compareEntry(column, matchNewColumn(column, newColumns) ?? column),
			),
	];
}

export function changeNewColumn(
	entries: string[],
	oldColumn: string,
	newColumn: string,
): string[] {
	return entries.map((entry) =>
		oldColumnOf(entry) === oldColumn
			? compareEntry(oldColumn, newColumn)
			: entry,
	);
}
