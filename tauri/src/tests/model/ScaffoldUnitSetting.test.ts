import { describe, expect, it } from "vitest";
import fixedColumnDefSettings from "../../../../core/src/main/resources/fixedcolumndef/fixedColumnDefSettings.json";
import javaBeanSettings from "../../../../core/src/main/resources/javabean/javaBeanSettings.json";
import ddlSettings from "../../../../core/src/main/resources/sql/ddlSettings.json";
import xlsxSchemaSettings from "../../../../core/src/main/resources/xlsxschema/xlsxSchemaSettings.json";
import {
	DatasetSettings,
	type DatasetSettingsBuilder,
} from "../../model/DatasetSettings";

// 保存時に補われる空の既定値（空配列・空オブジェクト・false）を除いて比較する
function stripEmpty(value: unknown): unknown {
	if (Array.isArray(value)) {
		return value.map(stripEmpty);
	}
	if (value !== null && typeof value === "object") {
		return Object.fromEntries(
			Object.entries(value)
				.map(([key, it]) => [key, stripEmpty(it)] as const)
				.filter(([, it]) => !isEmpty(it)),
		);
	}
	return value;
}
function isEmpty(value: unknown): boolean {
	if (Array.isArray(value)) {
		return value.length === 0;
	}
	if (value !== null && typeof value === "object") {
		return Object.keys(value).length === 0;
	}
	return value === false;
}

// ScaffoldでWorkspaceへ書き出すunitSettingのサンプルが、Dataset Settingsダイアログで開いて保存しても欠けないこと
describe("Scaffoldのunitsettingサンプルの読み込み・保存", () => {
	it.each([
		["ddl", ddlSettings],
		["javaBean", javaBeanSettings],
		["xlsxSchema", xlsxSchemaSettings],
		["fixedColumnDef", fixedColumnDefSettings],
	])("%s", (_, source) => {
		const saved = JSON.parse(
			JSON.stringify(DatasetSettings.build(source as DatasetSettingsBuilder)),
		);
		expect(stripEmpty(saved)).toEqual(stripEmpty(source));
	});
});
