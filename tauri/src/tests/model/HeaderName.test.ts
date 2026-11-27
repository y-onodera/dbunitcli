import { describe, expect, it } from "vitest";
import { blankColumnPositions, fillBlankColumns } from "../../model/HeaderName";

describe("blankColumnPositions", () => {
	it.each([
		{ value: "a,,b", expected: [2] },
		{ value: ",a", expected: [1] },
		{ value: "a,b,", expected: [3] },
		{ value: "a, ,b,,", expected: [2, 4, 5] },
		{ value: "a,b", expected: [] },
		{ value: "", expected: [] },
		{ value: "  ", expected: [] },
	])("$valueのブランク列は$expected", ({ value, expected }) => {
		expect(blankColumnPositions(value)).toEqual(expected);
	});
});

describe("fillBlankColumns", () => {
	it("ブランク列だけを1から数えて埋める", () => {
		expect(fillBlankColumns("a,,b, ,", "col")).toBe("a,col1,b,col2,col3");
	});

	it("ブランクが10以上ならブランク数の桁でゼロ埋めする", () => {
		const filled = fillBlankColumns(`a${",".repeat(10)}`, "c");
		expect(filled).toBe("a,c01,c02,c03,c04,c05,c06,c07,c08,c09,c10");
	});

	it("既存の列名と重なる番号は飛ばす", () => {
		expect(fillBlankColumns("col1,,,col3", "col")).toBe("col1,col2,col4,col3");
	});

	it("重複回避で桁に収まらなければ桁を増やす", () => {
		const existing = Array.from({ length: 9 }, (_, i) => `c${i + 1}`);
		expect(fillBlankColumns(`${existing.join(",")},`, "c")).toBe(
			`${existing.join(",")},c01`,
		);
	});

	it("ブランクが無ければそのまま返す", () => {
		expect(fillBlankColumns("a,b", "col")).toBe("a,b");
	});
});
