import type { CommandParam } from "../../model/CommandParam";
import type { Parameter } from "../../model/SelectParameter";
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
		elements: [] as CommandParam[],
	},
	convertResult: {
		prefix: "convertResult",
		elements: convertResultElements,
	},
} as unknown as Parameter;

const rawCompare = {
	elements: [] as CommandParam[],
	newData: {
		name: "compareNewData",
		prefix: "compare",
		elements: [] as CommandParam[],
	},
	oldData: {
		name: "compareOldData",
		prefix: "compare",
		elements: [] as CommandParam[],
	},
	imageOption: { name: "", prefix: "", elements: imageOptionElements },
	convertResult: { prefix: "compare", elements: convertResultElements },
	expectData: {
		name: "compareExpectData",
		prefix: "compare",
		elements: [] as CommandParam[],
	},
} as unknown as Parameter;

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
		elements: [] as CommandParam[],
	},
} as unknown as Parameter;

const rawRun = {
	elements: ["scriptType"].map(makeMinimalParam),
	srcData: {
		name: "runSrcData",
		prefix: "run",
		elements: [] as CommandParam[],
	},
} as unknown as Parameter;

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
		elements: [] as CommandParam[],
	},
} as unknown as Parameter;

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
});
