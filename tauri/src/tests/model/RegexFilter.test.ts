import { describe, expect, it } from "vitest";
import {
	buildFilterRegex,
	matchedKeys,
	type TableSource,
	toFilterRows,
} from "../../model/RegexFilter";

function source(
	tableName: string,
	filePath: string,
	sourceTableName: string,
	matchPath = filePath,
): TableSource {
	return { tableName, filePath, matchPath, sourceTableName };
}

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
			source("merged", "a.csv", "a"),
			source("merged", "b.csv", "b"),
			source("c", "book.xlsx", "c"),
			source("d", "book.xlsx", "d"),
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
	it("パスはsidecarが照合に使うパスに当て、一致した行の相対パスを返す", () => {
		const sources = [
			source("OLD", "old/a.csv", "a", "data/old/a.csv"),
			source("NEW", "new/a.csv", "a", "data/new/a.csv"),
		];
		expect(matchedKeys("path", "^data/old", sources)).toEqual(["old/a.csv"]);
	});

	it("テーブル名は元のテーブル名に当てる", () => {
		expect(
			matchedKeys("table", "^a$", [source("renamed", "a.csv", "a")]),
		).toEqual(["a"]);
	});

	it("解釈できない正規表現や空なら何も返さない", () => {
		const sources = [source("a", "a.csv", "a")];
		expect(matchedKeys("table", "(", sources)).toEqual([]);
		expect(matchedKeys("table", "", sources)).toEqual([]);
	});
});
