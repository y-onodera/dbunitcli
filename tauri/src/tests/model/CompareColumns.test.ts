import { describe, expect, it } from "vitest";
import {
	oldColumnOf,
	parseCompareColumns,
	selectedOldColumns,
	toggleCompareColumn,
	toggleCompareColumns,
} from "../../model/CompareColumns";

describe("CompareColumns", () => {
	it("カンマ区切りを空白と空の要素を除いて分ける", () => {
		expect(parseCompareColumns(" ID:CODE , ,SUB,")).toEqual(["ID:CODE", "SUB"]);
		expect(parseCompareColumns("")).toEqual([]);
	});

	it("old側の列名は':'の前、':'が無ければそのまま", () => {
		expect(oldColumnOf("ID:CODE")).toBe("ID");
		expect(oldColumnOf("SUB")).toBe("SUB");
		expect(selectedOldColumns(["ID:CODE", "SUB"])).toEqual(
			new Set(["ID", "SUB"]),
		);
	});

	it("選んでいない列は末尾に足し、選んでいる列は対応ごと外す", () => {
		expect(toggleCompareColumn(["ID:CODE"], "SUB")).toEqual(["ID:CODE", "SUB"]);
		expect(toggleCompareColumn(["ID:CODE", "SUB"], "ID")).toEqual(["SUB"]);
	});

	it("まとめて選ぶと未選択の列だけ足し、外すと対象の列だけ外す", () => {
		expect(toggleCompareColumns(["ID:CODE"], ["ID", "A", "B"], true)).toEqual([
			"ID:CODE",
			"A",
			"B",
		]);
		expect(
			toggleCompareColumns(["ID:CODE", "A", "X"], ["ID", "A"], false),
		).toEqual(["X"]);
	});
});
