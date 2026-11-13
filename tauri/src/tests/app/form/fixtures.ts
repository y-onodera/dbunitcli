import type {
	CommandOption,
	DatasetSource,
} from "../../../model/CommandOption";
import type {
	CompareOptions,
	ConvertOptions,
	GenerateOptions,
	ParameterizeOptions,
	RunOptions,
	ScaffoldOptions,
} from "../../../model/SelectParameter";
import type { DefaultPath } from "../../../model/WorkspaceResources";

function makeElement(
	name: string,
	type: string,
	value: string,
	defaultPath: DefaultPath,
	required: boolean,
	selectOption: string[] = [],
): CommandOption {
	return {
		name,
		value,
		attribute: { type, required, selectOption, defaultPath },
		optional: false,
	};
}

// 未入力時に使われる値（attribute.defaultValue）を持つテキスト項目
function makeTextWithDefault(
	name: string,
	value: string,
	defaultValue: string,
): CommandOption {
	const element = makeElement(name, "TEXT", value, "WORKSPACE", false);
	return { ...element, attribute: { ...element.attribute, defaultValue } };
}

const SRC_TYPE_OPTIONS = [
	"table",
	"sql",
	"file",
	"dir",
	"csv",
	"csvq",
	"reg",
	"fixed",
	"xls",
	"xlsx",
];

const EXPECT_SRC_TYPE_OPTIONS = ["none", "sql", "csv", "csvq", "xls", "xlsx"];

const PARAMETERIZE_SRC_TYPE_OPTIONS = ["none", ...SRC_TYPE_OPTIONS];

const TARGET_TYPE_OPTIONS = ["data", "image", "pdf"];

function makeTemplateOption() {
	return {
		prefix: "template",
		encoding: makeTextWithDefault("encoding", "", "UTF-8"),
		templateGroup: makeElement("templateGroup", "FILE", "", "TEMPLATE", false),
		templateParameterAttribute: makeTextWithDefault(
			"templateParameterAttribute",
			"",
			"param",
		),
		templateVarStart: makeTextWithDefault("templateVarStart", "", "$"),
		templateVarStop: makeTextWithDefault("templateVarStop", "", "$"),
	};
}

