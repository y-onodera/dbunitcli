import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import {
	CompareKeySettingDialog,
	CompareKeySettingsEditDialog,
} from "../../../../../app/form/section/dialog/CompareKeySettingsDialog";
import type { DatasetSrcInfo } from "../../../../../model/CommandOption";
import {
	DatasetSetting,
	DatasetSettings,
} from "../../../../../model/DatasetSettings";

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
		return { tableNames: ["users"], loading: false, logs: [] };
	},
	useDatasetTablePreview: () => ({ preview: null, loading: false, logs: [] }),
	useDatasetTableConvert: () => vi.fn(),
}));
vi.mock("../../../../../context/DatasetSrcInfoProvider", () => ({
	useDatasetSrcInfo: () => srcInfoFixture.current,
}));

// CompareFormではold側のデータセットの値が入る
const oldSrcInfo: DatasetSrcInfo = {
	values: {
		srcType: "csv",
		src: "old",
		setting: "old-setting.json",
		settingTargetOnly: "true",
	},
};

describe("CompareKeySettingDialogのテスト", () => {
	it("Targetとkeysだけを表示し、ほかのFilter / Order項目とPreviewは出さない", () => {
		srcInfoFixture.current = oldSrcInfo;
		render(
			<CompareKeySettingDialog
				setting={new DatasetSetting({ name: ["users"], keys: ["id"] })}
				handleDialogClose={vi.fn()}
				handleCommit={vi.fn()}
			/>,
		);

		expect(screen.getByText("Compare Key Setting")).toBeInTheDocument();
		expect(document.querySelector('select[name="target"]')).toBeInTheDocument();
		expect(
			document.querySelector<HTMLInputElement>('input[name="keys"]')?.value,
		).toBe("id");
		for (const name of ["exclude", "include", "filter", "order"]) {
			expect(
				document.querySelector(`input[name="${name}"]`),
			).not.toBeInTheDocument();
		}
		expect(
			document.querySelector('select[name="mode"]'),
		).not.toBeInTheDocument();
		expect(
			screen.queryByRole("button", { name: "Preview" }),
		).not.toBeInTheDocument();
	});

	it("テーブル名候補はold側のデータセットからsettingTargetOnlyで絞らずに取得する", () => {
		srcInfoFixture.current = oldSrcInfo;
		render(
			<CompareKeySettingDialog
				setting={new DatasetSetting({ name: ["users"] })}
				handleDialogClose={vi.fn()}
				handleCommit={vi.fn()}
			/>,
		);

		expect(mockTableNames).toHaveBeenCalledWith({
			values: {
				srcType: "csv",
				src: "old",
				setting: "old-setting.json",
				settingTargetOnly: "false",
			},
		});
	});

	it("keysを編集してSaveすると、keys以外の項目を残したまま反映する", async () => {
		srcInfoFixture.current = oldSrcInfo;
		const handleCommit = vi.fn();
		const user = userEvent.setup();
		render(
			<CompareKeySettingDialog
				setting={
					new DatasetSetting({
						name: ["users"],
						keys: ["id"],
						exclude: ["updated_at"],
					})
				}
				handleDialogClose={vi.fn()}
				handleCommit={handleCommit}
			/>,
		);

		const keys = document.querySelector<HTMLInputElement>('input[name="keys"]');
		if (!keys) {
			throw new Error("keys input not found");
		}
		await user.clear(keys);
		await user.type(keys, "user_id");
		await user.click(screen.getByRole("button", { name: "Save" }));

		const committed: DatasetSetting = handleCommit.mock.calls[0][0];
		expect(committed.keys).toEqual(["user_id"]);
		expect(committed.exclude).toEqual(["updated_at"]);
	});
});

describe("CompareKeySettingsEditDialogのテスト", () => {
	it("表の行に対象テーブルと比較キーを表示し、Save and Previewは出さない", () => {
		srcInfoFixture.current = oldSrcInfo;
		const settings = DatasetSettings.create().replaceSettings([
			new DatasetSetting({ name: ["users"], keys: ["id", "seq"] }),
		]);
		render(
			<CompareKeySettingsEditDialog
				settings={settings}
				fileName="compare.json"
				handleDialogClose={vi.fn()}
				handleSave={vi.fn()}
			/>,
		);

		expect(screen.getByText("Compare Key Settings")).toBeInTheDocument();
		expect(screen.getByText("Key Settings")).toBeInTheDocument();
		expect(screen.getByText("Common Key Settings")).toBeInTheDocument();
		expect(screen.getByText(/\[id, seq\]/)).toBeInTheDocument();
		expect(
			screen.queryByRole("button", { name: "Save and Preview" }),
		).not.toBeInTheDocument();
	});

	it("Saveで編集中の設定をファイルに保存し、保存先を返す", async () => {
		srcInfoFixture.current = oldSrcInfo;
		mockSave.mockResolvedValue("success");
		const settings = DatasetSettings.create();
		const handleSave = vi.fn();
		const user = userEvent.setup();
		render(
			<CompareKeySettingsEditDialog
				settings={settings}
				fileName="compare.json"
				handleDialogClose={vi.fn()}
				handleSave={handleSave}
			/>,
		);

		await user.click(screen.getByRole("button", { name: "Save" }));

		await waitFor(() => {
			expect(handleSave).toHaveBeenCalledWith("compare.json");
		});
		expect(mockSave).toHaveBeenCalledWith("compare.json", settings);
	});
});
