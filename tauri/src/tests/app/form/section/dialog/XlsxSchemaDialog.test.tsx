import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { XlsxSchemaEditDialog } from "../../../../../app/form/section/dialog/XlsxSchemaDialog";
import type { DatasetSrcInfo } from "../../../../../model/CommandOption";
import { XlsxSchema } from "../../../../../model/XlsxSchema";

// （mockReset:trueで実装が毎回リセットされるため、resolve値は各テスト内で設定する）
const { mockSave, mockTableNames, srcInfoFixture } = vi.hoisted(() => ({
	mockSave: vi.fn(),
	mockTableNames: vi.fn(),
	srcInfoFixture: { current: {} as DatasetSrcInfo },
}));
vi.mock("../../../../../hooks/useXlsxSchema", () => ({
	useSaveXlsxSchema: () => mockSave,
	useXlsxSchemaData: () => ({ schema: XlsxSchema.create(), loading: false }),
}));
vi.mock("../../../../../hooks/useDatasetSettings", () => ({
	useDatasetTableNames: (srcInfo: DatasetSrcInfo) => {
		mockTableNames(srcInfo);
		return { tableNames: ["sheet1"], loading: false, logs: [] };
	},
	useDatasetTablePreview: () => ({ preview: null, loading: false, logs: [] }),
	useDatasetTableConvert: () => vi.fn(),
}));
vi.mock("../../../../../context/DatasetSrcInfoProvider", () => ({
	useDatasetSrcInfo: () => srcInfoFixture.current,
}));

const baseSrcInfo: DatasetSrcInfo = {
	values: {
		srcType: "xlsx",
		src: "src",
		xlsxSchema: "old.json",
	},
};

function renderDialog() {
	const schema = XlsxSchema.create();
	const handleSave = vi.fn();
	render(
		<XlsxSchemaEditDialog
			schema={schema}
			fileName="schema.json"
			handleDialogClose={vi.fn()}
			handleSave={handleSave}
		/>,
	);
	return { user: userEvent.setup(), schema, handleSave };
}

describe("XlsxSchemaDialogのテスト", () => {
	it("Save and Previewで保存したファイルをxlsxSchemaに適用してテーブル一覧をプレビューする", async () => {
		srcInfoFixture.current = baseSrcInfo;
		mockSave.mockResolvedValue("success");
		const { user, schema, handleSave } = renderDialog();
		await user.click(screen.getByRole("button", { name: "Save and Preview" }));

		await waitFor(() => {
			expect(screen.getByText("Table List Preview")).toBeInTheDocument();
		});
		expect(mockSave).toHaveBeenCalledWith("schema.json", schema);
		expect(mockTableNames).toHaveBeenLastCalledWith({
			values: {
				srcType: "xlsx",
				src: "src",
				xlsxSchema: "schema.json",
			},
		});
		expect(screen.getByText("sheet1")).toBeInTheDocument();
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
