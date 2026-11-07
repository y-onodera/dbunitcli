import { describe, expect, it } from "vitest";
import {
	newColumnExpressionOption,
	toColumnExpression,
} from "../../model/ColumnExpression";

describe("toColumnExpressionのテスト", () => {
	it("関数なしはget('カラム名')で参照する", () => {
		expect(toColumnExpression("顧客名", newColumnExpressionOption("0"))).toBe(
			"get('顧客名')",
		);
	});

	it("カラム名の'と\\はエスケープする", () => {
		expect(
			toColumnExpression("O'Brien\\x", newColumnExpressionOption("0")),
		).toBe("get('O\\'Brien\\\\x')");
	});

	it("tryToNumは既定値をそのまま埋め込み、空ならnullにする", () => {
		const option = {
			...newColumnExpressionOption("0"),
			fn: "tryToNum",
		} as const;
		expect(toColumnExpression("金額", option)).toBe("tryToNum(get('金額'), 0)");
		expect(toColumnExpression("金額", { ...option, default: " " })).toBe(
			"tryToNum(get('金額'), null)",
		);
	});

	it("formatDateは書式を文字列にし、既定値があれば4引数にする", () => {
		const option = {
			...newColumnExpressionOption(""),
			fn: "formatDate",
		} as const;
		expect(toColumnExpression("日付", option)).toBe(
			"formatDate(get('日付'), 'yyyyMMdd', 'yyyy/MM/dd')",
		);
		expect(toColumnExpression("日付", { ...option, default: "''" })).toBe(
			"formatDate(get('日付'), 'yyyyMMdd', 'yyyy/MM/dd', '')",
		);
	});

	it("文字列メソッドは引数を文字列にしてget式に続ける", () => {
		const option = {
			...newColumnExpressionOption(""),
			fn: "startsWith",
			argument: "SALE'",
		} as const;
		expect(toColumnExpression("区分", option)).toBe(
			"get('区分').startsWith('SALE\\'')",
		);
	});

	it("substringはendが空なら1引数にする", () => {
		const option = {
			...newColumnExpressionOption(""),
			fn: "substring",
			start: "0",
			end: "4",
		} as const;
		expect(toColumnExpression("日付", option)).toBe(
			"get('日付').substring(0, 4)",
		);
		expect(toColumnExpression("日付", { ...option, end: "" })).toBe(
			"get('日付').substring(0)",
		);
	});
});
