import { describe, expect, it } from "vitest";
import {
	oldColumnOf,
	selectedOldColumns,
	toggleCompareColumns,
} from "../../model/CompareColumns";

describe("CompareColumns", () => {
	it("old側の列名は':'の前、':'が無ければそのまま", () => {
		expect(oldColumnOf("ID:CODE")).toBe("ID");
		expect(oldColumnOf("SUB")).toBe("SUB");
		expect(selectedOldColumns(["ID:CODE", "SUB"])).toEqual(
			new Set(["ID", "SUB"]),
		);
	});

	it("選ぶと未選択の列だけ末尾に足す", () => {
		expect(toggleCompareColumns(["ID:CODE"], ["ID", "A", "B"], true)).toEqual([
			"ID:CODE",
			"A",
			"B",
		]);
	});

	it("外すと対象の列を対応ごと除く", () => {
		expect(
			toggleCompareColumns(["ID:CODE", "A", "X"], ["ID", "A"], false),
		).toEqual(["X"]);
	});
});
