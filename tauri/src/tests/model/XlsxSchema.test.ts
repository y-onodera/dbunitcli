import { describe, expect, it } from "vitest";
import type {
	CellSettingBuilder,
	RowSettingBuilder,
	XlsxSchemaBuilder,
} from "../../model/XlsxSchema";
import {
	CellSetting,
	createCellSetting,
	createRowSetting,
	RowSetting,
	XlsxSchema,
} from "../../model/XlsxSchema";

describe("RowSettingクラス", () => {
	it("空のRowSettingを作成できること", () => {
		const setting = createRowSetting();
		expect(setting.sheetName).toBe("");
		expect(setting.tableName).toBe("");
		expect(setting.header).toEqual([]);
		expect(setting.dataStart).toBe("");
		expect(setting.columnIndex).toEqual([]);
		expect(setting.breakKey).toEqual([]);
		expect(setting.addFileInfo).toBe(false);
	});

	it("値を指定してRowSettingを作成できること", () => {
		const builder: RowSettingBuilder = {
			sheetName: "Sheet1",
			tableName: "Table1",
			header: ["col1", "col2"],
			dataStart: "A2",
			columnIndex: ["A", "B"],
			breakKey: ["id"],
			addFileInfo: true,
		};
		const setting = new RowSetting(builder);
		expect(setting.sheetName).toBe("Sheet1");
		expect(setting.tableName).toBe("Table1");
		expect(setting.header).toEqual(["col1", "col2"]);
		expect(setting.dataStart).toBe("A2");
		expect(setting.columnIndex).toEqual(["A", "B"]);
		expect(setting.breakKey).toEqual(["id"]);
		expect(setting.addFileInfo).toBe(true);
	});

	it("RowSettingを新しい値で更新できること", () => {
		const setting = createRowSetting();
		const updated = setting.with({
			sheetName: "NewSheet",
			tableName: "NewTable",
		});
		expect(updated.sheetName).toBe("NewSheet");
		expect(updated.tableName).toBe("NewTable");
	});

	it("名前を正しく表示できること", () => {
		const setting = new RowSetting({
			sheetName: "Sheet1",
			tableName: "Table1",
		});
		expect(setting.displayName()).toBe("Sheet1→Table1");
	});

	it("シート名またはテーブル名が未設定の場合は空文字を返却すること", () => {
		const setting = createRowSetting();
		expect(setting.displayName()).toBe("");
	});
});

describe("CellSettingクラス", () => {
	it("空のCellSettingを作成できること", () => {
		const setting = createCellSetting();
		expect(setting.sheetName).toBe("");
		expect(setting.tableName).toBe("");
		expect(setting.header).toEqual([]);
		expect(setting.rows).toEqual([]);
		expect(setting.addFileInfo).toBe(false);
	});

	it("値を指定してCellSettingを作成できること", () => {
		const builder: CellSettingBuilder = {
			sheetName: "Sheet1",
			tableName: "Table1",
			header: ["col1", "col2"],
			rows: [{ cellAddress: ["A1", "B1"] }],
			addFileInfo: true,
		};
		const setting = new CellSetting(builder);
		expect(setting.sheetName).toBe("Sheet1");
		expect(setting.tableName).toBe("Table1");
		expect(setting.header).toEqual(["col1", "col2"]);
		expect(setting.rows).toEqual([{ cellAddress: ["A1", "B1"] }]);
		expect(setting.addFileInfo).toBe(true);
	});

	it("行を置換できること", () => {
		const setting = new CellSetting({
			rows: [{ cellAddress: ["A1", "B1"] }],
		});
		const updated = setting.replaceRows(["C1", "D1"], 0);
		expect(updated.rows).toEqual([{ cellAddress: ["C1", "D1"] }]);
	});

	it("空配列を指定すると行を削除できること", () => {
		const setting = new CellSetting({
			rows: [{ cellAddress: ["A1", "B1"] }],
		});
		const updated = setting.replaceRows([], 0);
		expect(updated.rows).toEqual([]);
	});

	it("CellSettingを新しい値で更新できること", () => {
		const setting = createCellSetting();
		const updated = setting.with({
			sheetName: "NewSheet",
			tableName: "NewTable",
		});
		expect(updated.sheetName).toBe("NewSheet");
		expect(updated.tableName).toBe("NewTable");
	});

	it("既存の行の後ろに新しい行を追加できること", () => {
		const setting = new CellSetting({
			rows: [{ cellAddress: ["A1", "B1"] }],
		});
		const updated = setting.replaceRows(["C1", "D1"], 1);
		expect(updated.rows).toEqual([
			{ cellAddress: ["A1", "B1"] },
			{ cellAddress: ["C1", "D1"] },
		]);
	});

	it("名前を正しく表示できること", () => {
		const setting = new CellSetting({
			sheetName: "Sheet1",
			tableName: "Table1",
		});
		expect(setting.displayName()).toBe("Sheet1→Table1");
	});

	it("シート名またはテーブル名が未設定の場合は空文字を返却すること", () => {
		const setting = createCellSetting();
		expect(setting.displayName()).toBe("");
	});
});

