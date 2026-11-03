import { render, screen } from "@testing-library/react";
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
	it("Previewは保存せず、編集中の内容をsrcの代わりに、settingなしでテーブル一覧をプレビューする", async () => {
		const { user, handleSave } = renderDialog();
		await user.type(
			screen.getByPlaceholderText("Enter SQL query..."),
			" from dual",
		);
		await user.click(screen.getByRole("button", { name: "Preview" }));

		expect(screen.getByText("Table List Preview")).toBeInTheDocument();
		expect(mockTableNames).toHaveBeenLastCalledWith({
			values: {
				srcType: "sql",
				src: "old.sql",
				setting: "",
				settingTargetOnly: "true",
			},
			inlineQuery: "select 1 from dual",
		});
		expect(screen.getByText("result")).toBeInTheDocument();
		expect(mockSave).not.toHaveBeenCalled();
		expect(handleSave).not.toHaveBeenCalled();
		expect(
			screen.queryByRole("button", { name: "Save and Preview" }),
		).not.toBeInTheDocument();
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
});
