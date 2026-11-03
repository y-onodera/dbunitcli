import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import SqlTableInsertDialog from "../../../../../app/form/section/dialog/SqlTableInsertDialog";

// （mockReset:trueで実装が毎回リセットされるため、resolve値は各テスト内で設定する）
const { mockJdbcTables, mockJdbcColumns } = vi.hoisted(() => ({
	mockJdbcTables: vi.fn(),
	mockJdbcColumns: vi.fn(),
}));
vi.mock("../../../../../hooks/useJdbc", () => ({
	useJdbcTables: () => mockJdbcTables,
	useJdbcColumns: () => mockJdbcColumns,
}));

async function renderLoaded(plain = false) {
	mockJdbcTables.mockResolvedValue(["USER_ACCOUNT", "ORDERS"]);
	mockJdbcColumns.mockResolvedValue(["ID", "NAME"]);
	const onInsert = vi.fn();
	const onInsertColumn = vi.fn();
	const user = userEvent.setup();
	render(
		<SqlTableInsertDialog
			jdbcValues={{}}
			onInsert={onInsert}
			onInsertColumn={onInsertColumn}
			plain={plain}
			onClose={vi.fn()}
		/>,
	);
	await user.click(screen.getByRole("button", { name: "Load Tables" }));
	await screen.findByText("USER_ACCOUNT");
	return { user, onInsert, onInsertColumn };
}

describe("SqlTableInsertDialogのテーブル挿入", () => {
	it("選んだテーブルの接頭字が既定のエイリアスになり、用途のキーワード付きで挿入する", async () => {
		const { user, onInsert } = await renderLoaded();
		await user.click(screen.getByText("USER_ACCOUNT"));
		expect(screen.getByLabelText("Table alias")).toHaveValue("ua");
		expect(screen.getByTestId("insert-preview")).toHaveTextContent(
			"from USER_ACCOUNT ua",
		);

		await user.selectOptions(screen.getByLabelText("Usage"), "delete");
		await user.click(screen.getByRole("button", { name: "Insert" }));
		expect(onInsert).toHaveBeenCalledWith(["delete from USER_ACCOUNT ua"]);
	});

	it("1テーブルだけ選べ、別のテーブルを選ぶと置き換わる", async () => {
		const { user, onInsert } = await renderLoaded();
		await user.click(screen.getByText("USER_ACCOUNT"));
		await user.click(screen.getByText("ORDERS"));
		expect(screen.getByLabelText("Table alias")).toHaveValue("o");

		await user.selectOptions(screen.getByLabelText("Usage"), "crossJoin");
		await user.click(screen.getByRole("button", { name: "Insert" }));
		expect(onInsert).toHaveBeenCalledWith(["cross join ORDERS o"]);
	});

	it("joinではエイリアスが空なら挿入できない", async () => {
		const { user, onInsert } = await renderLoaded();
		await user.click(screen.getByText("ORDERS"));
		await user.selectOptions(screen.getByLabelText("Usage"), "leftJoin");
		await user.clear(screen.getByLabelText("Table alias*"));
		expect(screen.getByRole("button", { name: "Insert" })).toBeDisabled();

		await user.type(screen.getByLabelText("Table alias*"), "od");
		await user.click(screen.getByRole("button", { name: "Insert" }));
		expect(onInsert).toHaveBeenCalledWith(["left join ORDERS od on"]);
	});

	it("selectでは続けて選んだカラムと合わせ、文全体を挿入する", async () => {
		const { user, onInsert, onInsertColumn } = await renderLoaded();
		await user.click(screen.getByText("USER_ACCOUNT"));
		await user.click(screen.getByRole("button", { name: "Next" }));
		await screen.findByText("Select Columns");
		// 用途はテーブル側に合わせて固定し、1行目の区切りの指定も出さない
		expect(screen.getAllByLabelText("Usage")).toHaveLength(1);
		expect(screen.queryByText(/Separator on first line/)).toBeNull();
		await user.click(screen.getByText("NAME"));
		await user.click(screen.getByLabelText("Prefix table alias"));

		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsert).toHaveBeenCalledWith([
			"select",
			"ua.NAME",
			"from USER_ACCOUNT ua",
		]);
		expect(onInsertColumn).not.toHaveBeenCalled();
	});

	it("insertではエイリアスを入力できず、All Columnsで全カラムを書き出す", async () => {
		const { user, onInsert } = await renderLoaded();
		await user.click(screen.getByText("ORDERS"));
		await user.selectOptions(screen.getByLabelText("Usage"), "insert");
		expect(screen.getByLabelText("Table alias")).toBeDisabled();
		await user.click(screen.getByRole("button", { name: "Next" }));
		await screen.findByText("Select Columns");

		await user.click(screen.getByRole("button", { name: "All Columns" }));
		expect(onInsert).toHaveBeenCalledWith([
			"insert into ORDERS (",
			"ID",
			", NAME",
			") values (",
			")",
		]);
	});

	it("updateでは set に続けてカラムを = 付きで挿入する", async () => {
		const { user, onInsert } = await renderLoaded();
		await user.click(screen.getByText("ORDERS"));
		await user.selectOptions(screen.getByLabelText("Usage"), "update");
		await user.click(screen.getByRole("button", { name: "Next" }));
		await screen.findByText("Select Columns");

		await user.click(screen.getByRole("button", { name: "All Columns" }));
		expect(onInsert).toHaveBeenCalledWith([
			"update ORDERS o set",
			"ID = ",
			", NAME = ",
		]);
	});

	it("plainでは用途を出さず、複数テーブルを名前だけで挿入する", async () => {
		const { user, onInsert } = await renderLoaded(true);
		expect(screen.queryByLabelText("Usage")).toBeNull();
		await user.click(screen.getByText("USER_ACCOUNT"));
		await user.click(screen.getByText("ORDERS"));

		await user.click(screen.getByRole("button", { name: "Insert" }));
		expect(onInsert).toHaveBeenCalledWith(["USER_ACCOUNT", "ORDERS"]);
	});
});