describe("XlsxSchemaクラス", () => {
	it("空のXlsxSchemaを作成できること", () => {
		const schema = XlsxSchema.create();
		expect(schema.rows.length).toBe(1);
		expect(schema.cells.length).toBe(1);
	});

	it("ビルダーからXlsxSchemaを構築できること", () => {
		const builder: XlsxSchemaBuilder = {
			rows: [{ sheetName: "Sheet1", tableName: "Table1" }],
			cells: [{ sheetName: "Sheet2", tableName: "Table2" }],
		};
		const schema = XlsxSchema.build(builder);
		expect(schema.rows[0].sheetName).toBe("Sheet1");
		expect(schema.rows[0].tableName).toBe("Table1");
		expect(schema.cells[0].sheetName).toBe("Sheet2");
		expect(schema.cells[0].tableName).toBe("Table2");
	});

	it("行設定を追加できること", () => {
		const schema = XlsxSchema.create();
		const newSetting = createRowSetting();
		const updated = schema.addRowSetting(newSetting);
		expect(updated.rows.length).toBe(2);
		expect(updated.rows[1]).toBe(newSetting);
	});

	it("行設定を削除できること", () => {
		const schema = XlsxSchema.create();
		const updated = schema.deleteRowSetting(schema.rows[0]);
		expect(updated.rows.length).toBe(0);
	});

	it("行設定を更新できること", () => {
		const schema = XlsxSchema.create();
		const newSetting = createRowSetting();
		const updated = schema.updateRowSetting(schema.rows[0], newSetting);
		expect(updated.rows[0]).toBe(newSetting);
	});

	it("セル設定を追加できること", () => {
		const schema = XlsxSchema.create();
		const newSetting = createCellSetting();
		const updated = schema.addCellSetting(newSetting);
		expect(updated.cells.length).toBe(2);
		expect(updated.cells[1]).toBe(newSetting);
	});

	it("セル設定を削除できること", () => {
		const schema = XlsxSchema.create();
		const updated = schema.deleteCellSetting(schema.cells[0]);
		expect(updated.cells.length).toBe(0);
	});

	it("セル設定を更新できること", () => {
		const schema = XlsxSchema.create();
		const newSetting = createCellSetting();
		const updated = schema.updateCellSetting(schema.cells[0], newSetting);
		expect(updated.cells[0]).toBe(newSetting);
	});

	it("JSONに変換できること", () => {
		const schema = XlsxSchema.build({
			rows: [
				{ sheetName: "Sheet1", tableName: "Table1" },
				{ sheetName: "", tableName: "" },
			],
			cells: [
				{ sheetName: "Sheet2", tableName: "Table2" },
				{ sheetName: "", tableName: "" },
			],
		});
		const json = schema.toJSON();
		expect(json.rows.length).toBe(1);
		expect(json.cells.length).toBe(1);
		expect(json.rows[0].sheetName).toBe("Sheet1");
		expect(json.cells[0].sheetName).toBe("Sheet2");
	});
});

describe("XlsxSchemaのシートパターン", () => {
	it("sheetNameがpatternsのキーならsheetPatternとして読み込む", () => {
		const schema = XlsxSchema.build({
			patterns: {
				master: ".*マスタ.*",
				other: { regex: "Sheet.*", exclude: ["Sheet9"] },
			},
			rows: [{ sheetName: "master", tableName: "T1" }],
			cells: [{ sheetName: "other", tableName: "T2" }],
		});
		expect(schema.rows[0].sheetName).toBe("");
		expect(schema.rows[0].sheetPattern).toEqual({ string: ".*マスタ.*" });
		expect(schema.cells[0].sheetPattern).toEqual({
			regex: "Sheet.*",
			exclude: ["Sheet9"],
		});
	});

	it("patternsのキーでないsheetNameはシート名のまま", () => {
		const schema = XlsxSchema.build({
			patterns: { master: "マスタ" },
			rows: [{ sheetName: "Sheet1", tableName: "T1" }],
		});
		expect(schema.rows[0].sheetName).toBe("Sheet1");
		expect(schema.rows[0].sheetPattern).toBeUndefined();
	});

	it("sheetPatternはpatternsに登録しsheetNameでキーを参照して保存する", () => {
		const schema = new XlsxSchema(
			[
				new RowSetting({
					sheetPattern: { regex: "Sheet.*", exclude: ["Sheet9", ""] },
					tableName: "T1",
				}),
				new RowSetting({ sheetName: "Sheet1", tableName: "T2" }),
			],
			[
				new CellSetting({
					sheetPattern: { regex: "Sheet.*", exclude: ["Sheet9"] },
					tableName: "T3",
				}),
			],
		);
		const json = schema.toJSON();
		const key = "regex:Sheet.* exclude:Sheet9";
		expect(json.patterns).toEqual({
			[key]: { regex: "Sheet.*", exclude: ["Sheet9"] },
		});
		expect(json.rows.map((it) => it.sheetName)).toEqual([key, "Sheet1"]);
		expect(json.rows[0]).not.toHaveProperty("sheetPattern");
		expect(json.cells[0].sheetName).toBe(key);
	});

	it("regexとstringの両方があればregexだけを保存する", () => {
		const json = new XlsxSchema(
			[
				new RowSetting({
					sheetPattern: { string: "マスタ", regex: ".*マスタ" },
					tableName: "T1",
				}),
			],
			[],
		).toJSON();
		expect(json.patterns).toEqual({ "regex:.*マスタ": { regex: ".*マスタ" } });
	});

	it("パターンが空の設定は保存しない・パターンが無ければpatternsを出さない", () => {
		const json = new XlsxSchema(
			[new RowSetting({ sheetPattern: {}, tableName: "T1" })],
			[],
		).toJSON();
		expect(json.rows).toEqual([]);
		expect(json).not.toHaveProperty("patterns");
	});

	it("保存した内容を読み込むと同じパターンに戻る", () => {
		const setting = new RowSetting({
			sheetPattern: { string: "マスタ" },
			tableName: "T1",
		});
		const saved = JSON.parse(
			JSON.stringify(new XlsxSchema([setting], [])),
		) as XlsxSchemaBuilder;
		const loaded = XlsxSchema.build(saved);
		expect(loaded.rows[0].sheetPattern).toEqual({ string: "マスタ" });
		expect(loaded.rows[0].displayName()).toBe("string:マスタ→T1");
	});
});
