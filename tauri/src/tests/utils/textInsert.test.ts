import {
	columnLines,
	defaultTableAlias,
	insertAtCursor,
	qualifyColumn,
	tableLines,
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
	it("スネークケースの各語の先頭文字を大文字でつなげる", () => {
		expect(defaultTableAlias("user_account")).toBe("UA");
		expect(defaultTableAlias("USERS")).toBe("U");
	});

	it("スキーマ修飾と連続・前後の_は無視する", () => {
		expect(defaultTableAlias("PUBLIC.ORDER__DETAIL_")).toBe("OD");
	});
});

describe("tableLines", () => {
	it("SELECTは列の後ろにFROMとテーブルを置く", () => {
		expect(
			tableLines("TABLE", "select", "T", [
				"    T.COLUMNA AS A",
				"  , T.COLUMNB",
			]),
		).toEqual([
			"SELECT",
			"    T.COLUMNA AS A",
			"  , T.COLUMNB",
			"FROM",
			"    TABLE T",
		]);
	});

	it("INSERTはエイリアスを付けず、列を括弧で囲みVALUESを続ける", () => {
		expect(tableLines("TABLE", "insert", "T", ["    A", "  , B"])).toEqual([
			"INSERT INTO",
			"    TABLE",
			"(",
			"    A",
			"  , B",
			")",
			"VALUES",
			"(",
			")",
		]);
	});

	it("UPDATEはSETに続けて列を置く", () => {
		expect(tableLines("TABLE", "update", "T", ["    T.A = "])).toEqual([
			"UPDATE",
			"    TABLE T",
			"SET",
			"    T.A = ",
		]);
	});

	it("DELETEはテーブルだけ、エイリアスが空ならテーブル名だけ", () => {
		expect(tableLines("TABLE", "delete", " ", [])).toEqual([
			"DELETE FROM",
			"    TABLE",
		]);
	});

	it("JOINはFROMの中身の位置に置き、結合先とONの列を字下げする", () => {
		expect(
			tableLines("TABLE2", "innerJoin", "T2", ["          ON  T2.COLUMNA = "]),
		).toEqual([
			"    INNER JOIN",
			"        TABLE2 T2",
			"          ON  T2.COLUMNA = ",
		]);
		expect(tableLines("TABLE2", "leftJoin", "T2", [])[0]).toBe("    LEFT JOIN");
		expect(tableLines("TABLE2", "crossJoin", "T2", [])).toEqual([
			"    CROSS JOIN",
			"        TABLE2 T2",
		]);
	});
});

describe("columnLines", () => {
	const noAlias = { alias: "", columnAs: false };

	it("SELECTは4字下げし、2行目以降を  , でつなぎ、指定時は AS を付ける", () => {
		expect(
			columnLines(["COLUMNA", "COLUMNB"], "select", {
				alias: "T",
				columnAs: true,
			}),
		).toEqual(["    T.COLUMNA AS COLUMNA", "  , T.COLUMNB AS COLUMNB"]);
	});

	it("keyword指定時はキーワード行を先頭に置く", () => {
		expect(
			columnLines(["A", "B"], "orderBy", { ...noAlias, keyword: true }),
		).toEqual(["ORDER BY", "    A", "  , B"]);
		expect(
			columnLines(["A"], "groupBy", { ...noAlias, keyword: true }),
		).toEqual(["GROUP BY", "    A"]);
		expect(columnLines(["A"], "update", { ...noAlias, keyword: true })).toEqual(
			["SET", "    A = "],
		);
	});

	it("WHEREは = を付け、2行目以降はANDを行頭に置く", () => {
		expect(
			columnLines(["COLUMNA", "COLUMNB"], "where", {
				alias: "T",
				columnAs: false,
				keyword: true,
			}),
		).toEqual(["WHERE", "    T.COLUMNA = ", "AND T.COLUMNB = "]);
	});

	it("ONは結合先の下に字下げし、2行目以降はONの位置にANDを置く", () => {
		expect(
			columnLines(["A", "B"], "on", { alias: "T2", columnAs: false }),
		).toEqual(["          ON  T2.A = ", "          AND T2.B = "]);
	});

	it("continued なら1行目にも区切りを付け、キーワード行は付けない", () => {
		expect(
			columnLines(["A"], "where", {
				...noAlias,
				continued: true,
				keyword: true,
			}),
		).toEqual(["AND A = "]);
		expect(
			columnLines(["A"], "select", { ...noAlias, continued: true }),
		).toEqual(["  , A"]);
	});

	it("INSERT はエイリアスを指定しても付けない", () => {
		expect(
			columnLines(["ID", "NAME"], "insert", { alias: "U", columnAs: true }),
		).toEqual(["    ID", "  , NAME"]);
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