function makeJxlsTemplateOption(includeFormulaProcess: boolean) {
	return {
		...makeTemplateOption(),
		...(includeFormulaProcess
			? {
					formulaProcess: makeElement(
						"formulaProcess",
						"FLG",
						"true",
						"WORKSPACE",
						false,
					),
				}
			: {}),
		evaluateFormulas: makeElement(
			"evaluateFormulas",
			"FLG",
			"true",
			"WORKSPACE",
			false,
		),
		forceFormulaRecalc: makeElement(
			"forceFormulaRecalc",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		fastFormulaProcess: makeElement(
			"fastFormulaProcess",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		deleteBlankCells: makeElement(
			"deleteBlankCells",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
	};
}

function makeCsvSrcData(
	prefix: string,
	src: string,
	srcTypeOptions = SRC_TYPE_OPTIONS,
) {
	return {
		prefix,
		srcType: makeElement(
			"srcType",
			"ENUM",
			"csv",
			"WORKSPACE",
			false,
			srcTypeOptions,
		),
		src: makeElement("src", "FILE_OR_DIR", src, "DATASET", true),
		encoding: makeTextWithDefault("encoding", "", "UTF-8"),
		recursive: makeElement("recursive", "FLG", "false", "WORKSPACE", false),
		regInclude: makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
		regExclude: makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
		extension: makeTextWithDefault("extension", "", "csv"),
		headerName: makeElement("headerName", "TEXT", "", "WORKSPACE", false),
		startRow: makeTextWithDefault("startRow", "", "1"),
		addFileInfo: makeElement("addFileInfo", "FLG", "false", "WORKSPACE", false),
		delimiter: makeTextWithDefault("delimiter", "", ","),
		ignoreQuoted: makeElement(
			"ignoreQuoted",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		setting: makeElement("setting", "FILE", "", "SETTING", false),
		settingEncoding: makeTextWithDefault("settingEncoding", "", "UTF-8"),
		settingTargetOnly: makeElement(
			"settingTargetOnly",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		regTableInclude: makeElement(
			"regTableInclude",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
		regTableExclude: makeElement(
			"regTableExclude",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
		loadData: makeElement("loadData", "FLG", "true", "WORKSPACE", false),
		loadMetaData: makeElement(
			"loadMetaData",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		includeMetaData: makeElement(
			"includeMetaData",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
	};
}

function makeXlsxSrcData() {
	return {
		prefix: "src",
		srcType: makeElement(
			"srcType",
			"ENUM",
			"xlsx",
			"WORKSPACE",
			false,
			SRC_TYPE_OPTIONS,
		),
		src: makeElement("src", "FILE_OR_DIR", "", "DATASET", true),
		recursive: makeElement("recursive", "FLG", "false", "WORKSPACE", false),
		regInclude: makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
		regExclude: makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
		extension: makeTextWithDefault("extension", "", "xlsx"),
		headerName: makeElement("headerName", "TEXT", "", "WORKSPACE", false),
		startRow: makeTextWithDefault("startRow", "", "1"),
		addFileInfo: makeElement("addFileInfo", "FLG", "false", "WORKSPACE", false),
		xlsxSchema: makeElement("xlsxSchema", "FILE", "", "XLSX_SCHEMA", false),
		setting: makeElement("setting", "FILE", "", "SETTING", false),
		settingEncoding: makeTextWithDefault("settingEncoding", "", "UTF-8"),
		settingTargetOnly: makeElement(
			"settingTargetOnly",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		regTableInclude: makeElement(
			"regTableInclude",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
		regTableExclude: makeElement(
			"regTableExclude",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
		loadData: makeElement("loadData", "FLG", "true", "WORKSPACE", false),
		loadMetaData: makeElement(
			"loadMetaData",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		includeMetaData: makeElement(
			"includeMetaData",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
	};
}

function makeTableSrcData(prefix: string) {
	return {
		prefix,
		srcType: makeElement(
			"srcType",
			"ENUM",
			"table",
			"WORKSPACE",
			false,
			SRC_TYPE_OPTIONS,
		),
		src: makeElement("src", "FILE_OR_DIR", "", "TABLE", true),
		encoding: makeTextWithDefault("encoding", "", "UTF-8"),
		recursive: makeElement("recursive", "FLG", "false", "WORKSPACE", false),
		regInclude: makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
		regExclude: makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
		extension: makeTextWithDefault("extension", "", "(all files)"),
		headerName: makeElement("headerName", "TEXT", "", "WORKSPACE", false),
		addFileInfo: makeElement("addFileInfo", "FLG", "false", "WORKSPACE", false),
		jdbcProperties: makeElement("jdbcProperties", "FILE", "", "JDBC", false),
		jdbcUrl: makeElement("jdbcUrl", "TEXT", "", "WORKSPACE", false),
		jdbcUser: makeElement("jdbcUser", "TEXT", "", "WORKSPACE", false),
		jdbcPass: makeElement("jdbcPass", "TEXT", "", "WORKSPACE", false),
		useJdbcMetaData: makeElement(
			"useJdbcMetaData",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		templateGroup: makeElement("templateGroup", "FILE", "", "TEMPLATE", false),
		templateParameterAttribute: makeTextWithDefault(
			"templateParameterAttribute",
			"",
			"param",
		),
		templateVarStart: makeTextWithDefault("templateVarStart", "", "$"),
		templateVarStop: makeTextWithDefault("templateVarStop", "", "$"),
		setting: makeElement("setting", "FILE", "", "SETTING", false),
		settingEncoding: makeTextWithDefault("settingEncoding", "", "UTF-8"),
		settingTargetOnly: makeElement(
			"settingTargetOnly",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		regTableInclude: makeElement(
			"regTableInclude",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
		regTableExclude: makeElement(
			"regTableExclude",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
		loadData: makeElement("loadData", "FLG", "true", "WORKSPACE", false),
		loadMetaData: makeElement(
			"loadMetaData",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		includeMetaData: makeElement(
			"includeMetaData",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
	};
}

function makeImageFileSrcData(prefix: string) {
	return {
		prefix,
		srcType: makeElement("srcType", "ENUM", "file", "WORKSPACE", true, [
			"file",
		]),
		src: makeElement("src", "FILE_OR_DIR", "", "DATASET", true),
		recursive: makeElement("recursive", "FLG", "false", "WORKSPACE", false),
		regInclude: makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
		regExclude: makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
		extension: makeTextWithDefault("extension", "png", "(all files)"),
		setting: makeElement("setting", "FILE", "", "SETTING", false),
		settingEncoding: makeTextWithDefault("settingEncoding", "", "UTF-8"),
		settingTargetOnly: makeElement(
			"settingTargetOnly",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		regTableInclude: makeElement(
			"regTableInclude",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
		regTableExclude: makeElement(
			"regTableExclude",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
		loadData: makeElement("loadData", "FLG", "true", "WORKSPACE", false),
		loadMetaData: makeElement(
			"loadMetaData",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		includeMetaData: makeElement(
			"includeMetaData",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
	};
}

// parallelは並列化できるときだけレスポンスに含まれる（convertは入力・出力とも、generateはsrcが並列読込できるとき）
function parallelElement(parallel: boolean) {
	return parallel
		? { parallel: makeTextWithDefault("parallel", "", "(auto: CPU cores)") }
		: {};
}

// Compareの応答にはappendが含まれない（CompareOption#toParametersBuilder）
function makeCsvConvertResult(
	required = false,
	parallel = true,
	append = true,
) {
	return {
		prefix: "result",
		resultType: makeElement(
			"resultType",
			"ENUM",
			"csv",
			"WORKSPACE",
			required,
			required ? ["csv", "xls", "xlsx"] : ["csv", "xls", "xlsx", "table"],
		),
		result: makeElement("result", "DIR", "", "RESULT", false),
		exportEmptyTable: makeElement(
			"exportEmptyTable",
			"FLG",
			"true",
			"WORKSPACE",
			false,
		),
		exportHeader: makeElement(
			"exportHeader",
			"FLG",
			"true",
			"WORKSPACE",
			false,
		),
		...(append
			? { append: makeElement("append", "FLG", "false", "WORKSPACE", false) }
			: {}),
		outputEncoding: makeTextWithDefault("outputEncoding", "", "UTF-8"),
		...parallelElement(parallel),
	};
}

function makeXlsxConvertResult(resultValue: string, excelTable = "SHEET") {
	return {
		prefix: "result",
		resultType: makeElement("resultType", "ENUM", "xlsx", "WORKSPACE", false, [
			"csv",
			"xls",
			"xlsx",
			"table",
		]),
		result: makeElement("result", "DIR", resultValue, "RESULT", false),
		// BOOKと確定しているときはresultPathを使わないため含まれない
		...(excelTable === "BOOK"
			? {}
			: {
					resultPath: makeTextWithDefault("resultPath", "", "(parameter name)"),
				}),
		exportEmptyTable: makeElement(
			"exportEmptyTable",
			"FLG",
			"true",
			"WORKSPACE",
			false,
		),
		exportHeader: makeElement(
			"exportHeader",
			"FLG",
			"true",
			"WORKSPACE",
			false,
		),
		append: makeElement("append", "FLG", "false", "WORKSPACE", false),
		excelTable: makeElement(
			"excelTable",
			"TEXT",
			excelTable,
			"WORKSPACE",
			false,
		),
		...parallelElement(excelTable === "BOOK"),
	};
}

// convert-load-response.json をもとにしたフィクスチャ
export const convertLoadResponseFixture = {
	srcData: makeCsvSrcData(
		"src",
		"src/test/resources/workspace/sample/resources/src/csv",
	),
	convertResult: makeXlsxConvertResult("target/convert/result"),
} as ConvertOptions;

// compare-load-response.json をもとにしたフィクスチャ
export const compareLoadResponseFixture = {
	prefix: "",
	targetType: makeElement(
		"targetType",
		"ENUM",
		"data",
		"WORKSPACE",
		false,
		TARGET_TYPE_OPTIONS,
	),
	createPatchSql: makeElement(
		"createPatchSql",
		"FLG",
		"false",
		"WORKSPACE",
		false,
	),
	setting: makeElement("setting", "FILE", "", "COMPARE_SETTING", false),
	settingEncoding: makeTextWithDefault("settingEncoding", "", "UTF-8"),
	newData: makeCsvSrcData("new", "resources/src/csv/multi1.csv"),
	oldData: makeCsvSrcData("old", "resources/src/csv/multi2.csv"),
	convertResult: {
		prefix: "result",
		resultType: makeElement("resultType", "ENUM", "csv", "WORKSPACE", true, [
			"csv",
			"xls",
			"xlsx",
		]),
		result: makeElement(
			"result",
			"DIR",
			"target/compare/result",
			"RESULT",
			false,
		),
		exportEmptyTable: makeElement(
			"exportEmptyTable",
			"FLG",
			"true",
			"WORKSPACE",
			false,
		),
		exportHeader: makeElement(
			"exportHeader",
			"FLG",
			"true",
			"WORKSPACE",
			false,
		),
		outputEncoding: makeTextWithDefault("outputEncoding", "", "UTF-8"),
		...parallelElement(true),
	},
	expectData: {
		prefix: "expect",
		srcType: makeElement(
			"srcType",
			"ENUM",
			"none",
			"WORKSPACE",
			false,
			EXPECT_SRC_TYPE_OPTIONS,
		),
	},
} as CompareOptions;

// generate-load-response.json をもとにしたフィクスチャ
export const generateLoadResponseFixture = {
	prefix: "",
	generateType: makeElement("generateType", "ENUM", "txt", "WORKSPACE", false, [
		"txt",
		"xlsx",
		"xls",
		"settings",
		"sql",
		"xlsxTemplate",
		"fixedColumnDef",
	]),
	unit: makeElement("unit", "ENUM", "record", "WORKSPACE", false, [
		"record",
		"table",
		"dataset",
	]),
	template: makeElement("template", "FILE", "sample.stg", "TEMPLATE", true),
	...parallelElement(true),
	result: makeElement(
		"result",
		"DIR",
		"target/generate/result",
		"RESULT",
		false,
	),
	resultPath: makeTextWithDefault("resultPath", "", "(parameter name)"),
	outputEncoding: makeTextWithDefault("outputEncoding", "", "UTF-8"),
	srcData: makeCsvSrcData("src", "resources/src/csv"),
	templateOption: makeTemplateOption(),
} as GenerateOptions;

// Run の sidecar レスポンス共通の srcData（src のみ値が異なる）
function makeRunSrcData(src: string) {
	return {
		prefix: "src",
		src: makeElement("src", "FILE_OR_DIR", src, "DATASET", true),
		recursive: makeElement("recursive", "FLG", "false", "WORKSPACE", false),
		regInclude: makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
		regExclude: makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
		setting: makeElement("setting", "FILE", "", "SETTING", false),
		settingEncoding: makeTextWithDefault("settingEncoding", "", "UTF-8"),
		settingTargetOnly: makeElement(
			"settingTargetOnly",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		regTableInclude: makeElement(
			"regTableInclude",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
		regTableExclude: makeElement(
			"regTableExclude",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
	};
}

// run-load-response.json をもとにしたフィクスチャ
export const runLoadResponseFixture = {
	prefix: "",
	scriptType: makeElement("scriptType", "ENUM", "sql", "WORKSPACE", false, [
		"cmd",
		"bat",
		"sql",
		"ant",
	]),
	parallel: makeTextWithDefault("parallel", "", "1"),
	srcData: makeRunSrcData("resources/sql/sample.sql"),
	templateOption: makeTemplateOption(),
	jdbcOption: {
		prefix: "jdbc",
		jdbcProperties: makeElement("jdbcProperties", "FILE", "", "JDBC", false),
		jdbcUrl: makeElement("jdbcUrl", "TEXT", "", "WORKSPACE", false),
		jdbcUser: makeElement("jdbcUser", "TEXT", "", "WORKSPACE", false),
		jdbcPass: makeElement("jdbcPass", "TEXT", "", "WORKSPACE", false),
	},
} as RunOptions;

// parameterize-load-response.json をもとにしたフィクスチャ
export const parameterizeLoadResponseFixture = {
	prefix: "",
	unit: makeElement("unit", "ENUM", "record", "WORKSPACE", false, [
		"record",
		"table",
		"dataset",
	]),
	parameterize: makeElement("parameterize", "FLG", "true", "WORKSPACE", false),
	ignoreFail: makeElement("ignoreFail", "FLG", "false", "WORKSPACE", false),
	parallel: makeTextWithDefault("parallel", "", "1"),
	cmd: makeElement("cmd", "TEXT", "convert", "WORKSPACE", false),
	cmdParam: makeElement("cmdParam", "TEXT", "", "WORKSPACE", false),
	template: makeElement(
		"template",
		"FILE",
		"csvToXlsx.txt",
		"PARAMETERIZE_TEMPLATE",
		false,
	),
	paramData: makeCsvSrcData(
		"param",
		"csvToXlsx.csv",
		PARAMETERIZE_SRC_TYPE_OPTIONS,
	),
	templateOption: makeTemplateOption(),
} as ParameterizeOptions;

// --- refresh レスポンスフィクスチャ ---

// convert-refresh-srcType-xlsx-response.json をもとにしたフィクスチャ
export const convertRefreshSrcTypeXlsxResponseFixture = {
	srcData: makeXlsxSrcData(),
	convertResult: makeCsvConvertResult(),
} as ConvertOptions;

// convert-refresh-srcType-table-response.json をもとにしたフィクスチャ
export const convertRefreshSrcTypeTableResponseFixture = {
	srcData: makeTableSrcData("src"),
	convertResult: makeCsvConvertResult(),
} as ConvertOptions;

// convert-refresh-resultType-xlsx-response.json をもとにしたフィクスチャ
export const convertRefreshResultTypeXlsxResponseFixture = {
	srcData: makeCsvSrcData("src", ""),
	convertResult: makeXlsxConvertResult(""),
} as ConvertOptions;

// convert-refresh-resultType-xlsx-response.json でexcelTableをBOOKにしてrefreshしたレスポンス相当
export const convertRefreshExcelTableBookResponseFixture = {
	srcData: makeCsvSrcData("src", ""),
	convertResult: makeXlsxConvertResult("", "BOOK"),
} as ConvertOptions;

// excelTableに環境変数を指定してrefreshしたレスポンス相当
export const convertRefreshExcelTableEnvResponseFixture = {
	srcData: makeCsvSrcData("src", ""),
	convertResult: makeXlsxConvertResult("", "$EXCEL_TABLE"),
} as ConvertOptions;

// compare-refresh-targetType-image-response.json をもとにしたフィクスチャ
export const compareRefreshTargetTypeImageResponseFixture = {
	prefix: "",
	targetType: makeElement(
		"targetType",
		"ENUM",
		"image",
		"WORKSPACE",
		false,
		TARGET_TYPE_OPTIONS,
	),
	createPatchSql: makeElement(
		"createPatchSql",
		"FLG",
		"false",
		"WORKSPACE",
		false,
	),
	setting: makeElement("setting", "FILE", "", "COMPARE_SETTING", false),
	settingEncoding: makeTextWithDefault("settingEncoding", "", "UTF-8"),
	imageOption: {
		prefix: "image",
		threshold: makeTextWithDefault("threshold", "", "5"),
		pixelToleranceLevel: makeTextWithDefault("pixelToleranceLevel", "", "0.01"),
		allowingPercentOfDifferentPixels: makeTextWithDefault(
			"allowingPercentOfDifferentPixels",
			"",
			"0.01",
		),
		rectangleLineWidth: makeTextWithDefault("rectangleLineWidth", "", "1"),
		minimalRectangleSize: makeTextWithDefault("minimalRectangleSize", "", "1"),
		maximalRectangleCount: makeTextWithDefault(
			"maximalRectangleCount",
			"",
			"-1",
		),
		excludedAreas: makeElement("excludedAreas", "TEXT", "", "WORKSPACE", false),
		drawExcludedRectangles: makeElement(
			"drawExcludedRectangles",
			"FLG",
			"true",
			"WORKSPACE",
			false,
		),
		fillExcludedRectangles: makeElement(
			"fillExcludedRectangles",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		percentOpacityExcludedRectangles: makeTextWithDefault(
			"percentOpacityExcludedRectangles",
			"",
			"20.0",
		),
		excludedRectangleColor: makeElement(
			"excludedRectangleColor",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
		fillDifferenceRectangles: makeElement(
			"fillDifferenceRectangles",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		percentOpacityDifferenceRectangles: makeTextWithDefault(
			"percentOpacityDifferenceRectangles",
			"",
			"20.0",
		),
		differenceRectangleColor: makeElement(
			"differenceRectangleColor",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
	},
	newData: makeImageFileSrcData("new"),
	oldData: makeImageFileSrcData("old"),
	convertResult: makeCsvConvertResult(true, false, false),
	expectData: {
		prefix: "expect",
		srcType: makeElement(
			"srcType",
			"ENUM",
			"none",
			"WORKSPACE",
			false,
			EXPECT_SRC_TYPE_OPTIONS,
		),
	},
} as CompareOptions;

// compare-refresh-newSrcType-table-response.json をもとにしたフィクスチャ
export const compareRefreshNewSrcTypeTableResponseFixture = {
	prefix: "",
	targetType: makeElement(
		"targetType",
		"ENUM",
		"data",
		"WORKSPACE",
		false,
		TARGET_TYPE_OPTIONS,
	),
	createPatchSql: makeElement(
		"createPatchSql",
		"FLG",
		"false",
		"WORKSPACE",
		false,
	),
	setting: makeElement("setting", "FILE", "", "COMPARE_SETTING", false),
	settingEncoding: makeTextWithDefault("settingEncoding", "", "UTF-8"),
	newData: makeTableSrcData("new"),
	oldData: makeCsvSrcData("old", ""),
	convertResult: makeCsvConvertResult(true, true, false),
	expectData: {
		prefix: "expect",
		srcType: makeElement(
			"srcType",
			"ENUM",
			"none",
			"WORKSPACE",
			false,
			EXPECT_SRC_TYPE_OPTIONS,
		),
	},
} as CompareOptions;

// compare-refresh-expectSrcType-csv-response.json をもとにしたフィクスチャ
export const compareRefreshExpectSrcTypeCsvResponseFixture = {
	prefix: "",
	targetType: makeElement(
		"targetType",
		"ENUM",
		"data",
		"WORKSPACE",
		false,
		TARGET_TYPE_OPTIONS,
	),
	createPatchSql: makeElement(
		"createPatchSql",
		"FLG",
		"false",
		"WORKSPACE",
		false,
	),
	setting: makeElement("setting", "FILE", "", "COMPARE_SETTING", false),
	settingEncoding: makeTextWithDefault("settingEncoding", "", "UTF-8"),
	newData: makeCsvSrcData("new", ""),
	oldData: makeCsvSrcData("old", ""),
	convertResult: makeCsvConvertResult(true, true, false),
	expectData: makeCsvSrcData("expect", "", EXPECT_SRC_TYPE_OPTIONS),
} as CompareOptions;

// generate-refresh-srcType-table-response.json をもとにしたフィクスチャ
export const generateRefreshSrcTypeTableResponseFixture = {
	prefix: "",
	generateType: makeElement("generateType", "ENUM", "txt", "WORKSPACE", false, [
		"txt",
		"xlsx",
		"xls",
		"settings",
		"sql",
		"xlsxTemplate",
		"fixedColumnDef",
	]),
	unit: makeElement("unit", "ENUM", "record", "WORKSPACE", false, [
		"record",
		"table",
		"dataset",
	]),
	template: makeElement("template", "FILE", "", "TEMPLATE", true),
	...parallelElement(true),
	result: makeElement("result", "DIR", "", "RESULT", false),
	resultPath: makeTextWithDefault("resultPath", "", "(parameter name)"),
	outputEncoding: makeTextWithDefault("outputEncoding", "", "UTF-8"),
	srcData: makeTableSrcData("src"),
	templateOption: makeTemplateOption(),
} as GenerateOptions;

// run-refresh-scriptType-cmd-response.json をもとにしたフィクスチャ
export const runRefreshScriptTypeCmdResponseFixture = {
	prefix: "",
	scriptType: makeElement("scriptType", "ENUM", "cmd", "WORKSPACE", false, [
		"cmd",
		"bat",
		"sql",
		"ant",
	]),
	parallel: makeTextWithDefault("parallel", "", "1"),
	baseDir: makeElement("baseDir", "TEXT", "", "WORKSPACE", false),
	srcData: makeRunSrcData(""),
} as RunOptions;

// run-refresh-scriptType-ant-response.json をもとにしたフィクスチャ（cmd のレスポンスに antTarget を追加した構造）
export const runRefreshScriptTypeAntResponseFixture = {
	...runRefreshScriptTypeCmdResponseFixture,
	scriptType: makeElement("scriptType", "ENUM", "ant", "WORKSPACE", false, [
		"cmd",
		"bat",
		"sql",
		"ant",
	]),
	antTarget: makeTextWithDefault("antTarget", "", "(default target)"),
} as RunOptions;

// run-refresh-scriptType-sql-response.json をもとにしたフィクスチャ
export const runRefreshScriptTypeSqlResponseFixture = {
	prefix: "",
	scriptType: makeElement("scriptType", "ENUM", "sql", "WORKSPACE", false, [
		"cmd",
		"bat",
		"sql",
		"ant",
	]),
	parallel: makeTextWithDefault("parallel", "", "1"),
	srcData: makeRunSrcData(""),
	templateOption: makeTemplateOption(),
	jdbcOption: {
		prefix: "jdbc",
		jdbcProperties: makeElement("jdbcProperties", "FILE", "", "JDBC", false),
		jdbcUrl: makeElement("jdbcUrl", "TEXT", "", "WORKSPACE", false),
		jdbcUser: makeElement("jdbcUser", "TEXT", "", "WORKSPACE", false),
		jdbcPass: makeElement("jdbcPass", "TEXT", "", "WORKSPACE", false),
	},
} as RunOptions;

const GENERATE_TYPE_OPTIONS = [
	"txt",
	"xlsx",
	"xls",
	"settings",
	"sql",
	"xlsxTemplate",
];
const UNIT_OPTIONS = ["record", "table", "dataset"];

function makeGenerateJxlsFixture(
	generateTypeValue: "xlsx" | "xls",
	includeFormulaProcess: boolean,
): GenerateOptions {
	return {
		prefix: "",
		generateType: makeElement(
			"generateType",
			"ENUM",
			generateTypeValue,
			"WORKSPACE",
			false,
			GENERATE_TYPE_OPTIONS,
		),
		unit: makeElement(
			"unit",
			"ENUM",
			"record",
			"WORKSPACE",
			false,
			UNIT_OPTIONS,
		),
		template: makeElement("template", "FILE", "", "TEMPLATE", true),
		...parallelElement(true),
		result: makeElement("result", "DIR", "", "RESULT", false),
		resultPath: makeTextWithDefault("resultPath", "", "(parameter name)"),
		srcData: makeCsvSrcData("src", ""),
		templateOption: makeJxlsTemplateOption(includeFormulaProcess),
	} as GenerateOptions;
}

export const generateRefreshGenerateTypeXlsxResponseFixture =
	makeGenerateJxlsFixture("xlsx", true);

export const generateRefreshGenerateTypeXlsResponseFixture =
	makeGenerateJxlsFixture("xls", false);

// generate-refresh-generateType-sql-response.json をもとにしたフィクスチャ
export const generateRefreshGenerateTypeSqlResponseFixture = {
	prefix: "",
	generateType: makeElement(
		"generateType",
		"ENUM",
		"sql",
		"WORKSPACE",
		false,
		GENERATE_TYPE_OPTIONS,
	),
	...parallelElement(true),
	result: makeElement("result", "DIR", "", "RESULT", false),
	resultPath: makeTextWithDefault("resultPath", "", "(parameter name)"),
	outputEncoding: makeTextWithDefault("outputEncoding", "", "UTF-8"),
	commit: makeElement("commit", "FLG", "false", "WORKSPACE", false),
	op: makeElement("op", "ENUM", "INSERT", "WORKSPACE", false, [
		"INSERT",
		"UPDATE",
		"DELETE",
		"REFRESH",
		"CLEAN_INSERT",
	]),
	sqlFilePrefix: makeElement("sqlFilePrefix", "TEXT", "", "WORKSPACE", false),
	sqlFileSuffix: makeElement("sqlFileSuffix", "TEXT", "", "WORKSPACE", false),
	srcData: makeTableSrcData("src"),
} as GenerateOptions;

// generate-refresh-xlsxTemplate-compare-response.json をもとにしたフィクスチャ
export const generateRefreshXlsxTemplateCompareResponseFixture = {
	prefix: "",
	generateType: makeElement(
		"generateType",
		"ENUM",
		"xlsxTemplate",
		"WORKSPACE",
		false,
		GENERATE_TYPE_OPTIONS,
	),
	templateType: makeElement(
		"templateType",
		"ENUM",
		"compare",
		"WORKSPACE",
		true,
		["table", "compare", "metadata"],
	),
	oldTable: makeElement("oldTable", "TEXT", "", "WORKSPACE", true),
	newTable: makeElement("newTable", "TEXT", "", "WORKSPACE", true),
	keys: makeElement("keys", "TEXT", "", "WORKSPACE", true),
	compares: makeElement("compares", "TEXT", "", "WORKSPACE", false),
	compareSheetName: makeTextWithDefault("compareSheetName", "", "compare"),
	...parallelElement(true),
	result: makeElement("result", "DIR", "", "RESULT", false),
	resultPath: makeTextWithDefault("resultPath", "", "(parameter name)"),
	srcData: makeCsvSrcData("src", ""),
} as GenerateOptions;

// xlsxSchema生成ダイアログ（generate/refresh generateType=xlsxSchema）用fixture
export const generateRefreshXlsxSchemaResponseFixture = {
	prefix: "",
	generateType: makeElement(
		"generateType",
		"ENUM",
		"xlsxSchema",
		"WORKSPACE",
		false,
		GENERATE_TYPE_OPTIONS,
	),
	...parallelElement(true),
	result: makeElement("result", "DIR", "", "RESULT", false),
	resultPath: makeTextWithDefault("resultPath", "", "(parameter name)"),
	outputEncoding: makeTextWithDefault("outputEncoding", "", "UTF-8"),
	srcData: makeCsvSrcData("src", ""),
} as GenerateOptions;

// dataset-setting生成ダイアログ（generate/refresh generateType=settings）用fixture
export const generateRefreshSettingsResponseFixture = {
	prefix: "",
	generateType: makeElement(
		"generateType",
		"ENUM",
		"settings",
		"WORKSPACE",
		false,
		GENERATE_TYPE_OPTIONS,
	),
	...parallelElement(true),
	result: makeElement("result", "DIR", "", "RESULT", false),
	resultPath: makeTextWithDefault("resultPath", "", "(parameter name)"),
	outputEncoding: makeTextWithDefault("outputEncoding", "", "UTF-8"),
	srcData: makeCsvSrcData("src", ""),
} as GenerateOptions;

// fixed-column-def生成ダイアログ（generate/refresh generateType=fixedColumnDef）用fixture
export const generateRefreshFixedColumnDefResponseFixture = {
	prefix: "",
	generateType: makeElement(
		"generateType",
		"ENUM",
		"fixedColumnDef",
		"WORKSPACE",
		false,
		GENERATE_TYPE_OPTIONS,
	),
	...parallelElement(true),
	result: makeElement("result", "DIR", "", "RESULT", false),
	resultPath: makeTextWithDefault("resultPath", "", "(parameter name)"),
	outputEncoding: makeTextWithDefault("outputEncoding", "", "UTF-8"),
	srcData: makeCsvSrcData("src", ""),
} as GenerateOptions;

// sidecarのscaffold-refresh-*-response.jsonと同期させたscaffoldダイアログ用fixture
const SCAFFOLD_TARGET_OPTIONS = [
	"ddl",
	"javaBean",
	"xlsxSchema",
	"fixedColumnDef",
	"parameter",
];

const SCAFFOLD_DATASET_TYPE_OPTIONS = ["csv", "xls", "xlsx", "table", "fixed"];

function makeScaffoldOptionsBase(target: string) {
	return {
		prefix: "",
		result: makeElement("result", "DIR", "result", "RESULT", false),
		target: makeElement(
			"target",
			"ENUM",
			target,
			"WORKSPACE",
			true,
			SCAFFOLD_TARGET_OPTIONS,
		),
		unitSetting: makeElement("unitSetting", "TEXT", "", "WORKSPACE", false),
		template: makeElement("template", "TEXT", "", "WORKSPACE", false),
		parameter: makeElement("parameter", "TEXT", "", "WORKSPACE", false),
		commandType: makeElement("commandType", "TEXT", "", "WORKSPACE", false),
		datasetType: makeElement(
			"datasetType",
			"ENUM",
			"csv",
			"WORKSPACE",
			false,
			SCAFFOLD_DATASET_TYPE_OPTIONS,
		),
		datasetEncoding: makeTextWithDefault("datasetEncoding", "", "UTF-8"),
		dataset: scaffoldDataset(),
	};
}

// scaffoldのdatasetはloadMetaDataを持たない
function scaffoldDataset(): DatasetSource {
	const { loadMetaData: _, ...dataset } = makeCsvSrcData(
		"dataset",
		"resources/src/csv",
		["none", ...SRC_TYPE_OPTIONS],
	);
	return dataset as DatasetSource;
}

export const scaffoldRefreshTargetDdlResponseFixture = makeScaffoldOptionsBase(
	"ddl",
) as ScaffoldOptions;

export const scaffoldRefreshTargetFixedColumnDefResponseFixture = {
	...makeScaffoldOptionsBase("fixedColumnDef"),
	fixedLength: makeElement("fixedLength", "TEXT", "", "WORKSPACE", false),
	defaultLength: makeTextWithDefault("defaultLength", "", "10"),
	align: makeTextWithDefault("align", "", "left"),
} as ScaffoldOptions;

export const scaffoldExecResponseFixture: Record<string, string> = {
	"-generateType": "txt",
	"-unit": "table",
	"-template": "scaffoldTpl.txt",
	"-template.templateGroup": "scaffoldTpl.stg",
	"-resultPath": "$param.tableName$.sql",
	"-unitSetting": "scaffoldUnit.json",
	"-src.src": "src",
	"-src.srcType": "csv",
	"-src.encoding": "UTF-8",
};
