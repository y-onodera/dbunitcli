export const columnExpressionFunctions = [
	"none",
	"tryToNum",
	"formatDate",
	"contains",
	"startsWith",
	"endsWith",
	"matches",
	"substring",
] as const;

export type ColumnExpressionFunction =
	(typeof columnExpressionFunctions)[number];

// 読み込んだカラムをJEXL式として挿入するときの書式
export type ColumnExpressionOption = {
	fn: ColumnExpressionFunction;
	// tryToNum・formatDateの既定値（JEXL式のまま埋め込む）
	defaultValue: string;
	// formatDateの書式（文字列リテラルとして埋め込む）
	from: string;
	to: string;
	// contains・startsWith・endsWith・matchesの引数（文字列リテラルとして埋め込む）
	argument: string;
	// substringの引数（JEXL式のまま埋め込む）
	start: string;
	end: string;
};

export function newColumnExpressionOption(
	defaultValue: string,
): ColumnExpressionOption {
	return {
		fn: "none",
		defaultValue,
		from: "yyyyMMdd",
		to: "yyyy/MM/dd",
		argument: "",
		start: "0",
		end: "",
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
	switch (option.fn) {
		case "tryToNum":
			return `tryToNum(${ref}, ${defaultValue || "null"})`;
		case "formatDate": {
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
		case "contains":
		case "startsWith":
		case "endsWith":
		case "matches":
			return `${ref}.${option.fn}(${quoteJexlString(option.argument)})`;
		case "substring": {
			const args = [option.start.trim() || "0"];
			if (option.end.trim()) {
				args.push(option.end.trim());
			}
			return `${ref}.substring(${args.join(", ")})`;
		}
		default:
			return ref;
	}
}

// 式に追記する演算子。textは追記する文字列そのもの
export const expressionOperators = [
	{ label: "== (equal)", text: " == " },
	{ label: "!= (not equal)", text: " != " },
	{ label: "> (greater)", text: " > " },
	{ label: "< (less)", text: " < " },
	{ label: ">= (greater or equal)", text: " >= " },
	{ label: "<= (less or equal)", text: " <= " },
	{ label: "=~ (regex match)", text: " =~ " },
	{ label: "!~ (regex not match)", text: " !~ " },
	{ label: "&& (AND)", text: " && " },
	{ label: "|| (OR)", text: " || " },
	{ label: "! (NOT)", text: "!" },
	{ label: "+ (add / concat)", text: " + " },
	{ label: "- (subtract)", text: " - " },
	{ label: "* (multiply)", text: " * " },
	{ label: "/ (divide)", text: " / " },
	{ label: "% (modulo)", text: " % " },
	{ label: "? (ternary)", text: " ? " },
	{ label: ": (ternary)", text: " : " },
	{ label: "== null", text: " == null" },
	{ label: "!= null", text: " != null" },
] as const;
