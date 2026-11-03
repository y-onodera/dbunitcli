import { describe, expect, it } from "vitest";
import {
	columnLines,
	defaultTableAlias,
	insertAtCursor,
	qualifyColumn,
	statementLines,
	tableClause,
} from "../../utils/textInsert";

describe("insertAtCursor", () => {
	it("空文字ならそのまま挿入し末尾に改行を付ける", () => {
		expect(insertAtCursor("", "T1", 0, 0)).toEqual({
			value: "T1\n",
			cursor: 3,
		});
	});

	it("行の途中では前後に改行を足す", () => {
		expect(insertAtCursor("SELECT * FROM ", "T1", 14, 14)).toEqual({
			value: "SELECT * FROM \nT1\n",
			cursor: 18,
		});
	});

	it("行頭では前の改行を足さない", () => {
		expect(insertAtCursor("A\nB", "T1", 2, 2)).toEqual({
			value: "A\nT1\nB",
			cursor: 5,
		});
	});

	it("直後が改行なら後ろの改行を足さない", () => {
		expect(insertAtCursor("A\nB", "T1", 1, 1)).toEqual({
			value: "A\nT1\nB",
			cursor: 4,
		});
	});

	it("選択範囲は置換される", () => {
		expect(insertAtCursor("A\nXX\nB", "T1", 2, 4)).toEqual({
			value: "A\nT1\nB",
			cursor: 4,
		});
	});

	it("連続挿入は直前の結果のカーソルから続く", () => {
		const first = insertAtCursor("", "c1", 0, 0);
		const second = insertAtCursor(
			first.value,
			"c2",
			first.cursor,
			first.cursor,
		);
		expect(second.value).toBe("c1\nc2\n");
	});
});

describe("defaultTableAlias", () => {
	it("スネークケースの各語の先頭文字を小文字でつなげる", () => {
		expect(defaultTableAlias("USER_ACCOUNT")).toBe("ua");
		expect(defaultTableAlias("USERS")).toBe("u");
	});

	it("スキーマ修飾と連続・前後の_は無視する", () => {
		expect(defaultTableAlias("PUBLIC.ORDER__DETAIL_")).toBe("od");
	});
});

describe("tableClause", () => {
	it("用途ごとのキーワードとエイリアスを付ける", () => {
		expect(tableClause("USERS", "select", "u")).toBe("from USERS u");
		expect(tableClause("USERS", "update", "u")).toBe("update USERS u");
		expect(tableClause("USERS", "delete", "u")).toBe("delete from USERS u");
		expect(tableClause("ORDERS", "leftJoin", "o")).toBe(
			"left join ORDERS o on",
		);
		expect(tableClause("ORDERS", "innerJoin", "o")).toBe(
			"inner join ORDERS o on",
		);
		expect(tableClause("ORDERS", "crossJoin", "o")).toBe("cross join ORDERS o");
	});

	it("エイリアスが空ならテーブル名だけ、insertではエイリアスを付けない", () => {
		expect(tableClause("USERS", "select", " ")).toBe("from USERS");
		expect(tableClause("USERS", "insert", "u")).toBe("insert into USERS");
	});
});

describe("columnLines", () => {
	const noAlias = { alias: "", columnAs: false };

	it("selectは2行目以降を , でつなぎ、指定時は列名で as を付ける", () => {
		expect(
			columnLines(["ID", "NAME"], "select", { alias: "u", columnAs: true }),
		).toEqual(["u.ID as ID", ", u.NAME as NAME"]);
		expect(columnLines(["ID", "NAME"], "select", noAlias)).toEqual([
			"ID",
			", NAME",
		]);
	});

	it("order by は , でつなぎ、as は付けない", () => {
		expect(
			columnLines(["ID", "NAME"], "orderBy", { alias: "u", columnAs: true }),
		).toEqual(["u.ID", ", u.NAME"]);
	});

	it("where/on は = を付けて and でつなぐ", () => {
		expect(
			columnLines(["ID", "NAME"], "condition", { alias: "u", columnAs: false }),
		).toEqual(["u.ID = ", "and u.NAME = "]);
	});

	it("update は = を付けて , でつなぐ", () => {
		expect(columnLines(["ID", "NAME"], "update", noAlias)).toEqual([
			"ID = ",
			", NAME = ",
		]);
	});

	it("continued なら1行目にも区切りを付ける", () => {
		expect(
			columnLines(["ID", "NAME"], "condition", {
				alias: "",
				columnAs: false,
				continued: true,
			}),
		).toEqual(["and ID = ", "and NAME = "]);
		expect(
			columnLines(["ID"], "select", {
				alias: "u",
				columnAs: false,
				continued: true,
			}),
		).toEqual([", u.ID"]);
	});

	it("insert はエイリアスを指定しても付けない", () => {
		expect(
			columnLines(["ID", "NAME"], "insert", { alias: "u", columnAs: true }),
		).toEqual(["ID", ", NAME"]);
	});
});

describe("statementLines", () => {
	it("用途ごとにテーブルの句とカラムの行を組み合わせる", () => {
		expect(
			statementLines("USERS", "select", "u", ["u.ID", ", u.NAME"]),
		).toEqual(["select", "u.ID", ", u.NAME", "from USERS u"]);
		expect(statementLines("USERS", "insert", "u", ["ID", ", NAME"])).toEqual([
			"insert into USERS (",
			"ID",
			", NAME",
			") values (",
			")",
		]);
		expect(statementLines("USERS", "update", "u", ["u.ID = "])).toEqual([
			"update USERS u set",
			"u.ID = ",
		]);
	});
});

describe("qualifyColumn", () => {
	it("エイリアス指定時は エイリアス.カラム名 にする", () => {
		expect(qualifyColumn("ID", "t1")).toBe("t1.ID");
	});

	it("エイリアスが空または空白のみならカラム名のまま", () => {
		expect(qualifyColumn("ID", "")).toBe("ID");
		expect(qualifyColumn("ID", "  ")).toBe("ID");
	});
});
