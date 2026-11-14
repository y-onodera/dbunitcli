// core の TemplateRender#getAttributeExpression / getAttributeName(name, format) と同じ形の式を組み立てる
export type TemplateExpressionSyntax = {
	parameterAttribute: string;
	varStart: string;
	varStop: string;
};

export const DEFAULT_TEMPLATE_EXPRESSION_SYNTAX: TemplateExpressionSyntax = {
	parameterAttribute: "param",
	varStart: "$",
	varStop: "$",
};

// SqlEscapeStringRenderer の format のうち、ファイルパスに使えるもの
export const NAME_FORMATS = [
	"camelToSnake",
	"camelToUpperSnake",
	"snakeToCamel",
	"snakeToUpperCamel",
	"camelToKebab",
	"camelToUpperKebab",
	"kebabToCamel",
	"kebabToUpperCamel",
] as const;

export function buildTemplateExpression(
	syntax: TemplateExpressionSyntax,
	variable: string,
	format: string,
): string {
	const name = syntax.parameterAttribute
		? `${syntax.parameterAttribute}.${variable}`
		: variable;
	const formatPart = format ? `; format="${format}"` : "";
	return `${syntax.varStart}${name}${formatPart}${syntax.varStop}`;
}
