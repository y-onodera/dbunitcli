import { describe, expect, it } from "vitest";
import { decorateLines, insertAtCursor } from "../../utils/textInsert";

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

describe("decorateLines", () => {
	it("全行の前後に文字列を付ける", () => {
		expect(decorateLines(["a", "b"], "and ", " = ")).toEqual([
			"and a = ",
			"and b = ",
		]);
	});

	it("前後が空なら元のまま", () => {
		expect(decorateLines(["a", "b"], "", "")).toEqual(["a", "b"]);
	});
});