describe("SqlTableInsertDialogのカラム挿入", () => {
	it("テーブル側で入力したエイリアスで修飾し、selectでは as を付けられる", async () => {
		const { user, onInsertColumn } = await renderLoaded();
		await user.click(screen.getByText("USER_ACCOUNT"));
		await user.clear(screen.getByLabelText("Table alias"));
		await user.type(screen.getByLabelText("Table alias"), "acc");
		await user.click(screen.getAllByTitle("Select columns")[0]);
		await screen.findByText("Select Columns");
		await user.click(screen.getByText("ID"));
		await user.click(screen.getByText("NAME"));
		await user.click(screen.getByLabelText("Prefix table alias"));
		await user.click(screen.getByLabelText("as column name"));

		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith([
			"acc.ID as ID",
			", acc.NAME as NAME",
		]);
	});

	it("where/onでは = を付けて and でつなぎ、修飾しなければ列名のまま", async () => {
		const { user, onInsertColumn } = await renderLoaded();
		await user.click(screen.getAllByTitle("Select columns")[1]);
		await screen.findByText("Select Columns");
		await user.click(screen.getByText("ID"));
		await user.click(screen.getByText("NAME"));
		await user.selectOptions(screen.getAllByLabelText("Usage")[1], "condition");
		expect(screen.queryByLabelText("as column name")).toBeNull();

		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith(["ID = ", "and NAME = "]);
	});

	it("All Columnsは選択に関わらず全カラムを書き出す", async () => {
		const { user, onInsertColumn } = await renderLoaded();
		await user.click(screen.getAllByTitle("Select columns")[1]);
		await screen.findByText("Select Columns");
		await user.click(screen.getByText("ID"));

		await user.click(screen.getByRole("button", { name: "All Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith(["ID", ", NAME"]);
	});

	it("insertでは修飾の指定を出さない", async () => {
		const { user, onInsertColumn } = await renderLoaded();
		await user.click(screen.getAllByTitle("Select columns")[1]);
		await screen.findByText("Select Columns");
		await user.click(screen.getByText("ID"));
		await user.selectOptions(screen.getAllByLabelText("Usage")[1], "insert");
		expect(screen.queryByLabelText("Prefix table alias")).toBeNull();

		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith(["ID"]);
	});

	it("Separator on first line をONにすると1行目にも区切りを付ける", async () => {
		const { user, onInsertColumn } = await renderLoaded();
		await user.click(screen.getAllByTitle("Select columns")[1]);
		await screen.findByText("Select Columns");
		await user.click(screen.getByText("ID"));
		await user.selectOptions(screen.getAllByLabelText("Usage")[1], "condition");
		await user.click(screen.getByLabelText("Separator on first line (and)"));

		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith(["and ID = "]);
	});
});
