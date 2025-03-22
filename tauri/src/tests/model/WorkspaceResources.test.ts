import { describe, expect, it } from "vitest";
import type { ParameterListBuilder } from "../../model/WorkspaceResources";
import { ParameterList } from "../../model/WorkspaceResources";

describe("ParameterListクラス", () => {
	it("空のパラメータリストを作成できること", () => {
		const paramList = ParameterList.create();
		expect(paramList.convert).toEqual([]);
		expect(paramList.compare).toEqual([]);
		expect(paramList.generate).toEqual([]);
		expect(paramList.run).toEqual([]);
		expect(paramList.parameterize).toEqual([]);
	});

	it("ビルダーからパラメータリストを作成できること", () => {
		const builder: ParameterListBuilder = {
			convert: ["convert1", "convert2"],
			compare: ["compare1"],
			generate: ["generate1", "generate2", "generate3"],
			run: ["run1"],
			parameterize: ["param1", "param2"],
		};

		const paramList = ParameterList.from(builder);
		expect(paramList.convert).toEqual(["convert1", "convert2"]);
		expect(paramList.compare).toEqual(["compare1"]);
		expect(paramList.generate).toEqual(["generate1", "generate2", "generate3"]);
		expect(paramList.run).toEqual(["run1"]);
		expect(paramList.parameterize).toEqual(["param1", "param2"]);
	});

	it("convertパラメータを置換できること", () => {
		const paramList = ParameterList.create();
		const newList = ["new1", "new2"];
		const updatedList = paramList.replace("convert", newList);
		expect(updatedList.convert).toEqual(newList);
		expect(updatedList.compare).toEqual([]);
		expect(updatedList.generate).toEqual([]);
		expect(updatedList.run).toEqual([]);
		expect(updatedList.parameterize).toEqual([]);
	});

	it("compareパラメータを置換できること", () => {
		const paramList = ParameterList.create();
		const newList = ["new1", "new2"];
		const updatedList = paramList.replace("compare", newList);
		expect(updatedList.convert).toEqual([]);
		expect(updatedList.compare).toEqual(newList);
		expect(updatedList.generate).toEqual([]);
		expect(updatedList.run).toEqual([]);
		expect(updatedList.parameterize).toEqual([]);
	});

	it("generateパラメータを置換できること", () => {
		const paramList = ParameterList.create();
		const newList = ["new1", "new2"];
		const updatedList = paramList.replace("generate", newList);
		expect(updatedList.convert).toEqual([]);
		expect(updatedList.compare).toEqual([]);
		expect(updatedList.generate).toEqual(newList);
		expect(updatedList.run).toEqual([]);
		expect(updatedList.parameterize).toEqual([]);
	});

	it("runパラメータを置換できること", () => {
		const paramList = ParameterList.create();
		const newList = ["new1", "new2"];
		const updatedList = paramList.replace("run", newList);
		expect(updatedList.convert).toEqual([]);
		expect(updatedList.compare).toEqual([]);
		expect(updatedList.generate).toEqual([]);
		expect(updatedList.run).toEqual(newList);
		expect(updatedList.parameterize).toEqual([]);
	});

	it("parameterizeパラメータを置換できること", () => {
		const paramList = ParameterList.create();
		const newList = ["new1", "new2"];
		const updatedList = paramList.replace("parameterize", newList);
		expect(updatedList.convert).toEqual([]);
		expect(updatedList.compare).toEqual([]);
		expect(updatedList.generate).toEqual([]);
		expect(updatedList.run).toEqual([]);
		expect(updatedList.parameterize).toEqual(newList);
	});

	it("置換時に既存の値を維持できること", () => {
		const builder: ParameterListBuilder = {
			convert: ["convert1"],
			compare: ["compare1"],
			generate: ["generate1"],
			run: ["run1"],
			parameterize: ["param1"],
		};

		const paramList = ParameterList.from(builder);
		const newList = ["new1", "new2"];
		const updatedList = paramList.replace("convert", newList);

		expect(updatedList.convert).toEqual(newList);
		expect(updatedList.compare).toEqual(["compare1"]);
		expect(updatedList.generate).toEqual(["generate1"]);
		expect(updatedList.run).toEqual(["run1"]);
		expect(updatedList.parameterize).toEqual(["param1"]);
	});
});
