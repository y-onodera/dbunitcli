import type { CommandOption } from "../../model/CommandOption";
import type { Options } from "../../model/SelectParameter";
import { SelectParameter } from "../../model/SelectParameter";
import { makeMinimalParam } from "../setup";

const convertResultElements = [
	"resultType",
	"result",
	"resultPath",
	"exportEmptyTable",
	"exportHeader",
].map(makeMinimalParam);

const imageOptionElements = [
	"threshold",
	"pixelToleranceLevel",
	"allowingPercentOfDifferentPixels",
	"rectangleLineWidth",
	"minimalRectangleSize",
	"maximalRectangleCount",
	"excludedAreas",
	"drawExcludedRectangles",
	"fillExcludedRectangles",
	"percentOpacityExcludedRectangles",
	"excludedRectangleColor",
	"fillDifferenceRectangles",
	"percentOpacityDifferenceRectangles",
	"differenceRectangleColor",
].map(makeMinimalParam);

const rawConvert = {
	srcData: {
		name: "convertSrcData",
		prefix: "convert",
		elements: [] as CommandOption[],
	},
	convertResult: {
		prefix: "convertResult",
		elements: convertResultElements,
	},
} as unknown as Options;

const rawCompare = {
	elements: [] as CommandOption[],
	newData: {
		name: "compareNewData",
		prefix: "compare",
		elements: [] as CommandOption[],
	},
	oldData: {
		name: "compareOldData",
		prefix: "compare",
		elements: [] as CommandOption[],
	},
	imageOption: { name: "", prefix: "", elements: imageOptionElements },
	convertResult: { prefix: "compare", elements: convertResultElements },
	expectData: {
		name: "compareExpectData",
		prefix: "compare",
		elements: [] as CommandOption[],
	},
} as unknown as Options;

const rawGenerate = {
	elements: [
		"generateType",
		"unit",
		"template",
		"result",
		"resultPath",
		"outputEncoding",
	].map(makeMinimalParam),
	srcData: {
		name: "generateSrcData",
		prefix: "generate",
		elements: [] as CommandOption[],
	},
} as unknown as Options;

const rawRun = {
	elements: ["scriptType"].map(makeMinimalParam),
	srcData: {
		name: "runSrcData",
		prefix: "run",
		elements: [] as CommandOption[],
	},
} as unknown as Options;

const rawParameterize = {
	elements: [
		"unit",
		"parameterize",
		"ignoreFail",
		"cmd",
		"cmdParam",
		"template",
	].map(makeMinimalParam),
	paramData: {
		name: "parameterizeParamData",
		prefix: "parameterize",
		elements: [] as CommandOption[],
	},
} as unknown as Options;

describe("SelectParameterクラス", () => {
	it("convertコマンドで初期化できること", () => {
		const param = new SelectParameter(rawConvert, "convert", "testConvert");
		expect(param.name).toBe("testConvert");
		expect(param.command).toBe("convert");
	});

	it("compareコマンドで初期化できること", () => {
		const param = new SelectParameter(rawCompare, "compare", "testCompare");
		expect(param.name).toBe("testCompare");
		expect(param.command).toBe("compare");
	});

	it("generateコマンドで初期化できること", () => {
		const param = new SelectParameter(rawGenerate, "generate", "testGenerate");
		expect(param.name).toBe("testGenerate");
		expect(param.command).toBe("generate");
	});

	it("runコマンドで初期化できること", () => {
		const param = new SelectParameter(rawRun, "run", "testRun");
		expect(param.name).toBe("testRun");
		expect(param.command).toBe("run");
	});

	it("parameterizeコマンドで初期化できること", () => {
		const param = new SelectParameter(
			rawParameterize,
			"parameterize",
			"testParameterize",
		);
		expect(param.name).toBe("testParameterize");
		expect(param.command).toBe("parameterize");
	});

	it("レスポンスのinputParamを保存済みの-Pとして持つこと", () => {
		const param = new SelectParameter(
			{ ...rawConvert, inputParam: { orderDate: "2026-10-01" } },
			"convert",
			"testConvert",
		);
		expect(param.inputParam).toStrictEqual({ orderDate: "2026-10-01" });
	});

	it("inputParamがなければ読み込みごとに別の空オブジェクトになること", () => {
		// コンストラクタがレスポンスにinputParamを書き込むため、未設定のレスポンスを都度作る
		const first = new SelectParameter(
			{ ...rawConvert, inputParam: undefined },
			"convert",
			"a",
		);
		const second = new SelectParameter(
			{ ...rawConvert, inputParam: undefined },
			"convert",
			"b",
		);
		expect(first.inputParam).toStrictEqual({});
		expect(first.inputParam).not.toBe(second.inputParam);
	});

	it("引数のinputParamを優先し、optionsを引き継いでも同じinputParamを保つこと", () => {
		const inputParam = { key: "value" };
		const param = new SelectParameter(
			{ ...rawConvert },
			"convert",
			"a",
			inputParam,
		);
		expect(param.inputParam).toBe(inputParam);
		const renamed = new SelectParameter(param.options, "convert", "renamed");
		expect(renamed.inputParam).toBe(inputParam);
	});
});
