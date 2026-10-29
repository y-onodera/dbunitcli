import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import SqlEditorDialog from "../../../../../app/form/section/dialog/SqlEditorDialog";
import type { DatasetSrcInfo } from "../../../../../model/CommandOption";

// jsdomにはshowModalが未実装のためモック
HTMLDialogElement.prototype.showModal = vi.fn(function (
	this: HTMLDialogElement,
) {
	this.setAttribute("open", "");
});

// （mockReset:trueで実装が毎回リセットされるため、resolve値は各テスト内で設定する）
const { mockSave, mockTableNames } = vi.hoisted(() => ({
	mockSave: vi.fn(),
	mockTableNames: vi.fn(),
}));
vi.mock("../../../../../hooks/useResourceFiles", () => ({
	useResourceContent: () => ({ content: "select 1", loading: false }),
	useSaveResourceContent: () => mockSave,
}));
vi.mock("../../../../../hooks/useDatasetSettings", () => ({
	useDatasetTableNames: (srcInfo: DatasetSrcInfo) => {
		mockTableNames(srcInfo);
		return { tableNames: ["result"], loading: false, logs: [] };
	},
	useDatasetTablePreview: () => ({ preview: null, loading: false, logs: [] }),
	useDatasetTableConvert: () => vi.fn(),
}));
vi.mock("../../../../../context/DatasetSrcInfoProvider", () => ({
	useDatasetSrcInfo: () => ({
		srcType: "sql",
		srcPath: "old.sql",
		setting: "setting.json",
	}),
}));
vi.mock("../../../../../context/JdbcConnectionProvider", () => ({
	useJdbcConnectionState: () => ({ jdbcValues: {}, connectionOk: false }),
}));

function renderDialog() {
	const handleSave = vi.fn();
	render(
		<SqlEditorDialog
			type="sql"
			fileName="query.sql"
			handleDialogClose={vi.fn()}
			handleSave={handleSave}
		/>,
	);
	return { user: userEvent.setup(), handleSave };
}

describe("SqlEditorDialogのテスト", () => {
	it("Save and Previewで保存したファイルをsrcに、settingなしでテーブル一覧をプレビューする", async () => {
		mockSave.mockResolvedValue("success");
		const { user, handleSave } = renderDialog();
		await user.click(screen.getByRole("button", { name: "Save and Preview" }));

		await waitFor(() => {
			expect(screen.getByText("Table List Preview")).toBeInTheDocument();
		});
		expect(mockSave).toHaveBeenCalledWith("query.sql", "select 1");
		expect(mockTableNames).toHaveBeenLastCalledWith(
			expect.objectContaining({
				srcType: "sql",
				srcPath: "query.sql",
				setting: "",
			}),
		);
		expect(screen.getByText("result")).toBeInTheDocument();
		// 編集ダイアログは開いたまま（保存後に閉じるhandleSaveは呼ばない）
		expect(handleSave).not.toHaveBeenCalled();
	});

	it("保存に失敗したときはプレビューを開かない", async () => {
		mockSave.mockResolvedValue("failed");
		const { user } = renderDialog();
		await user.click(screen.getByRole("button", { name: "Save and Preview" }));

		await waitFor(() => {
			expect(mockSave).toHaveBeenCalled();
		});
		expect(screen.queryByText("Table List Preview")).not.toBeInTheDocument();
	});
});
