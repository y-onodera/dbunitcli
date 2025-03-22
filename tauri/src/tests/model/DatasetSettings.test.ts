import { describe, expect, it } from "vitest";
import type { DatasetSettingsBuilder } from "../../model/DatasetSettings";
import {
	DatasetSetting,
	DatasetSettings,
	newDatasetSetting,
} from "../../model/DatasetSettings";

describe("DatasetSettingsクラス", () => {
	it("デフォルト設定でDatasetSettingsインスタンスを作成できること", () => {
		const settings = DatasetSettings.create();
		expect(settings.settings.length).toBe(1);
		expect(settings.commonSettings.length).toBe(1);
	});

	it("ビルダーからDatasetSettingsインスタンスを構築できること", () => {
		const builder: DatasetSettingsBuilder = {
			settings: [{ name: "testSetting" }],
			commonSettings: [{ name: "commonSetting" }],
		};
		const settings = DatasetSettings.build(builder);
		expect(settings.settings.length).toBe(1);
		expect(settings.settings[0].name).toEqual(["testSetting"]);
		expect(settings.commonSettings.length).toBe(1);
		expect(settings.commonSettings[0].name).toEqual(["commonSetting"]);
	});

	it("新しい設定を追加できること", () => {
		const settings = DatasetSettings.create();
		const newSetting = newDatasetSetting();
		const updatedSettings = settings.add(newSetting);
		expect(updatedSettings.settings.length).toBe(2);
	});

	it("設定を削除できること", () => {
		const settings = DatasetSettings.create();
		const updatedSettings = settings.delete(settings.settings[0]);
		expect(updatedSettings.settings.length).toBe(1);
	});

	it("設定を更新できること", () => {
		const settings = DatasetSettings.create();
		const newSetting = newDatasetSetting();
		const updatedSettings = settings.update(settings.settings[0], newSetting);
		expect(updatedSettings.settings[0]).toBe(newSetting);
	});

	it("新しい共通設定を追加できること", () => {
		const settings = DatasetSettings.create();
		const newSetting = newDatasetSetting();
		const updatedSettings = settings.addCommon(newSetting);
		expect(updatedSettings.commonSettings.length).toBe(2);
	});

	it("共通設定を削除できること", () => {
		const settings = DatasetSettings.create();
		const updatedSettings = settings.deleteCommon(settings.commonSettings[0]);
		expect(updatedSettings.commonSettings.length).toBe(1);
	});

	it("共通設定を更新できること", () => {
		const settings = DatasetSettings.create();
		const newSetting = newDatasetSetting();
		const updatedSettings = settings.updateCommon(
			settings.commonSettings[0],
			newSetting,
		);
		expect(updatedSettings.commonSettings[0]).toBe(newSetting);
	});

	it("JSONに変換できること", () => {
		const settings = new DatasetSettings(
			[new DatasetSetting({ name: "test" }), new DatasetSetting({ name: "" })],
			[
				new DatasetSetting({ name: "common" }),
				new DatasetSetting({ name: "" }),
			],
		);
		const json = settings.toJSON();
		// displayName（name）が空のsettingsはJSONに反映されないこと
		expect(json.settings.length).toBe(1);
		expect(json.settings[0].name).toEqual(["test"]);
		expect(json.commonSettings.length).toBe(1);
		expect(json.commonSettings[0].name).toEqual(["common"]);
	});
});

