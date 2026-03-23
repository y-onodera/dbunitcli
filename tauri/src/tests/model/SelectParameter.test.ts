import type {
	CommandParams,
	DatasetSource,
	JdbcOption,
	SettingElements,
	SrcElements,
	TemplateOption,
} from "../../model/CommandParam";
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
const createSrcElements = () => createCommandParams() as unknown as SrcElements;
const createSettingElements = () =>
	createCommandParams() as unknown as SettingElements;
const createTemplateOption = () =>
	createCommandParams() as unknown as TemplateOption;
const createJdbcOption = () => createCommandParams() as unknown as JdbcOption;
const createDatasetSource = (name: string, prefix: string): DatasetSource => ({
	name,
	prefix,
	elements: [],
	srcType: () => "csv",
	srcElements: createSrcElements,
	srcTypeSettings: () => createCommandParams(),
	jdbcElements: () => createCommandParams(),
	settingElements: createSettingElements,
	jdbcOption: createJdbcOption,
	templateOption: createTemplateOption,
});

describe("SelectParameterクラス", () => {
	const mockConvertParams: ConvertParams = {
		srcData: createDatasetSource("convertSrcData", "convert"),
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
		newData: createDatasetSource("compareNewData", "compare"),
		oldData: createDatasetSource("compareOldData", "compare"),
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
		expectData: createDatasetSource("compareExpectData", "compare"),
	};

	const mockGenerateParams: GenerateParams = {
		elements: [],
		srcData: createDatasetSource("generateSrcData", "generate"),
		templateOption: createTemplateOption(),
	};

	const mockRunParams: RunParams = {
		elements: [],
		srcData: createDatasetSource("runSrcData", "run"),
		templateOption: createTemplateOption(),
		jdbcOption: createJdbcOption(),
	};

	const mockParameterizeParams: ParameterizeParams = {
		elements: [],
		paramData: createDatasetSource("parameterizeParamData", "parameterize"),
		templateOption: createTemplateOption(),
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
