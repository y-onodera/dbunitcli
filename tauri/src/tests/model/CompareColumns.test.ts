import { describe, expect, it } from "vitest";
import {
	changeNewColumn,
	matchNewColumn,
	newColumnOf,
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

	it("new側の列名は':'の後、':'が無ければold側と同じ", () => {
		expect(newColumnOf("ID:CODE")).toBe("CODE");
		expect(newColumnOf("SUB")).toBe("SUB");
	});

	it("newの列は完全一致を優先し、無ければ大文字小文字を無視して探す", () => {
		expect(matchNewColumn("id", ["ID", "id"])).toBe("id");
		expect(matchNewColumn("Name", ["ID", "NAME"])).toBe("NAME");
		expect(matchNewColumn("AMT", ["ID"])).toBeUndefined();
	});

	it("選ぶとnewの一致した列を対応させ、一致が無ければ同名のまま足す", () => {
		expect(
			toggleCompareColumns(["ID"], ["ID", "Name", "AMT"], true, ["ID", "NAME"]),
		).toEqual(["ID", "Name:NAME", "AMT"]);
	});

	it("new側の列を変えると、同名なら':'を付けない", () => {
		expect(changeNewColumn(["ID", "A:B"], "ID", "CODE")).toEqual([
			"ID:CODE",
			"A:B",
		]);
		expect(changeNewColumn(["ID", "A:B"], "A", "A")).toEqual(["ID", "A"]);
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
