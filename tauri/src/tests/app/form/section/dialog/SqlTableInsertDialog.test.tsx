import { render, screen, within } from "@testing-library/react";
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
		expect(screen.getByLabelText("Table alias")).toHaveValue("UA");

		await user.selectOptions(screen.getByLabelText("Usage"), "delete");
		expect(screen.getByTestId("insert-preview")).toHaveTextContent(
			"DELETE FROM USER_ACCOUNT UA",
		);
		await user.click(screen.getByRole("button", { name: "Insert" }));
		expect(onInsert).toHaveBeenCalledWith([
			"DELETE FROM",
			"    USER_ACCOUNT UA",
		]);
	});

	it("1テーブルだけ選べ、別のテーブルを選ぶと置き換わる", async () => {
		const { user, onInsert } = await renderLoaded();
		await user.click(screen.getByText("USER_ACCOUNT"));
		await user.click(screen.getByText("ORDERS"));
		expect(screen.getByLabelText("Table alias")).toHaveValue("O");

		await user.selectOptions(screen.getByLabelText("Usage"), "crossJoin");
		await user.click(screen.getByRole("button", { name: "Insert" }));
		expect(onInsert).toHaveBeenCalledWith([
			"    CROSS JOIN",
			"        ORDERS O",
		]);
	});

	it("joinではエイリアスが空なら進めず、続けて選んだカラムでONを組み立てる", async () => {
		const { user, onInsert } = await renderLoaded();
		await user.click(screen.getByText("ORDERS"));
		await user.selectOptions(screen.getByLabelText("Usage"), "innerJoin");
		await user.clear(screen.getByLabelText("Table alias*"));
		expect(screen.getByRole("button", { name: "Next" })).toBeDisabled();

		await user.type(screen.getByLabelText("Table alias*"), "O2");
		await user.click(screen.getByRole("button", { name: "Next" }));
		await screen.findByText("Select Columns");
		expect(screen.getByLabelText("Prefix table alias (O2)")).toBeChecked();
		await user.click(screen.getByRole("button", { name: "All Columns" }));
		expect(onInsert).toHaveBeenCalledWith([
			"    INNER JOIN",
			"        ORDERS O2",
			"          ON  O2.ID = ",
			"          AND O2.NAME = ",
		]);
	});

	it("selectでは続けて選んだカラムと合わせ、文全体を挿入する", async () => {
		const { user, onInsert, onInsertColumn } = await renderLoaded();
		await user.click(screen.getByText("USER_ACCOUNT"));
		await user.click(screen.getByRole("button", { name: "Next" }));
		await screen.findByText("Select Columns");
		// 用途はテーブル側に合わせて固定し、キーワード行・1行目の区切りの指定も出さない
		expect(screen.getAllByLabelText("Usage")).toHaveLength(1);
		expect(screen.queryByText(/Separator on first line/)).toBeNull();
		expect(screen.queryByText(/Keyword line/)).toBeNull();
		await user.click(screen.getByText("ID"));
		await user.click(screen.getByText("NAME"));
		expect(screen.getByLabelText("Prefix table alias (UA)")).toBeChecked();
		await user.click(screen.getByLabelText("AS column name"));

		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsert).toHaveBeenCalledWith([
			"SELECT",
			"    UA.ID AS ID",
			"  , UA.NAME AS NAME",
			"FROM",
			"    USER_ACCOUNT UA",
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
			"INSERT INTO",
			"    ORDERS",
			"(",
			"    ID",
			"  , NAME",
			")",
			"VALUES",
			"(",
			")",
		]);
	});

	it("updateでは SET に続けてカラムを = 付きで挿入する", async () => {
		const { user, onInsert } = await renderLoaded();
		await user.click(screen.getByText("ORDERS"));
		await user.selectOptions(screen.getByLabelText("Usage"), "update");
		await user.click(screen.getByRole("button", { name: "Next" }));
		await screen.findByText("Select Columns");

		await user.click(screen.getByRole("button", { name: "All Columns" }));
		expect(onInsert).toHaveBeenCalledWith([
			"UPDATE",
			"    ORDERS O",
			"SET",
			"    O.ID = ",
			"  , O.NAME = ",
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
	async function openColumnsOfOrders() {
		const loaded = await renderLoaded();
		await loaded.user.click(screen.getAllByTitle("Select columns")[1]);
		await screen.findByText("Select Columns");
		return loaded;
	}

	it("テーブル側で入力したエイリアスで修飾する", async () => {
		const { user, onInsertColumn } = await renderLoaded();
		await user.click(screen.getByText("USER_ACCOUNT"));
		await user.clear(screen.getByLabelText("Table alias"));
		await user.type(screen.getByLabelText("Table alias"), "ACC");
		await user.click(screen.getAllByTitle("Select columns")[0]);
		await screen.findByText("Select Columns");
		await user.click(screen.getByText("ID"));
		await user.click(screen.getByText("NAME"));
		expect(screen.getByLabelText("Prefix table alias (ACC)")).toBeChecked();

		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith(["    ACC.ID", "  , ACC.NAME"]);
	});

	it("WHEREはキーワード行を付け、= を付けて AND でつなぐ", async () => {
		const { user, onInsertColumn } = await openColumnsOfOrders();
		await user.click(screen.getByText("ID"));
		await user.click(screen.getByText("NAME"));
		await user.selectOptions(screen.getAllByLabelText("Usage")[1], "where");
		expect(screen.queryByLabelText("AS column name")).toBeNull();

		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith([
			"WHERE",
			"    ID = ",
			"AND NAME = ",
		]);
	});

	it("Keyword line をOFFにするとキーワード行を付けない", async () => {
		const { user, onInsertColumn } = await openColumnsOfOrders();
		await user.click(screen.getByText("ID"));
		await user.selectOptions(screen.getAllByLabelText("Usage")[1], "orderBy");
		await user.click(screen.getByLabelText("Keyword line (ORDER BY)"));

		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith(["    ID"]);
	});

	it("選択中でないテーブルは別名のデフォルトを表示し、修飾は最初OFF", async () => {
		const { user, onInsertColumn } = await openColumnsOfOrders();
		await user.click(screen.getByText("ID"));
		const qualify = screen.getByLabelText("Prefix table alias (O)");
		expect(qualify).not.toBeChecked();
		await user.click(qualify);

		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith(["    O.ID"]);
	});

	it("テーブル側の別名を空にすると修飾の指定を出さない", async () => {
		const { user, onInsert } = await renderLoaded();
		await user.click(screen.getByText("ORDERS"));
		await user.clear(screen.getByLabelText("Table alias"));
		await user.click(screen.getByRole("button", { name: "Next" }));
		await screen.findByText("Select Columns");
		expect(screen.queryByLabelText(/Prefix table alias/)).toBeNull();

		await user.click(screen.getByRole("button", { name: "All Columns" }));
		expect(onInsert).toHaveBeenCalledWith([
			"SELECT",
			"    ID",
			"  , NAME",
			"FROM",
			"    ORDERS",
		]);
	});

	it("同じテーブルの列は開き直しても問い合わせ直さない", async () => {
		const { user } = await openColumnsOfOrders();
		const columnDialog = await findDialog("Select Columns");
		await user.click(columnDialog.getByRole("button", { name: "Cancel" }));
		await user.click(screen.getAllByTitle("Select columns")[1]);
		await screen.findByText("Select Columns");
		expect(mockJdbcColumns).toHaveBeenCalledTimes(1);
	});

	it("All Columnsは選択に関わらず全カラムを書き出す", async () => {
		const { user, onInsertColumn } = await openColumnsOfOrders();
		await user.click(screen.getByText("ID"));

		await user.click(screen.getByRole("button", { name: "All Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith(["    ID", "  , NAME"]);
	});

	it("insertでは修飾の指定を出さない", async () => {
		const { user, onInsertColumn } = await openColumnsOfOrders();
		await user.click(screen.getByText("ID"));
		await user.selectOptions(screen.getAllByLabelText("Usage")[1], "insert");
		expect(screen.queryByLabelText(/Prefix table alias/)).toBeNull();

		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith(["    ID"]);
	});

	it("Order by では Separator on first line で1行目にも区切りを付け、キーワード行は付けない", async () => {
		const { user, onInsertColumn } = await openColumnsOfOrders();
		await user.click(screen.getByText("ID"));
		await user.selectOptions(screen.getAllByLabelText("Usage")[1], "orderBy");
		await user.click(screen.getByLabelText("Separator on first line (,)"));
		expect(screen.getByLabelText("Keyword line (ORDER BY)")).toBeDisabled();

		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith(["  , ID"]);
	});
});

// 入れ子のダイアログは見出しの親（ダイアログ本体）の中だけを探す
async function findDialog(title: string) {
	const heading = await screen.findByRole("heading", { name: title });
	return within(heading.parentElement as HTMLElement);
}

describe("SqlTableInsertDialogの条件の挿入", () => {
	async function openWhere() {
		const loaded = await renderLoaded();
		await loaded.user.click(screen.getAllByTitle("Select columns")[1]);
		await screen.findByText("Select Columns");
		await loaded.user.selectOptions(
			screen.getAllByLabelText("Usage")[1],
			"where",
		);
		return loaded;
	}

	it("IS NOT NULL を OR でつなぎ、() でまとめて AND でメインクエリにつなぐ", async () => {
		const { user, onInsertColumn } = await openWhere();
		await user.selectOptions(screen.getByLabelText("Operator"), "isNull");
		await user.click(screen.getByLabelText("NOT"));
		await user.selectOptions(screen.getByLabelText("Join columns with"), "OR");
		await user.click(screen.getByLabelText("Group with ( )"));
		await user.selectOptions(
			screen.getByLabelText("Connect to main query"),
			"AND",
		);
		expect(screen.getByLabelText("Keyword line (WHERE)")).toBeDisabled();

		await user.click(screen.getByRole("button", { name: "All Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith([
			"AND (",
			"        ID IS NOT NULL",
			"    OR  NAME IS NOT NULL",
			"    )",
		]);
	});

	it("= で NOT を ON にすると != にする", async () => {
		const { user, onInsertColumn } = await openWhere();
		await user.click(screen.getByLabelText("NOT"));

		await user.click(screen.getByRole("button", { name: "All Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith([
			"WHERE",
			"    ID != ",
			"AND NAME != ",
		]);
	});

	it("IN は1カラムだけ選べ、値を1行で入れる", async () => {
		const { user, onInsertColumn } = await openWhere();
		await user.selectOptions(screen.getByLabelText("Operator"), "in");
		expect(screen.queryByRole("button", { name: "All Columns" })).toBeNull();
		await user.click(screen.getByText("ID"));
		await user.click(screen.getByText("NAME"));
		await user.type(screen.getByLabelText("Values"), "1, 2");

		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsertColumn).toHaveBeenCalledWith([
			"WHERE",
			"    NAME IN (1, 2)",
		]);
	});

	it("IN のサブクエリは、テーブル・SELECTする列・WHEREの列を続けて選ぶ", async () => {
		const { user, onInsertColumn } = await openWhere();
		await user.selectOptions(screen.getByLabelText("Operator"), "in");
		await user.click(screen.getByLabelText("NOT"));
		await user.selectOptions(screen.getByLabelText("IN source"), "subquery");
		await user.click(screen.getByText("ID"));
		const columnDialog = await findDialog("Select Columns");
		await user.click(columnDialog.getByRole("button", { name: "Next" }));

		const tableStep = await findDialog("Subquery Table");
		await user.click(tableStep.getByText("USER_ACCOUNT"));
		expect(tableStep.getByLabelText("Table alias")).toHaveValue("UA");
		await user.click(tableStep.getByRole("button", { name: "Next" }));

		const columnStep = await findDialog("Subquery Columns");
		const [selectList, whereList] = columnStep.getAllByRole("table");
		await user.click(within(selectList).getByText("ID"));
		await user.click(within(whereList).getByText("NAME"));
		await user.click(columnStep.getByRole("button", { name: "Insert" }));

		expect(onInsertColumn).toHaveBeenCalledWith([
			"WHERE",
			"    ID NOT IN (",
			"        SELECT",
			"            UA.ID",
			"        FROM",
			"            USER_ACCOUNT UA",
			"        WHERE",
			"            UA.NAME = ",
			"    )",
		]);
	});

	it("EXISTS はカラムを選ばず、サブクエリのテーブルを選んで SELECT 1 にする", async () => {
		const { user, onInsertColumn } = await openWhere();
		await user.selectOptions(screen.getByLabelText("Operator"), "exists");
		await user.selectOptions(
			screen.getByLabelText("Connect to main query"),
			"OR",
		);
		expect(screen.queryByPlaceholderText("Filter columns...")).toBeNull();
		const columnDialog = await findDialog("Select Columns");
		await user.click(columnDialog.getByRole("button", { name: "Next" }));

		const tableStep = await findDialog("Subquery Table");
		// 外側の別名（O）と重なるときは数字を付ける
		await user.click(tableStep.getByText("ORDERS"));
		expect(tableStep.getByLabelText("Table alias")).toHaveValue("O2");
		await user.click(tableStep.getByRole("button", { name: "Next" }));
		const columnStep = await findDialog("Subquery Columns");
		await user.click(columnStep.getByRole("button", { name: "Insert" }));

		expect(onInsertColumn).toHaveBeenCalledWith([
			"OR  EXISTS (",
			"        SELECT",
			"            1",
			"        FROM",
			"            ORDERS O2",
			"    )",
		]);
	});

	it("join の ON でも演算子を選べ、ON の位置に揃える", async () => {
		const { user, onInsert } = await renderLoaded();
		await user.click(screen.getByText("ORDERS"));
		await user.selectOptions(screen.getByLabelText("Usage"), "leftJoin");
		await user.click(screen.getByRole("button", { name: "Next" }));
		await screen.findByText("Select Columns");
		await user.selectOptions(screen.getByLabelText("Operator"), "isNull");
		expect(screen.queryByLabelText("Connect to main query")).toBeNull();

		await user.click(screen.getByRole("button", { name: "All Columns" }));
		expect(onInsert).toHaveBeenCalledWith([
			"    LEFT JOIN",
			"        ORDERS O",
			"          ON  O.ID IS NULL",
			"          AND O.NAME IS NULL",
		]);
	});
});

describe("SqlTableInsertDialogのサブクエリをテーブルにする挿入", () => {
	async function chooseSubquery(table: string, usage: string) {
		const loaded = await renderLoaded();
		await loaded.user.click(screen.getByText(table));
		await loaded.user.selectOptions(screen.getByLabelText("Usage"), usage);
		await loaded.user.selectOptions(
			screen.getByLabelText("Source"),
			"subquery",
		);
		return loaded;
	}

	it("サブクエリの別名が空なら進めない", async () => {
		const { user } = await chooseSubquery("USER_ACCOUNT", "select");
		expect(screen.getByRole("button", { name: "Next" })).toBeDisabled();
		await user.type(screen.getByLabelText("Subquery alias*"), "S");
		expect(screen.getByRole("button", { name: "Next" })).toBeEnabled();
	});

	it("Selectはサブクエリの列を選び、続けてその列から外側の列を選ぶ", async () => {
		const { user, onInsert } = await chooseSubquery("USER_ACCOUNT", "select");
		await user.type(screen.getByLabelText("Subquery alias*"), "S");
		await user.click(screen.getByRole("button", { name: "Next" }));

		const subquery = await findDialog("Subquery Columns");
		const [selectList, whereList] = subquery.getAllByRole("table");
		await user.click(within(selectList).getByText("ID"));
		await user.click(within(whereList).getByText("NAME"));
		await user.click(subquery.getByRole("button", { name: "Next" }));

		const outer = await findDialog("Select Columns");
		expect(outer.getByText("(subquery) S")).toBeInTheDocument();
		expect(outer.queryByText("NAME")).toBeNull();
		await user.click(outer.getByRole("button", { name: "All Columns" }));

		expect(onInsert).toHaveBeenCalledWith([
			"SELECT",
			"    S.ID",
			"FROM (",
			"    SELECT",
			"        UA.ID",
			"    FROM",
			"        USER_ACCOUNT UA",
			"    WHERE",
			"        UA.NAME = ",
			") S",
		]);
	});

	it("Inner joinはJOINの行末で括弧を開き、ONをサブクエリの別名で組み立てる", async () => {
		const { user, onInsert } = await chooseSubquery("ORDERS", "innerJoin");
		// 選んだテーブルの別名は任意になり、サブクエリの別名が必須になる
		expect(screen.getByLabelText("Table alias")).toBeInTheDocument();
		await user.type(screen.getByLabelText("Subquery alias*"), "OS");
		await user.click(screen.getByRole("button", { name: "Next" }));

		const subquery = await findDialog("Subquery Columns");
		await user.click(subquery.getByRole("button", { name: "All Columns" }));
		const outer = await findDialog("Select Columns");
		await user.click(outer.getByText("ID"));
		await user.click(outer.getByRole("button", { name: "Insert Columns" }));

		expect(onInsert).toHaveBeenCalledWith([
			"    INNER JOIN (",
			"        SELECT",
			"            O.ID",
			"          , O.NAME",
			"        FROM",
			"            ORDERS O",
			"    ) OS",
			"          ON  OS.ID = ",
		]);
	});

	it("Cross joinはサブクエリの列を選んだら挿入する", async () => {
		const { user, onInsert } = await chooseSubquery("ORDERS", "crossJoin");
		await user.type(screen.getByLabelText("Subquery alias*"), "OS");
		await user.click(screen.getByRole("button", { name: "Next" }));

		const subquery = await findDialog("Subquery Columns");
		const [selectList] = subquery.getAllByRole("table");
		await user.click(within(selectList).getByText("ID"));
		await user.click(subquery.getByRole("button", { name: "Insert" }));

		expect(onInsert).toHaveBeenCalledWith([
			"    CROSS JOIN (",
			"        SELECT",
			"            O.ID",
			"        FROM",
			"            ORDERS O",
			"    ) OS",
		]);
	});

	it("Usageを変えるとSourceはTableに戻り、Insert/Update/Deleteでは出さない", async () => {
		const { user } = await chooseSubquery("ORDERS", "select");
		await user.selectOptions(screen.getByLabelText("Usage"), "update");
		expect(screen.queryByLabelText("Source")).toBeNull();
		await user.selectOptions(screen.getByLabelText("Usage"), "leftJoin");
		expect(screen.getByLabelText("Source")).toHaveValue("table");
		expect(screen.queryByLabelText(/Subquery alias/)).toBeNull();
	});
});

describe("SqlTableInsertDialogのスカラー値の挿入", () => {
	async function openScalar() {
		const loaded = await renderLoaded();
		await loaded.user.click(screen.getByText("USER_ACCOUNT"));
		await loaded.user.click(screen.getAllByTitle("Select columns")[0]);
		await screen.findByText("Select Columns");
		await loaded.user.selectOptions(
			screen.getAllByLabelText("Usage")[1],
			"scalar",
		);
		return loaded;
	}

	it("カラムを1つ選び、COALESCE と AS を付けて SELECT の列に置く", async () => {
		const { user, onInsertColumn } = await openScalar();
		await user.click(screen.getByText("ID"));
		await user.click(screen.getByText("NAME"));
		await user.click(screen.getByLabelText("COALESCE"));
		await user.type(screen.getByLabelText("Fallback value"), "''");
		await user.type(screen.getByLabelText("AS name"), "USER_NAME");
		await user.click(screen.getByLabelText("Separator on first line (,)"));

		await user.click(screen.getByRole("button", { name: "Insert Scalar" }));
		expect(onInsertColumn).toHaveBeenCalledWith([
			"  , COALESCE(UA.NAME, '') AS USER_NAME",
		]);
	});

	it("CASE を ORDER BY に置く", async () => {
		const { user, onInsertColumn } = await openScalar();
		await user.selectOptions(
			screen.getByLabelText("Source", {
				selector: "#sqlColumnInsert_scalarSource",
			}),
			"case",
		);
		await user.selectOptions(screen.getByLabelText("Place in"), "orderBy");
		expect(screen.queryByLabelText("AS name")).toBeNull();
		await user.clear(screen.getByLabelText("WHEN count"));
		await user.type(screen.getByLabelText("WHEN count"), "2");
		await user.click(screen.getByText("ID"));

		await user.click(screen.getByRole("button", { name: "Insert Scalar" }));
		expect(onInsertColumn).toHaveBeenCalledWith([
			"    CASE",
			"        WHEN UA.ID =  THEN ",
			"        WHEN UA.ID =  THEN ",
			"        ELSE ",
			"    END",
		]);
	});

	it("サブクエリを条件の右辺に置くときは、改行を足さずに挿入する", async () => {
		const { user, onInsertColumn } = await openScalar();
		await user.selectOptions(
			screen.getByLabelText("Source", {
				selector: "#sqlColumnInsert_scalarSource",
			}),
			"subquery",
		);
		await user.selectOptions(screen.getByLabelText("Place in"), "condition");
		expect(screen.queryByPlaceholderText("Filter columns...")).toBeNull();
		expect(screen.queryByText(/Separator on first line/)).toBeNull();
		const columnDialog = await findDialog("Select Columns");
		await user.click(columnDialog.getByRole("button", { name: "Next" }));

		const tableStep = await findDialog("Subquery Table");
		await user.click(tableStep.getByText("ORDERS"));
		await user.click(tableStep.getByRole("button", { name: "Next" }));
		const columnStep = await findDialog("Subquery Columns");
		const [selectList] = columnStep.getAllByRole("table");
		await user.click(within(selectList).getByText("NAME"));
		await user.click(columnStep.getByRole("button", { name: "Insert" }));

		expect(onInsertColumn).toHaveBeenCalledWith(
			[
				"(",
				"    SELECT",
				"        O.NAME",
				"    FROM",
				"        ORDERS O",
				")",
			],
			true,
		);
	});
});
