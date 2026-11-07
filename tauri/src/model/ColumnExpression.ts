// from・to・argumentは文字列リテラルとして、default・start・endはJEXL式のまま埋め込む
export type ColumnExpressionArg =
	| "default"
	| "from"
	| "to"
	| "argument"
	| "start"
	| "end";

// 関数ごとに入力させる引数（並びは入力欄の並び）
export const columnExpressionArgs = {
	none: [],
	tryToNum: ["default"],
	formatDate: ["from", "to", "default"],
	contains: ["argument"],
	startsWith: ["argument"],
	endsWith: ["argument"],
	matches: ["argument"],
	substring: ["start", "end"],
} as const satisfies Record<string, readonly ColumnExpressionArg[]>;

export type ColumnExpressionFunction = keyof typeof columnExpressionArgs;

export type ColumnExpressionOption = { fn: ColumnExpressionFunction } & Record<
	ColumnExpressionArg,
	string
>;

export function newColumnExpressionOption(
	defaultValue: string,
): ColumnExpressionOption {
	return {
		fn: "none",
		default: defaultValue,
		from: "yyyyMMdd",
		to: "yyyy/MM/dd",
		argument: "",
		start: "0",
		end: "",
	};
}

function quoteJexlString(text: string): string {
	return `'${text.replace(/[\\']/g, "\\$&")}'`;
}

// 日本語や記号を含むカラム名はJEXLの識別子に書けないため、get('カラム名')で参照する
export function toColumnExpression(
	column: string,
	option: ColumnExpressionOption,
): string {
	const ref = `get(${quoteJexlString(column)})`;
	const defaultValue = option.default.trim();
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

// valueは追記する文字列。二項演算子は前後に空白を付ける
export const expressionOperators: { value: string; label: string }[] = [
	{ value: " == ", label: "== (equal)" },
	{ value: " != ", label: "!= (not equal)" },
	{ value: " > ", label: "> (greater)" },
	{ value: " < ", label: "< (less)" },
	{ value: " >= ", label: ">= (greater or equal)" },
	{ value: " <= ", label: "<= (less or equal)" },
	{ value: " =~ ", label: "=~ (regex match)" },
	{ value: " !~ ", label: "!~ (regex not match)" },
	{ value: " && ", label: "&& (AND)" },
	{ value: " || ", label: "|| (OR)" },
	{ value: "!", label: "! (NOT)" },
	{ value: " + ", label: "+ (add / concat)" },
	{ value: " - ", label: "- (subtract)" },
	{ value: " * ", label: "* (multiply)" },
	{ value: " / ", label: "/ (divide)" },
	{ value: " % ", label: "% (modulo)" },
	{ value: " ? ", label: "? (ternary)" },
	{ value: " : ", label: ": (ternary)" },
	{ value: " == null", label: "== null" },
	{ value: " != null", label: "!= null" },
];
