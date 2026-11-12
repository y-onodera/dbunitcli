import { describe, expect, it } from "vitest";
import {
	buildCompareTemplateRequest,
	EMPTY_DATASET_COLUMNS,
	selectTable,
	type XlsxCompareTemplateInput,
} from "../../model/XlsxCompareTemplate";

const input: XlsxCompareTemplateInput = {
	fileName: "cmp",
	compareSheetName: "compare",
	first: { tableName: "T1", sheetName: "old", columns: ["ID", "NAME"] },
	second: { tableName: "T2", sheetName: "new", columns: ["CODE", "NAME"] },
	keys: [{ first: "ID", second: "CODE" }],
	compares: [{ first: "NAME", second: "NAME" }],
};

describe("selectTable", () => {
	it("シート名が未入力か前のテーブル名のままなら新しいテーブル名に合わせ、列は読み直させる", () => {
		const selected = selectTable(EMPTY_DATASET_COLUMNS, "T1");
		expect(selected).toEqual({ tableName: "T1", sheetName: "T1", columns: [] });
		expect(selectTable({ ...selected, columns: ["A"] }, "T2")).toEqual({
			tableName: "T2",
			sheetName: "T2",
			columns: [],
		});
	});

	it("入力したシート名は残す", () => {
		expect(
			selectTable({ tableName: "T1", sheetName: "mine", columns: [] }, "T2")
				.sheetName,
		).toBe("mine");
	});
});

describe("buildCompareTemplateRequest", () => {
	it("未選択のペアは除いて送る", () => {
		const request = buildCompareTemplateRequest({
			...input,
			keys: [...input.keys, { first: "NAME", second: "" }],
			compares: [
				{ first: "NAME", second: "NAME" },
				{ first: "", second: "" },
			],
		});
		expect(request.keys).toEqual([{ first: "ID", second: "CODE" }]);
		expect(request.compares).toEqual([{ first: "NAME", second: "NAME" }]);
	});

	it("comparesがnullならキー以外の全列を同名どうしで比べる", () => {
		const request = buildCompareTemplateRequest({
			...input,
			first: { tableName: "T1", sheetName: "old", columns: ["ID", "A", "B"] },
			keys: [{ first: "ID", second: "ID" }],
			compares: null,
		});
		expect(request.compares).toEqual([
			{ first: "A", second: "A" },
			{ first: "B", second: "B" },
		]);
	});

	it("キーが無ければエラー", () => {
		expect(() =>
			buildCompareTemplateRequest({
				...input,
				keys: [{ first: "", second: "" }],
			}),
		).toThrow("Select at least one key column.");
	});

	it("newのデータセットの列が未読込ならエラー", () => {
		expect(() =>
			buildCompareTemplateRequest({
				...input,
				second: { ...input.first, columns: [] },
			}),
		).toThrow("Load the columns of the new dataset.");
	});
});
