import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import XlsxRowSettingDialog from "../../../../../app/form/section/dialog/XlsxRowSettingDialog";
import type { DatasetSrcInfo } from "../../../../../model/CommandOption";
import { RowSetting, XlsxSchema } from "../../../../../model/XlsxSchema";

const baseSrcInfo: DatasetSrcInfo = {
	values: {
		srcType: "xlsx",
		src: "src",
		xlsxSchema: "schema.json",
		setting: "setting.json",
	},
};
const { mockTableNames, srcInfoFixture } = vi.hoisted(() => ({
	mockTableNames: vi.fn(),
	srcInfoFixture: { current: {} as DatasetSrcInfo },
}));
vi.mock("../../../../../hooks/useDatasetSettings", () => ({
	useDatasetTableNames: (srcInfo: DatasetSrcInfo) => {
		mockTableNames(srcInfo);
		return { tableNames: ["after"], loading: false, logs: [] };
	},
	useDatasetTablePreview: () => ({ preview: null, loading: false, logs: [] }),
	useDatasetTableConvert: () => vi.fn(),
}));
vi.mock("../../../../../hooks/useXlsxSchema", () => ({
	useSrcInfoSheets: () => [],
}));
vi.mock("../../../../../context/DatasetSrcInfoProvider", () => ({
	useDatasetSrcInfo: () => srcInfoFixture.current,
}));

function renderDialog() {
	const handleCommit = vi.fn();
	render(
		<XlsxRowSettingDialog
			setting={new RowSetting({ sheetName: "Sheet1", tableName: "before" })}
			handleDialogClose={vi.fn()}
			handleCommit={handleCommit}
		/>,
	);
	return { user: userEvent.setup(), handleCommit };
}

describe("XlsxRowSettingDialogのテスト", () => {
	it("Previewで編集中の設定1件だけのスキーマを使い、settingファイルなしでテーブル一覧をプレビューする", async () => {
		srcInfoFixture.current = baseSrcInfo;
		const { user, handleCommit } = renderDialog();
		const tableName = screen.getByDisplayValue("before");
		await user.clear(tableName);
		await user.type(tableName, "after");
		await user.click(screen.getByRole("button", { name: "Preview" }));

		expect(screen.getByText("Table List Preview")).toBeInTheDocument();
		const expected = new RowSetting({
			sheetName: "Sheet1",
			tableName: "after",
		});
		expect(mockTableNames).toHaveBeenLastCalledWith({
			values: {
				srcType: "xlsx",
				src: "src",
				xlsxSchema: "schema.json",
				setting: "",
			},
			inlineXlsxSchema: new XlsxSchema([expected], []),
		});
		// プレビューしても設定は確定しない
		expect(handleCommit).not.toHaveBeenCalled();
	});

	it("srcTypeが未指定ならPreviewボタンを出さない", () => {
		srcInfoFixture.current = { values: { src: "src" } };
		renderDialog();

		expect(
			screen.queryByRole("button", { name: "Preview" }),
		).not.toBeInTheDocument();
	});
});
