import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import SqlEditorDialog from "../../../../../app/form/section/dialog/SqlEditorDialog";
import type { DatasetSrcInfo } from "../../../../../model/CommandOption";

// （mockReset:trueで実装が毎回リセットされるため、resolve値は各テスト内で設定する）
const { mockSave, mockTableNames, mockTablePreview, jdbcState, resourceKinds } =
	vi.hoisted(() => ({
		mockSave: vi.fn(),
		mockTableNames: vi.fn(),
		mockTablePreview: vi.fn(),
		jdbcState: { connectionOk: false },
		// 読込・保存のフックに渡されたリソースの種類
		resourceKinds: { load: [] as string[], save: [] as string[] },
	}));
vi.mock("../../../../../hooks/useResourceFiles", () => ({
	CONTENT_PARSER: {},
	useResourceLoad: (kind: string) => {
		resourceKinds.load.push(kind);
		return { data: "select 1", location: null, loading: false };
	},
	useSaveResourceContent: (kind: string) => {
		resourceKinds.save.push(kind);
		return mockSave;
	},
}));
vi.mock("../../../../../hooks/useDatasetSettings", () => ({
	useDatasetTableNames: (srcInfo: DatasetSrcInfo) => {
		mockTableNames(srcInfo);
		return { tableNames: ["result"], loading: false, logs: [] };
	},
	useDatasetTablePreview: (srcInfo: DatasetSrcInfo, tableName: string) => {
		mockTablePreview(srcInfo, tableName);
		return { preview: null, loading: false, logs: [], error: "" };
	},
	useDatasetTableConvert: () => vi.fn(),
	useCsvColumnNamesFetcher: () => vi.fn(),
	INLINE_SOURCE_TABLE_NAME: "inlineSource",
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

function renderDialog(type: "sql" | "table" | "csvq" = "sql") {
	const handleSave = vi.fn();
	render(
		<SqlEditorDialog
			type={type}
			fileName="query.sql"
			handleDialogClose={vi.fn()}
			handleSave={handleSave}
		/>,
	);
	return { user: userEvent.setup(), handleSave };
}

const PREVIEW_SRC_VALUES = {
	srcType: "sql",
	src: "old.sql",
	setting: "",
	settingTargetOnly: "true",
};

describe("SqlEditorDialogのテスト", () => {
	it.each(["sql", "table", "csvq"] as const)(
		"srcType=%sの種類のリソースとして読み込み・保存することを確認",
		(type) => {
			resourceKinds.load.length = 0;
			resourceKinds.save.length = 0;
			renderDialog(type);
			expect(new Set(resourceKinds.load)).toStrictEqual(new Set([type]));
			expect(new Set(resourceKinds.save)).toStrictEqual(new Set([type]));
		},
	);

	it.each(["sql", "csvq"] as const)(
		"srcType=%sのPreviewは保存せず、編集中の内容をsrcの代わりに、settingなしで一覧を挟まずにそのテーブルをプレビューする",
		async (type) => {
			const { user, handleSave } = renderDialog(type);
			await user.type(
				screen.getByPlaceholderText("Enter SQL query..."),
				" from dual",
			);
			await user.click(screen.getByRole("button", { name: "Preview" }));

			expect(screen.queryByText("Table List Preview")).not.toBeInTheDocument();
			expect(mockTableNames).not.toHaveBeenCalled();
			expect(
				screen.getByText("inlineSource — Data Preview"),
			).toBeInTheDocument();
			expect(mockTablePreview).toHaveBeenLastCalledWith(
				{ values: PREVIEW_SRC_VALUES, inlineSource: "select 1 from dual" },
				"inlineSource",
			);
			expect(mockSave).not.toHaveBeenCalled();
			expect(handleSave).not.toHaveBeenCalled();
			expect(
				screen.queryByRole("button", { name: "Save and Preview" }),
			).not.toBeInTheDocument();
		},
	);

	it("srcType=tableのPreviewは編集中の内容をsrcの代わりに、settingなしでテーブル一覧をプレビューする", async () => {
		const { user } = renderDialog("table");
		await user.type(
			screen.getByPlaceholderText("Enter table definition..."),
			"\nt2",
		);
		await user.click(screen.getByRole("button", { name: "Preview" }));

		expect(screen.getByText("Table List Preview")).toBeInTheDocument();
		expect(mockTableNames).toHaveBeenLastCalledWith({
			values: PREVIEW_SRC_VALUES,
			inlineSource: "select 1\nt2",
		});
		expect(screen.getByText("result")).toBeInTheDocument();
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
