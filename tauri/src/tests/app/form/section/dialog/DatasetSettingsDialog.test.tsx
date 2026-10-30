import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { DatasetSettingsEditDialog } from "../../../../../app/form/section/dialog/DatasetSettingsDialog";
import type { DatasetSrcInfo } from "../../../../../model/CommandOption";
import { DatasetSettings } from "../../../../../model/DatasetSettings";

// （mockReset:trueで実装が毎回リセットされるため、resolve値は各テスト内で設定する）
const { mockSave, mockTableNames, srcInfoFixture } = vi.hoisted(() => ({
	mockSave: vi.fn(),
	mockTableNames: vi.fn(),
	srcInfoFixture: { current: {} as DatasetSrcInfo },
}));
vi.mock("../../../../../hooks/useDatasetSettings", () => ({
	useSaveDatasetSettings: () => mockSave,
	useDatasetTableNames: (srcInfo: DatasetSrcInfo) => {
		mockTableNames(srcInfo);
		return { tableNames: ["result"], loading: false, logs: [] };
	},
	useDatasetTablePreview: () => ({ preview: null, loading: false, logs: [] }),
	useDatasetTableConvert: () => vi.fn(),
}));
vi.mock("../../../../../context/DatasetSrcInfoProvider", () => ({
	useDatasetSrcInfo: () => srcInfoFixture.current,
}));

const baseSrcInfo: DatasetSrcInfo = {
	values: {
		srcType: "csv",
		src: "src",
		setting: "old.json",
		settingTargetOnly: "true",
	},
};

function renderDialog() {
	const settings = DatasetSettings.create();
	const handleSave = vi.fn();
	render(
		<DatasetSettingsEditDialog
			settings={settings}
			fileName="setting.json"
			handleDialogClose={vi.fn()}
			handleSave={handleSave}
		/>,
	);
	return { user: userEvent.setup(), settings, handleSave };
}

describe("DatasetSettingsDialogのテスト", () => {
	it("Save and Previewで保存したファイルをsettingに適用してテーブル一覧をプレビューする", async () => {
		srcInfoFixture.current = baseSrcInfo;
		mockSave.mockResolvedValue("success");
		const { user, settings, handleSave } = renderDialog();
		await user.click(screen.getByRole("button", { name: "Save and Preview" }));

		await waitFor(() => {
			expect(screen.getByText("Table List Preview")).toBeInTheDocument();
		});
		expect(mockSave).toHaveBeenCalledWith("setting.json", settings);
		expect(mockTableNames).toHaveBeenLastCalledWith({
			values: {
				srcType: "csv",
				src: "src",
				setting: "setting.json",
				settingTargetOnly: "true",
			},
		});
		expect(screen.getByText("result")).toBeInTheDocument();
		// 編集ダイアログは開いたまま（保存後に閉じるhandleSaveは呼ばない）
		expect(handleSave).not.toHaveBeenCalled();
	});

	it("保存に失敗したときはプレビューを開かない", async () => {
		srcInfoFixture.current = baseSrcInfo;
		mockSave.mockResolvedValue("failed");
		const { user } = renderDialog();
		await user.click(screen.getByRole("button", { name: "Save and Preview" }));

		await waitFor(() => {
			expect(mockSave).toHaveBeenCalled();
		});
		expect(screen.queryByText("Table List Preview")).not.toBeInTheDocument();
	});

	it("srcTypeが未指定ならSave and Previewボタンを出さない", () => {
		srcInfoFixture.current = { values: { src: "src" } };
		renderDialog();

		expect(
			screen.queryByRole("button", { name: "Save and Preview" }),
		).not.toBeInTheDocument();
	});
});
