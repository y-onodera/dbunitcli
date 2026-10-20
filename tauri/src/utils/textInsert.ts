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

export function decorateLines(
	items: string[],
	prefix: string,
	suffix: string,
): string[] {
	return items.map((item) => `${prefix}${item}${suffix}`);
}