describe("DatasetSettingクラス", () => {
	it("新しいDatasetSettingインスタンスを作成できること", () => {
		const setting = newDatasetSetting();
		expect(setting).toBeInstanceOf(DatasetSetting);
	});

	it("名前を置換できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.replaceName("newName", 0);
		expect(updatedSetting.name).toEqual(["newName"]);
	});

	it("名前を削除できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.removeName(0);
		expect(updatedSetting.name).toEqual([]);
	});

	it("パターンを置換できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.replacePattern({ string: "newPattern" });
		expect(updatedSetting.pattern?.string).toBe("newPattern");
	});

	it("パターン除外を置換できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.replacePatternExclude("newExclude", 0);
		expect(updatedSetting.pattern?.exclude).toEqual(["newExclude"]);
	});

	it("パターン除外に要素を追加できること", () => {
		const setting = newDatasetSetting();
		const setting2 = setting.replacePatternExclude("exclude1", 0);
		const updatedSetting = setting2.replacePatternExclude("exclude2", 1);
		expect(updatedSetting.pattern?.exclude).toEqual(["exclude1", "exclude2"]);
	});

	it("パターン除外を削除できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.removePatternExclude(0);
		expect(updatedSetting.pattern?.exclude).toEqual([]);
	});

	it("結合条件を置換できること", () => {
		const setting = new DatasetSetting({ innerJoin: { left: "oldLeft" } });
		const updatedSetting = setting.replaceJoin({ left: "newLeft" });
		expect(updatedSetting.innerJoin?.left).toBe("newLeft");
	});

	it("結合カラムを置換できること", () => {
		const setting = new DatasetSetting({
			innerJoin: { column: ["oldColumn"] },
		});
		const updatedSetting = setting.replaceJoinColumn("newColumn", 0);
		expect(updatedSetting.innerJoin?.column).toEqual(["newColumn"]);
	});

	it("結合カラムに要素を追加できること", () => {
		const setting = new DatasetSetting({
			innerJoin: { column: ["column1"] },
		});
		const setting2 = setting.replaceJoinColumn("column2", 1);
		expect(setting2.innerJoin?.column).toEqual(["column1", "column2"]);
	});

	it("結合カラムを削除できること", () => {
		const setting = new DatasetSetting({
			innerJoin: { column: ["oldColumn"] },
		});
		const updatedSetting = setting.removeJoinColumn(0);
		expect(updatedSetting.innerJoin?.column).toEqual([]);
	});

	it("分割設定を置換できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.replaceSplit({ tableName: "newTable" });
		expect(updatedSetting.split?.tableName).toBe("newTable");
	});

	it("分割ブレークキーを置換できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.replaceSplitBreakKey("newKey", 0);
		expect(updatedSetting.split?.breakKey).toEqual(["newKey"]);
	});

	it("分割ブレークキーに要素を追加できること", () => {
		const setting = newDatasetSetting();
		const setting2 = setting.replaceSplitBreakKey("key1", 0);
		const updatedSetting = setting2.replaceSplitBreakKey("key2", 1);
		expect(updatedSetting.split?.breakKey).toEqual(["key1", "key2"]);
	});

	it("分割ブレークキーを削除できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.removeSplitBreakKey(0);
		expect(updatedSetting.split?.breakKey).toEqual([]);
	});

	it("キーを置換できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.replaceKeys("newKey", 0);
		expect(updatedSetting.keys).toEqual(["newKey"]);
	});

	it("キーに要素を追加できること", () => {
		const setting = newDatasetSetting();
		const setting2 = setting.replaceKeys("key1", 0);
		const updatedSetting = setting2.replaceKeys("key2", 1);
		expect(updatedSetting.keys).toEqual(["key1", "key2"]);
	});

	it("キーを削除できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.removeKeys(0);
		expect(updatedSetting.keys).toEqual([]);
	});

	it("インクルード設定を置換できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.replaceInclude("newInclude", 0);
		expect(updatedSetting.include).toEqual(["newInclude"]);
	});

	it("インクルード設定に要素を追加できること", () => {
		const setting = newDatasetSetting();
		const setting2 = setting.replaceInclude("include1", 0);
		const updatedSetting = setting2.replaceInclude("include2", 1);
		expect(updatedSetting.include).toEqual(["include1", "include2"]);
	});

	it("インクルード設定を削除できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.removeInclude(0);
		expect(updatedSetting.include).toEqual([]);
	});

	it("除外設定を置換できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.replaceExclude("newExclude", 0);
		expect(updatedSetting.exclude).toEqual(["newExclude"]);
	});

	it("除外設定に要素を追加できること", () => {
		const setting = newDatasetSetting();
		const setting2 = setting.replaceExclude("exclude1", 0);
		const updatedSetting = setting2.replaceExclude("exclude2", 1);
		expect(updatedSetting.exclude).toEqual(["exclude1", "exclude2"]);
	});

	it("除外設定を削除できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.removeExclude(0);
		expect(updatedSetting.exclude).toEqual([]);
	});

	it("文字列設定を置換できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.replaceString(0, { prop: "newString" });
		expect(updatedSetting.string).toEqual({ prop: "newString" });
	});

	it("文字列設定を削除できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.removeString(0);
		expect(updatedSetting.string).toEqual({});
	});

	it("数値設定を置換できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.replaceNumber(0, { prop: "newNumber" });
		expect(updatedSetting.number).toEqual({ prop: "newNumber" });
	});

	it("数値設定を削除できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.removeNumber(0);
		expect(updatedSetting.number).toEqual({});
	});

	it("真偽値設定を置換できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.replaceBoolean(0, { prop: "newBoolean" });
		expect(updatedSetting.boolean).toEqual({ prop: "newBoolean" });
	});

	it("真偽値設定を削除できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.removeBoolean(0);
		expect(updatedSetting.boolean).toEqual({});
	});

	it("関数設定を置換できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.replaceFunction(0, { prop: "newFunction" });
		expect(updatedSetting.function).toEqual({ prop: "newFunction" });
	});

	it("関数設定を削除できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.removeFunction(0);
		expect(updatedSetting.function).toEqual({});
	});

	it("フィルター設定を置換できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.replaceFilter("newFilter", 0);
		expect(updatedSetting.filter).toEqual(["newFilter"]);
	});

	it("フィルター設定に要素を追加できること", () => {
		const setting = newDatasetSetting();
		const setting2 = setting.replaceFilter("filter1", 0);
		const updatedSetting = setting2.replaceFilter("filter2", 1);
		expect(updatedSetting.filter).toEqual(["filter1", "filter2"]);
	});

	it("フィルター設定を削除できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.removeFilter(0);
		expect(updatedSetting.filter).toEqual([]);
	});

	it("ソート設定を置換できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.replaceOrder("newOrder", 0);
		expect(updatedSetting.order).toEqual(["newOrder"]);
	});

	it("ソート設定に要素を追加できること", () => {
		const setting = newDatasetSetting();
		const setting2 = setting.replaceOrder("order1", 0);
		const updatedSetting = setting2.replaceOrder("order2", 1);
		expect(updatedSetting.order).toEqual(["order1", "order2"]);
	});

	it("ソート設定を削除できること", () => {
		const setting = newDatasetSetting();
		const updatedSetting = setting.removeOrder(0);
		expect(updatedSetting.order).toEqual([]);
	});
});
