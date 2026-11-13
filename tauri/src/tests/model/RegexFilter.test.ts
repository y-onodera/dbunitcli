import { describe, expect, it } from "vitest";
import {
	buildFilterRegex,
	matchedKeys,
	toFilterRows,
} from "../../model/RegexFilter";

describe("buildFilterRegex", () => {
	it("テーブル名は特殊文字をエスケープして完全一致のORにする", () => {
		expect(buildFilterRegex("table", ["OLD_USERS", "a.b"])).toBe(
			"^(?:OLD_USERS|a\\.b)$",
		);
	});

	it("パスは区切りを/と\\の両方に合わせ、区切りか先頭からの末尾一致にする", () => {
		const regex = buildFilterRegex("path", ["old/a.csv", "b.csv"]);
		expect(regex).toBe("(?:^|[\\\\/])(?:old[\\\\/]a\\.csv|b\\.csv)$");
		const pattern = new RegExp(regex);
		expect(pattern.test("C:\\data\\old\\a.csv")).toBe(true);
		expect(pattern.test("/data/old/a.csv")).toBe(true);
		expect(pattern.test("/data/b.csv")).toBe(true);
		expect(pattern.test("/data/new/a.csv")).toBe(false);
		expect(pattern.test("/data/xb.csv")).toBe(false);
	});

	it("何も選ばなければ空にする", () => {
		expect(buildFilterRegex("path", [])).toBe("");
	});
});

describe("toFilterRows", () => {
	it("正規表現を当てる値ごとに最終的なテーブル名をまとめる", () => {
		const sources = [
			{ tableName: "merged", filePath: "a.csv", sourceTableName: "a" },
			{ tableName: "merged", filePath: "b.csv", sourceTableName: "b" },
			{ tableName: "c", filePath: "book.xlsx", sourceTableName: "c" },
			{ tableName: "d", filePath: "book.xlsx", sourceTableName: "d" },
		];
		expect(toFilterRows("path", sources)).toEqual([
			{ key: "a.csv", tableNames: ["merged"] },
			{ key: "b.csv", tableNames: ["merged"] },
			{ key: "book.xlsx", tableNames: ["c", "d"] },
		]);
		expect(toFilterRows("table", sources).map((row) => row.key)).toEqual([
			"a",
			"b",
			"c",
			"d",
		]);
	});
});

describe("matchedKeys", () => {
	it("パスは相対パスかsrcを付けたパスに一致する値を返す", () => {
		expect(
			matchedKeys("path", "^/data/old", ["old/a.csv", "new/a.csv"], "/data/"),
		).toEqual(["old/a.csv"]);
	});

	it("解釈できない正規表現や空なら何も返さない", () => {
		expect(matchedKeys("table", "(", ["a"], "")).toEqual([]);
		expect(matchedKeys("table", "", ["a"], "")).toEqual([]);
	});
});
