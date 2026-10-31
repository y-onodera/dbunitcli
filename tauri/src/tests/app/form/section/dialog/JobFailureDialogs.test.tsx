import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import ColumnDatalistDialog from "../../../../../app/form/section/dialog/ColumnDatalistDialog";
import { RefreshableDialogShell } from "../../../../../app/form/section/dialog/RefreshableDialogShell";
import SqlTableInsertDialog from "../../../../../app/form/section/dialog/SqlTableInsertDialog";
import type { JdbcFetchOptions } from "../../../../../hooks/useJdbc";
import { DatasetSetting } from "../../../../../model/DatasetSettings";
import {
	emitLogsThenFail,
	expectJobFailure,
	JOB_ERROR,
	JOB_LOG_LINES,
} from "../../../../helpers/jobFailure";

// （mockReset:trueで実装が毎回リセットされるため、実装は各テスト内で設定する）
const { mockJdbcTables, mockJdbcColumns, mockFetchColumnNames } = vi.hoisted(
	() => ({
		mockJdbcTables: vi.fn(),
		mockJdbcColumns: vi.fn(),
		mockFetchColumnNames: vi.fn(),
	}),
);
vi.mock("../../../../../hooks/useJdbc", () => ({
	useJdbcTables: () => mockJdbcTables,
	useJdbcColumns: () => mockJdbcColumns,
}));
vi.mock("../../../../../hooks/useDatasetSettings", () => ({
	useColumnNamesFetcher: () => mockFetchColumnNames,
}));

describe("RefreshableDialogShellのExecute失敗", () => {
	it("エラーと実行中のログを残す", () => {
		render(
			<RefreshableDialogShell
				command="generate"
				label="Generate"
				title="Generate"
				formId="testForm"
				loadState={{ status: "loaded", options: {} }}
				executing={false}
				executeError={JOB_ERROR}
				logs={JOB_LOG_LINES}
				onExecute={vi.fn()}
				onClose={vi.fn()}
			>
				{() => null}
			</RefreshableDialogShell>,
		);
		expectJobFailure();
	});
});

describe("SqlTableInsertDialogの読込失敗", () => {
	it("テーブル一覧の読込に失敗したら、エラーと読込中のログを残す", async () => {
		mockJdbcTables.mockImplementation(
			(_jdbcValues, fetchOptions: JdbcFetchOptions) =>
				emitLogsThenFail(fetchOptions.onLog),
		);
		render(
			<SqlTableInsertDialog
				jdbcValues={{}}
				onInsert={vi.fn()}
				onInsertColumn={vi.fn()}
				onClose={vi.fn()}
			/>,
		);
		await userEvent.click(screen.getByRole("button", { name: "Load Tables" }));

		expectJobFailure();
		expect(screen.queryByText("No tables found")).not.toBeInTheDocument();
	});

	it("列の読込に失敗したら、列選択ダイアログにエラーと読込中のログを残す", async () => {
		mockJdbcTables.mockResolvedValue(["T1"]);
		mockJdbcColumns.mockImplementation(
			(_jdbcValues, _table, fetchOptions: JdbcFetchOptions) =>
				emitLogsThenFail(fetchOptions.onLog),
		);
		render(
			<SqlTableInsertDialog
				jdbcValues={{}}
				onInsert={vi.fn()}
				onInsertColumn={vi.fn()}
				onClose={vi.fn()}
			/>,
		);
		await userEvent.click(screen.getByRole("button", { name: "Load Tables" }));
		await userEvent.click(await screen.findByTitle("Select columns"));

		expectJobFailure();
		expect(screen.getByText("Select Columns")).toBeInTheDocument();
	});
});

describe("ColumnDatalistDialogの読込失敗", () => {
	it("列の読込に失敗したら、閉じずにエラーと読込中のログを残す", async () => {
		mockFetchColumnNames.mockImplementation(
			(_srcInfo, _tableName, _signal, onLog: (line: string) => void) =>
				emitLogsThenFail(onLog),
		);
		const onLoad = vi.fn();
		const onClose = vi.fn();
		render(
			<ColumnDatalistDialog
				tableNames={["T1"]}
				target={new DatasetSetting({})}
				onLoad={onLoad}
				onClose={onClose}
			/>,
		);
		await userEvent.click(screen.getByRole("button", { name: "Load Columns" }));

		expectJobFailure();
		expect(onLoad).not.toHaveBeenCalled();
		expect(onClose).not.toHaveBeenCalled();
		// 別のテーブルを選び直せるよう一覧は残す
		expect(
			screen.getByRole("button", { name: "Load Columns" }),
		).toBeInTheDocument();
	});
});
