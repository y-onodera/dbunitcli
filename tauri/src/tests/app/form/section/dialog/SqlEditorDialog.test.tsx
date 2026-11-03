import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import SqlEditorDialog from "../../../../../app/form/section/dialog/SqlEditorDialog";
import type { DatasetSrcInfo } from "../../../../../model/CommandOption";

// （mockReset:trueで実装が毎回リセットされるため、resolve値は各テスト内で設定する）
const { mockSave, mockTableNames, jdbcState } = vi.hoisted(() => ({
	mockSave: vi.fn(),
	mockTableNames: vi.fn(),
	jdbcState: { connectionOk: false },
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
	useCsvColumnNamesFetcher: () => vi.fn(),
}));
vi.mock("../../../../../context/DatasetSrcInfoProvider", () => ({
	useDatasetSrcInfo: () => ({
		values: {
			srcType: "sql",
			src: "old.sql",
			setting: "setting.json",
			settingTargetOnly: "true",
		},
	}),
}));
vi.mock("../../../../../context/JdbcConnectionProvider", () => ({
	useJdbcConnectionState: () => ({
		jdbcValues: {},
		connectionOk: jdbcState.connectionOk,
	}),
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
		expect(mockTableNames).toHaveBeenLastCalledWith({
			values: {
				srcType: "sql",
				src: "query.sql",
				setting: "",
				settingTargetOnly: "true",
			},
		});
		expect(screen.getByText("result")).toBeInTheDocument();
		// 編集ダイアログは開いたまま（保存後に閉じるhandleSaveは呼ばない）
		expect(handleSave).not.toHaveBeenCalled();
	});

	it.each([
		{ type: "sql", connectionOk: true, expected: "Select Tables" },
		{ type: "sql", connectionOk: false, expected: null },
		// csvqはDBを読まないため、JDBCに接続できていてもSelect Tablesは出さない
		{ type: "csvq", connectionOk: true, expected: "Select CSV" },
	] as const)(
		"srcType=$type・JDBC接続$connectionOkのときのテーブル選択ボタンは$expected",
		({ type, connectionOk, expected }) => {
			jdbcState.connectionOk = connectionOk;
			render(
				<SqlEditorDialog
					type={type}
					fileName="query.sql"
					handleDialogClose={vi.fn()}
					handleSave={vi.fn()}
				/>,
			);
			const buttons = ["Select Tables", "Select CSV"].filter(
				(name) => screen.queryByRole("button", { name }) !== null,
			);
			jdbcState.connectionOk = false;
			expect(buttons).toStrictEqual(expected === null ? [] : [expected]);
		},
	);

	it("Select CSVで挿入した行をカーソル位置に入れる", async () => {
		const user = userEvent.setup();
		render(
			<SqlEditorDialog
				type="csvq"
				fileName="query.txt"
				handleDialogClose={vi.fn()}
				handleSave={vi.fn()}
			/>,
		);
		expect(screen.getByText("Edit SQL")).toBeInTheDocument();
		await user.click(screen.getByRole("button", { name: "Select CSV" }));
		expect(screen.getByText("Insert CSVREAD")).toBeInTheDocument();
		// フォームのencodingが未指定ならUTF-8
		expect(screen.getByLabelText("Charset*")).toHaveValue("UTF-8");
		await user.type(screen.getByLabelText("CSV file*"), "a.csv");
		await user.selectOptions(screen.getByLabelText("Usage"), "crossJoin");
		await user.type(screen.getByLabelText("Table alias*"), "A");
		await user.click(screen.getByRole("button", { name: "Insert" }));

		expect(screen.queryByText("Insert CSVREAD")).not.toBeInTheDocument();
		expect(
			(document.getElementById("contents") as HTMLTextAreaElement).value,
		).toContain(
			"    CROSS JOIN\n        CSVREAD('a.csv',null,'charset=UTF-8') A\n",
		);
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
