import type { CommandParams } from "../../model/CommandParam";
import type {
	CompareParams,
	ConvertParams,
	GenerateParams,
	ParameterizeParams,
	RunParams,
} from "../../model/SelectParameter";
import { SelectParameter } from "../../model/SelectParameter";

const createCommandParams = (): CommandParams => ({
	name: "",
	prefix: "",
	elements: [],
});

describe("SelectParameterクラス", () => {
	const mockConvertParams: ConvertParams = {
		srcData: {
			name: "convertSrcData",
			prefix: "convert",
			elements: [],
			srcType: () => "csv",
			srcElements: () => createCommandParams(),
			srcTypeSettings: () => createCommandParams(),
			jdbcElements: () => createCommandParams(),
			settingElements: () => createCommandParams(),
		},
		convertResult: {
			name: "convertResult",
			prefix: "convert",
			elements: [],
			jdbc: {
				name: "",
				prefix: "",
				elements: [],
			},
		},
	};

	const mockCompareParams: CompareParams = {
		elements: [],
		newData: {
			name: "compareNewData",
			prefix: "compare",
			elements: [],
			srcType: () => "csv",
			srcElements: () => createCommandParams(),
			srcTypeSettings: () => createCommandParams(),
			jdbcElements: () => createCommandParams(),
			settingElements: () => createCommandParams(),
		},
		oldData: {
			name: "compareOldData",
			prefix: "compare",
			elements: [],
			srcType: () => "csv",
			srcElements: () => createCommandParams(),
			srcTypeSettings: () => createCommandParams(),
			jdbcElements: () => createCommandParams(),
			settingElements: () => createCommandParams(),
		},
		imageOption: {
			name: "",
			prefix: "",
			elements: [],
		},
		convertResult: {
			name: "compareConvertResult",
			prefix: "compare",
			elements: [],
			jdbc: {
				name: "",
				prefix: "",
				elements: [],
			},
		},
		expectData: {
			name: "compareExpectData",
			prefix: "compare",
			elements: [],
			srcType: () => "csv",
			srcElements: () => createCommandParams(),
			srcTypeSettings: () => createCommandParams(),
			jdbcElements: () => createCommandParams(),
			settingElements: () => createCommandParams(),
		},
	};

	const mockGenerateParams: GenerateParams = {
		elements: [],
		srcData: {
			name: "generateSrcData",
			prefix: "generate",
			elements: [],
			srcType: () => "csv",
			srcElements: () => createCommandParams(),
			srcTypeSettings: () => createCommandParams(),
			jdbcElements: () => createCommandParams(),
			settingElements: () => createCommandParams(),
		},
		templateOption: {
			name: "",
			prefix: "",
			elements: [],
		},
	};

	const mockRunParams: RunParams = {
		elements: [],
		srcData: {
			name: "runSrcData",
			prefix: "run",
			elements: [],
			srcType: () => "csv",
			srcElements: () => createCommandParams(),
			srcTypeSettings: () => createCommandParams(),
			jdbcElements: () => createCommandParams(),
			settingElements: () => createCommandParams(),
		},
		templateOption: {
			name: "",
			prefix: "",
			elements: [],
		},
		jdbcOption: {
			name: "",
			prefix: "",
			elements: [],
		},
	};

	const mockParameterizeParams: ParameterizeParams = {
		elements: [],
		paramData: {
			name: "parameterizeParamData",
			prefix: "parameterize",
			elements: [],
			srcType: () => "csv",
			srcElements: () => createCommandParams(),
			srcTypeSettings: () => createCommandParams(),
			jdbcElements: () => createCommandParams(),
			settingElements: () => createCommandParams(),
		},
		templateOption: {
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
