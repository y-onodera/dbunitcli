import {
	type ConditionOptions,
	columnLines,
	conditionLines,
	csvFileAlias,
	csvReadExpression,
	defaultTableAlias,
	derivedTableLines,
	insertAtCursor,
	insertInlineAtCursor,
	placeScalar,
	qualifyColumn,
	replaceSelection,
	scalarExpression,
	subqueryLines,
	tableLines,
	uniqueAlias,
	withCoalesce,
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

describe("csvReadExpression / csvFileAlias", () => {
	it("パスの区切りを / に揃え、' を重ねて文字列リテラルにする", () => {
		expect(csvReadExpression("dataset\\it's.csv", " MS932 ")).toBe(
			"CSVREAD('dataset/it''s.csv',null,'charset=MS932')",
		);
	});

	it("拡張子を除いたファイル名から別名を作る", () => {
		expect(csvFileAlias("dataset/user_account.csv")).toBe("UA");
		expect(csvFileAlias("C:\\data\\orders.csv")).toBe("O");
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

describe("conditionLines", () => {
	const base: ConditionOptions = {
		operator: "equals",
		not: false,
		conjunction: "AND",
		group: false,
		connector: null,
		keyword: false,
		inValues: "",
		subquery: null,
	};

	it("ON の () は ON の後ろで開き、中の条件をさらに字下げする", () => {
		expect(
			conditionLines(["T2.A", "T2.B"], "on", {
				...base,
				conjunction: "OR",
				group: true,
			}),
		).toEqual([
			"          ON  (",
			"                  T2.A = ",
			"              OR  T2.B = ",
			"              )",
		]);
	});

	it("ON に続けるときは AND / OR を ON の位置に置く", () => {
		expect(
			conditionLines(["T2.A"], "on", { ...base, connector: "OR" }),
		).toEqual(["          OR  T2.A = "]);
	});

	it("WHERE の NOT EXISTS はサブクエリを条件の位置から字下げする", () => {
		expect(
			conditionLines([], "where", {
				...base,
				operator: "exists",
				not: true,
				keyword: true,
				subquery: subqueryLines("T2", "X", ["    1"], []),
			}),
		).toEqual([
			"WHERE",
			"    NOT EXISTS (",
			"        SELECT",
			"            1",
			"        FROM",
			"            T2 X",
			"    )",
		]);
	});

	it("IN の値は1行にまとめ、NOT を付けられる", () => {
		expect(
			conditionLines(["T.A"], "where", {
				...base,
				operator: "in",
				not: true,
				inValues: " 'a', 'b' ",
			}),
		).toEqual(["    T.A NOT IN ('a', 'b')"]);
	});
});

describe("derivedTableLines", () => {
	const sub = ["SELECT", "    A", "FROM", "    T"];

	it("SELECTはFROMの行末で括弧を開き、閉じ括弧の後ろに別名を置く", () => {
		expect(derivedTableLines("select", sub, " S ", ["    S.A"])).toEqual([
			"SELECT",
			"    S.A",
			"FROM (",
			"    SELECT",
			"        A",
			"    FROM",
			"        T",
			") S",
		]);
	});

	it("LEFT JOINは結合先の位置にサブクエリを置き、ONの列を続ける", () => {
		expect(
			derivedTableLines("leftJoin", sub, "S", ["          ON  S.A = "]),
		).toEqual([
			"    LEFT JOIN (",
			"        SELECT",
			"            A",
			"        FROM",
			"            T",
			"    ) S",
			"          ON  S.A = ",
		]);
	});

	it("CROSS JOINは列を付けない", () => {
		expect(derivedTableLines("crossJoin", sub, "S", ["x"]).slice(-1)[0]).toBe(
			"    ) S",
		);
	});
});

describe("insertInlineAtCursor", () => {
	it("改行を足さずに入れ、2行目以降は行の要素の位置に揃える", () => {
		const value = "WHERE\n    T.A = \nAND T.B = ";
		const cursor = value.length;
		expect(
			insertInlineAtCursor(value, ["(", "    SELECT", ")"], cursor, cursor),
		).toEqual({
			value: "WHERE\n    T.A = \nAND T.B = (\n        SELECT\n    )",
			cursor: value.length + "(\n        SELECT\n    )".length,
		});
	});

	it("1行ならカーソル位置にそのまま入れ、選択範囲は置き換える", () => {
		expect(insertInlineAtCursor("T.A = X;", ["1"], 6, 7)).toEqual({
			value: "T.A = 1;",
			cursor: 7,
		});
	});
});

describe("scalarExpression / withCoalesce / placeScalar", () => {
	const none = { whenCount: 1, subquery: null };

	it("CASE は指定した数の WHEN を並べる", () => {
		expect(scalarExpression("case", "T.A", { ...none, whenCount: 2 })).toEqual([
			"CASE",
			"    WHEN T.A =  THEN ",
			"    WHEN T.A =  THEN ",
			"    ELSE ",
			"END",
		]);
	});

	it("サブクエリは括弧を単独の行にして字下げする", () => {
		expect(
			scalarExpression("subquery", "", {
				whenCount: 1,
				subquery: ["SELECT", "    X.B", "FROM", "    T2 X"],
			}),
		).toEqual([
			"(",
			"    SELECT",
			"        X.B",
			"    FROM",
			"        T2 X",
			")",
		]);
	});

	it("COALESCE は1行なら1行、複数行なら外側で開く", () => {
		expect(withCoalesce(["T.A"], " 0 ")).toEqual(["COALESCE(T.A, 0)"]);
		expect(withCoalesce(["CASE", "END"], "0")).toEqual([
			"COALESCE(",
			"    CASE",
			"    END",
			"  , 0",
			")",
		]);
	});

	it("SELECT の列に置き、最後の行に AS を付ける。ORDER BY では付けない", () => {
		expect(
			placeScalar(["(", "    SELECT", ")"], "select", {
				continued: true,
				asName: "CNT",
			}),
		).toEqual(["  , (", "        SELECT", "    ) AS CNT"]);
		expect(
			placeScalar(["T.A"], "orderBy", { continued: false, asName: "X" }),
		).toEqual(["    T.A"]);
	});
});

describe("uniqueAlias", () => {
	it("使われている別名と重なるときは数字を付ける", () => {
		expect(uniqueAlias("T", ["T"])).toBe("T2");
		expect(uniqueAlias("T", ["T", "T2"])).toBe("T3");
		expect(uniqueAlias("U", ["T"])).toBe("U");
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

describe("replaceSelection", () => {
	it("選択範囲を置き換え、カーソルを挿入した文字列の直後に置く", () => {
		expect(replaceSelection("(  ) && x", "a == b", 1, 3)).toEqual({
			value: "(a == b) && x",
			cursor: 7,
		});
	});
});
