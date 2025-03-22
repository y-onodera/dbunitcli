import { describe, expect, it } from "vitest";
import type {
	CompareParams,
	ConvertParams,
	GenerateParams,
	ParameterizeParams,
	RunParams,
} from "../../model/CommandParam";
import { SelectParameter } from "../../model/CommandParam";

describe("SelectParameterクラス", () => {
	const mockConvertParams: ConvertParams = {
		srcData: {
			handleTypeSelect: async () => {},
			name: "convertSrcData",
			prefix: "convert",
			elements: [],
			srcElements: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
			srcTypeSettings: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
			settingElements: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
		},
		convertResult: {
			handleTypeSelect: async () => {},
			name: "convertResult",
			prefix: "convert",
			elements: [],
			jdbc: {
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			},
		},
	};

	const mockCompareParams: CompareParams = {
		elements: [],
		newData: {
			handleTypeSelect: async () => {},
			name: "compareNewData",
			prefix: "compare",
			elements: [],
			srcElements: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
			srcTypeSettings: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
			settingElements: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
		},
		oldData: {
			handleTypeSelect: async () => {},
			name: "compareOldData",
			prefix: "compare",
			elements: [],
			srcElements: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
			srcTypeSettings: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
			settingElements: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
		},
		imageOption: {
			handleTypeSelect: async () => {},
			name: "",
			prefix: "",
			elements: [],
		},
		convertResult: {
			handleTypeSelect: async () => {},
			name: "compareConvertResult",
			prefix: "compare",
			elements: [],
			jdbc: {
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			},
		},
		expectData: {
			handleTypeSelect: async () => {},
			name: "compareExpectData",
			prefix: "compare",
			elements: [],
			srcElements: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
			srcTypeSettings: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
			settingElements: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
		},
	};

	const mockGenerateParams: GenerateParams = {
		elements: [],
		srcData: {
			handleTypeSelect: async () => {},
			name: "generateSrcData",
			prefix: "generate",
			elements: [],
			srcElements: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
			srcTypeSettings: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
			settingElements: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
		},
		templateOption: {
			handleTypeSelect: async () => {},
			name: "",
			prefix: "",
			elements: [],
		},
	};

	const mockRunParams: RunParams = {
		elements: [],
		srcData: {
			handleTypeSelect: async () => {},
			name: "runSrcData",
			prefix: "run",
			elements: [],
			srcElements: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
			srcTypeSettings: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
			settingElements: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
		},
		templateOption: {
			handleTypeSelect: async () => {},
			name: "",
			prefix: "",
			elements: [],
		},
		jdbcOption: {
			handleTypeSelect: async () => {},
			name: "",
			prefix: "",
			elements: [],
		},
	};

	const mockParameterizeParams: ParameterizeParams = {
		elements: [],
		paramData: {
			handleTypeSelect: async () => {},
			name: "parameterizeParamData",
			prefix: "parameterize",
			elements: [],
			srcElements: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
			srcTypeSettings: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
			settingElements: () => ({
				handleTypeSelect: async () => {},
				name: "",
				prefix: "",
				elements: [],
			}),
		},
		templateOption: {
			handleTypeSelect: async () => {},
			name: "",
			prefix: "",
			elements: [],
		},
	};

	it("convertコマンドで初期化できること", () => {
		const param = new SelectParameter(
			mockConvertParams,
			"convert",
			"testConvert",
		);
		expect(param.name).toBe("testConvert");
		expect(param.command).toBe("convert");
		expect(param.convert).toBe(mockConvertParams);
	});

	it("compareコマンドで初期化できること", () => {
		const param = new SelectParameter(
			mockCompareParams,
			"compare",
			"testCompare",
		);
		expect(param.name).toBe("testCompare");
		expect(param.command).toBe("compare");
		expect(param.compare).toBe(mockCompareParams);
	});

	it("generateコマンドで初期化できること", () => {
		const param = new SelectParameter(
			mockGenerateParams,
			"generate",
			"testGenerate",
		);
		expect(param.name).toBe("testGenerate");
		expect(param.command).toBe("generate");
		expect(param.generate).toBe(mockGenerateParams);
	});

	it("runコマンドで初期化できること", () => {
		const param = new SelectParameter(mockRunParams, "run", "testRun");
		expect(param.name).toBe("testRun");
		expect(param.command).toBe("run");
		expect(param.run).toBe(mockRunParams);
	});

	it("parameterizeコマンドで初期化できること", () => {
		const param = new SelectParameter(
			mockParameterizeParams,
			"parameterize",
			"testParameterize",
		);
		expect(param.name).toBe("testParameterize");
		expect(param.command).toBe("parameterize");
		expect(param.parameterize).toBe(mockParameterizeParams);
	});

	it("現在のパラメータを正しく返却すること", () => {
		const param = new SelectParameter(
			mockConvertParams,
			"convert",
			"testConvert",
		);
		expect(param.currentParameter()).toBe(mockConvertParams);

		const param2 = new SelectParameter(
			mockCompareParams,
			"compare",
			"testCompare",
		);
		expect(param2.currentParameter()).toBe(mockCompareParams);

		const param3 = new SelectParameter(
			mockGenerateParams,
			"generate",
			"testGenerate",
		);
		expect(param3.currentParameter()).toBe(mockGenerateParams);

		const param4 = new SelectParameter(mockRunParams, "run", "testRun");
		expect(param4.currentParameter()).toBe(mockRunParams);

		const param5 = new SelectParameter(
			mockParameterizeParams,
			"parameterize",
			"testParameterize",
		);
		expect(param5.currentParameter()).toBe(mockParameterizeParams);
	});
});
