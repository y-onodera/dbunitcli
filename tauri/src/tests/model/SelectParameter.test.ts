import type { CommandParam } from "../../model/CommandParam";
import type { Parameter } from "../../model/SelectParameter";
import { SelectParameter } from "../../model/SelectParameter";

function makeMinimalParam(name: string): CommandParam {
	return {
		name,
		value: "",
		attribute: {
			type: "TEXT",
			required: false,
			selectOption: [],
			defaultPath: "WORKSPACE",
		},
		optional: false,
	};
}

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

	it("現在のパラメータを正しく返却すること", () => {
		const param = new SelectParameter(rawConvert, "convert", "testConvert");
		expect(param.currentParameter()).toBe(param.convert);

		const param2 = new SelectParameter(rawCompare, "compare", "testCompare");
		expect(param2.currentParameter()).toBe(param2.compare);

		const param3 = new SelectParameter(rawGenerate, "generate", "testGenerate");
		expect(param3.currentParameter()).toBe(param3.generate);

		const param4 = new SelectParameter(rawRun, "run", "testRun");
		expect(param4.currentParameter()).toBe(param4.run);

		const param5 = new SelectParameter(
			rawParameterize,
			"parameterize",
			"testParameterize",
		);
		expect(param5.currentParameter()).toBe(param5.parameterize);
	});
});
