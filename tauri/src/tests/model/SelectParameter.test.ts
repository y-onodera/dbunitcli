import type { CommandParam } from "../../model/CommandParam";
import type { Parameter } from "../../model/SelectParameter";
import { SelectParameter } from "../../model/SelectParameter";

const rawConvert = {
	srcData: {
		name: "convertSrcData",
		prefix: "convert",
		elements: [] as CommandParam[],
	},
	convertResult: {
		prefix: "convertResult",
		elements: [] as CommandParam[],
	},
} as unknown as Parameter;

const rawCompare = {
	elements: [] as CommandParam[],
	newData: { name: "compareNewData", prefix: "compare", elements: [] as CommandParam[] },
	oldData: { name: "compareOldData", prefix: "compare", elements: [] as CommandParam[] },
	imageOption: { name: "", prefix: "", elements: [] as CommandParam[] },
	convertResult: { prefix: "compare", elements: [] as CommandParam[] },
	expectData: { name: "compareExpectData", prefix: "compare", elements: [] as CommandParam[] },
} as unknown as Parameter;

const rawGenerate = {
	elements: [] as CommandParam[],
	srcData: { name: "generateSrcData", prefix: "generate", elements: [] as CommandParam[] },
} as unknown as Parameter;

const rawRun = {
	elements: [] as CommandParam[],
	srcData: { name: "runSrcData", prefix: "run", elements: [] as CommandParam[] },
} as unknown as Parameter;

const rawParameterize = {
	elements: [] as CommandParam[],
	paramData: { name: "parameterizeParamData", prefix: "parameterize", elements: [] as CommandParam[] },
} as unknown as Parameter;

describe("SelectParameterクラス", () => {
	it("convertコマンドで初期化できること", () => {
		const param = new SelectParameter(rawConvert, "convert", "testConvert");
		expect(param.name).toBe("testConvert");
		expect(param.command).toBe("convert");
		expect(param.convert.srcData.name).toBe("convertSrcData");
	});

	it("compareコマンドで初期化できること", () => {
		const param = new SelectParameter(rawCompare, "compare", "testCompare");
		expect(param.name).toBe("testCompare");
		expect(param.command).toBe("compare");
		expect(param.compare.newData.name).toBe("compareNewData");
	});

	it("generateコマンドで初期化できること", () => {
		const param = new SelectParameter(rawGenerate, "generate", "testGenerate");
		expect(param.name).toBe("testGenerate");
		expect(param.command).toBe("generate");
		expect(param.generate.commandElements.name).toBe("generate");
	});

	it("runコマンドで初期化できること", () => {
		const param = new SelectParameter(rawRun, "run", "testRun");
		expect(param.name).toBe("testRun");
		expect(param.command).toBe("run");
		expect(param.run.commandElements.name).toBe("run");
	});

	it("parameterizeコマンドで初期化できること", () => {
		const param = new SelectParameter(
			rawParameterize,
			"parameterize",
			"testParameterize",
		);
		expect(param.name).toBe("testParameterize");
		expect(param.command).toBe("parameterize");
		expect(param.parameterize.commandElements.name).toBe("parameterize");
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
