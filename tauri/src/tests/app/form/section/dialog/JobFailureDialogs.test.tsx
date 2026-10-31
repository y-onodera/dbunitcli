import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import ColumnDatalistDialog from "../../../../../app/form/section/dialog/ColumnDatalistDialog";
import { RefreshableDialogShell } from "../../../../../app/form/section/dialog/RefreshableDialogShell";
import SqlTableInsertDialog from "../../../../../app/form/section/dialog/SqlTableInsertDialog";
import type { JdbcFetchOptions } from "../../../../../hooks/useJdbc";
import { DatasetSetting } from "../../../../../model/DatasetSettings";

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

// ログを2行流してから失敗する取得処理
const failWithLogs =
	(onLogOf: (args: unknown[]) => ((line: string) => void) | undefined) =>
	async (...args: unknown[]) => {
		const onLog = onLogOf(args);
		onLog?.("step1");
		onLog?.("step2");
		throw new Error("boom");
	};

const expectFailureWithLogs = () => {
	expect(screen.getByText("Error: boom")).toBeInTheDocument();
	expect(screen.getByRole("log", { name: "execution log" })).toHaveTextContent(
		"step1 step2",
	);
};

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
				executeError="boom"
				logs={["step1", "step2"]}
				onExecute={vi.fn()}
				onClose={vi.fn()}
			>
				{() => null}
			</RefreshableDialogShell>,
		);
		expectFailureWithLogs();
	});
});

describe("SqlTableInsertDialogの読込失敗", () => {
	it("テーブル一覧の読込に失敗したら、エラーと読込中のログを残す", async () => {
		mockJdbcTables.mockImplementation(
			failWithLogs((args) => (args[1] as JdbcFetchOptions).onLog),
		);
		render(
			<SqlTableInsertDialog
				jdbcValues={{}}
				onInsert={vi.fn()}
				onClose={vi.fn()}
			/>,
		);
		await userEvent.click(screen.getByRole("button", { name: "Load Tables" }));

		expectFailureWithLogs();
		expect(screen.queryByText("No tables found")).not.toBeInTheDocument();
	});

	it("列の読込に失敗したら、列選択ダイアログにエラーと読込中のログを残す", async () => {
		mockJdbcTables.mockResolvedValue(["T1"]);
		mockJdbcColumns.mockImplementation(
			failWithLogs((args) => (args[2] as JdbcFetchOptions).onLog),
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

		expectFailureWithLogs();
		expect(screen.getByText("Select Columns")).toBeInTheDocument();
	});
});

describe("ColumnDatalistDialogの読込失敗", () => {
	it("列の読込に失敗したら、閉じずにエラーと読込中のログを残す", async () => {
		mockFetchColumnNames.mockImplementation(
			failWithLogs((args) => args[3] as (line: string) => void),
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

		expectFailureWithLogs();
		expect(onLoad).not.toHaveBeenCalled();
		expect(onClose).not.toHaveBeenCalled();
		// 別のテーブルを選び直せるよう一覧は残す
		expect(
			screen.getByRole("button", { name: "Load Columns" }),
		).toBeInTheDocument();
	});
});
