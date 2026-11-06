export type ColumnExpressionFunction = "none" | "tryToNum" | "formatDate";

// 読み込んだカラムをJEXL式として挿入するときの書式
export type ColumnExpressionOption = {
	fn: ColumnExpressionFunction;
	// tryToNum・formatDateの既定値（JEXL式のまま埋め込む）
	defaultValue: string;
	// formatDateの書式（文字列リテラルとして埋め込む）
	from: string;
	to: string;
};

export function newColumnExpressionOption(
	defaultValue: string,
): ColumnExpressionOption {
	return {
		fn: "none",
		defaultValue,
		from: "yyyyMMdd",
		to: "yyyy/MM/dd",
	};
}

export function quoteJexlString(text: string): string {
	return `'${text.replace(/[\\']/g, "\\$&")}'`;
}

// 日本語や記号を含むカラム名はJEXLの識別子に書けないため、get('カラム名')で参照する
export function toColumnExpression(
	column: string,
	option: ColumnExpressionOption,
): string {
	const ref = `get(${quoteJexlString(column)})`;
	const defaultValue = option.defaultValue.trim();
	if (option.fn === "tryToNum") {
		return `tryToNum(${ref}, ${defaultValue || "null"})`;
	}
	if (option.fn === "formatDate") {
		const args = [
			ref,
			quoteJexlString(option.from),
			quoteJexlString(option.to),
		];
		if (defaultValue) {
			args.push(defaultValue);
		}
		return `formatDate(${args.join(", ")})`;
	}
	return ref;
}
