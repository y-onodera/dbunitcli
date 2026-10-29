import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import DatasetSettingDialog from "../../../../../app/form/section/dialog/DatasetSettingDialog";
import type { DatasetSrcInfo } from "../../../../../model/CommandOption";
import { DatasetSetting } from "../../../../../model/DatasetSettings";

const { mockTableNames, srcInfoFixture } = vi.hoisted(() => ({
	mockTableNames: vi.fn(),
	srcInfoFixture: {
		current: {
			srcType: "csv",
			srcPath: "src",
			setting: "setting.json",
		} as Partial<DatasetSrcInfo>,
	},
}));
vi.mock("../../../../../hooks/useDatasetSettings", () => ({
	useDatasetTableNames: (srcInfo: DatasetSrcInfo) => {
		mockTableNames(srcInfo);
		return { tableNames: ["renamed"], loading: false, logs: [] };
	},
	useDatasetTablePreview: () => ({ preview: null, loading: false, logs: [] }),
	useDatasetTableConvert: () => vi.fn(),
}));
vi.mock("../../../../../context/DatasetSrcInfoProvider", () => ({
	useDatasetSrcInfo: () => srcInfoFixture.current,
}));

function renderDialog() {
	const setting = new DatasetSetting({ name: ["big"], tableName: "renamed" });
	render(
		<DatasetSettingDialog
			setting={setting}
			handleDialogClose={vi.fn()}
			handleCommit={vi.fn()}
		/>,
	);
	return { user: userEvent.setup(), setting };
}

describe("DatasetSettingDialogのテスト", () => {
	it("Previewで編集中の設定だけを適用し、settingファイルなしでテーブル一覧をプレビューする", async () => {
		srcInfoFixture.current = {
			srcType: "csv",
			srcPath: "src",
			setting: "setting.json",
		};
		const { user, setting } = renderDialog();
		await user.click(screen.getByRole("button", { name: "Preview" }));

		expect(screen.getByText("Table List Preview")).toBeInTheDocument();
		expect(mockTableNames).toHaveBeenLastCalledWith(
			expect.objectContaining({
				srcType: "csv",
				srcPath: "src",
				setting: "",
				inlineSetting: setting,
			}),
		);
	});

	it("srcTypeが未指定ならPreviewボタンを出さない", () => {
		srcInfoFixture.current = { srcPath: "src" };
		renderDialog();

		expect(
			screen.queryByRole("button", { name: "Preview" }),
		).not.toBeInTheDocument();
	});
});
