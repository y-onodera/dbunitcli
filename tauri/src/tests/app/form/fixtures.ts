import type { CommandOption, DefaultPath } from "../../../model/CommandOption";
import type {
	CompareOptions,
	ConvertOptions,
	GenerateOptions,
	ParameterizeOptions,
	RunOptions,
} from "../../../model/SelectParameter";

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
		encoding: makeElement("encoding", "TEXT", "UTF-8", "WORKSPACE", false),
		templateGroup: makeElement("templateGroup", "FILE", "", "TEMPLATE", false),
		templateParameterAttribute: makeElement(
			"templateParameterAttribute",
			"TEXT",
			"param",
			"WORKSPACE",
			false,
		),
		templateVarStart: makeElement(
			"templateVarStart",
			"TEXT",
			"$",
			"WORKSPACE",
			false,
		),
		templateVarStop: makeElement(
			"templateVarStop",
			"TEXT",
			"$",
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
		encoding: makeElement("encoding", "TEXT", "UTF-8", "WORKSPACE", false),
		recursive: makeElement("recursive", "FLG", "false", "WORKSPACE", false),
		regInclude: makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
		regExclude: makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
		extension: makeElement("extension", "TEXT", "", "WORKSPACE", false),
		headerName: makeElement("headerName", "TEXT", "", "WORKSPACE", false),
		startRow: makeElement("startRow", "TEXT", "1", "WORKSPACE", false),
		addFileInfo: makeElement("addFileInfo", "FLG", "false", "WORKSPACE", false),
		delimiter: makeElement("delimiter", "TEXT", ",", "WORKSPACE", false),
		ignoreQuoted: makeElement(
			"ignoreQuoted",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
		setting: makeElement("setting", "FILE", "", "SETTING", false),
		settingEncoding: makeElement(
			"settingEncoding",
			"TEXT",
			"UTF-8",
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
		extension: makeElement("extension", "TEXT", "", "WORKSPACE", false),
		headerName: makeElement("headerName", "TEXT", "", "WORKSPACE", false),
		startRow: makeElement("startRow", "TEXT", "1", "WORKSPACE", false),
		addFileInfo: makeElement("addFileInfo", "FLG", "false", "WORKSPACE", false),
		xlsxSchema: makeElement("xlsxSchema", "FILE", "", "XLSX_SCHEMA", false),
		setting: makeElement("setting", "FILE", "", "SETTING", false),
		settingEncoding: makeElement(
			"settingEncoding",
			"TEXT",
			"UTF-8",
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
		src: makeElement("src", "FILE_OR_DIR", "", "DATASET", true),
		encoding: makeElement("encoding", "TEXT", "UTF-8", "WORKSPACE", false),
		recursive: makeElement("recursive", "FLG", "false", "WORKSPACE", false),
		regInclude: makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
		regExclude: makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
		extension: makeElement("extension", "TEXT", "", "WORKSPACE", false),
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
		templateParameterAttribute: makeElement(
			"templateParameterAttribute",
			"TEXT",
			"param",
			"WORKSPACE",
			false,
		),
		templateVarStart: makeElement(
			"templateVarStart",
			"TEXT",
			"$",
			"WORKSPACE",
			false,
		),
		templateVarStop: makeElement(
			"templateVarStop",
			"TEXT",
			"$",
			"WORKSPACE",
			false,
		),
		setting: makeElement("setting", "FILE", "", "SETTING", false),
		settingEncoding: makeElement(
			"settingEncoding",
			"TEXT",
			"UTF-8",
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
		extension: makeElement("extension", "TEXT", "png", "WORKSPACE", false),
		setting: makeElement("setting", "FILE", "", "SETTING", false),
		settingEncoding: makeElement(
			"settingEncoding",
			"TEXT",
			"UTF-8",
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
		includeMetaData: makeElement(
			"includeMetaData",
			"FLG",
			"false",
			"WORKSPACE",
			false,
		),
	};
}

function makeCsvConvertResult(required = false) {
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
		resultPath: makeElement("resultPath", "TEXT", "", "WORKSPACE", false),
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
		outputEncoding: makeElement(
			"outputEncoding",
			"TEXT",
			"UTF-8",
			"WORKSPACE",
			false,
		),
	};
}

function makeXlsxConvertResult(resultValue: string) {
	return {
		prefix: "result",
		resultType: makeElement("resultType", "ENUM", "xlsx", "WORKSPACE", false, [
			"csv",
			"xls",
			"xlsx",
			"table",
		]),
		result: makeElement("result", "DIR", resultValue, "RESULT", false),
		resultPath: makeElement("resultPath", "TEXT", "", "WORKSPACE", false),
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
		excelTable: makeElement("excelTable", "TEXT", "SHEET", "WORKSPACE", false),
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
	setting: makeElement("setting", "FILE", "", "SETTING", false),
	settingEncoding: makeElement(
		"settingEncoding",
		"TEXT",
		"UTF-8",
		"WORKSPACE",
		false,
	),
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
		resultPath: makeElement("resultPath", "TEXT", "", "WORKSPACE", false),
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
		outputEncoding: makeElement(
			"outputEncoding",
			"TEXT",
			"UTF-8",
			"WORKSPACE",
			false,
		),
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
	]),
	unit: makeElement("unit", "ENUM", "record", "WORKSPACE", false, [
		"record",
		"table",
		"dataset",
	]),
	template: makeElement("template", "FILE", "sample.stg", "TEMPLATE", true),
	result: makeElement(
		"result",
		"DIR",
		"target/generate/result",
		"RESULT",
		false,
	),
	resultPath: makeElement("resultPath", "TEXT", "result", "WORKSPACE", false),
	outputEncoding: makeElement(
		"outputEncoding",
		"TEXT",
		"UTF-8",
		"WORKSPACE",
		false,
	),
	srcData: makeCsvSrcData("src", "resources/src/csv"),
	templateOption: makeTemplateOption(),
} as GenerateOptions;

// run-load-response.json をもとにしたフィクスチャ
export const runLoadResponseFixture = {
	prefix: "",
	scriptType: makeElement("scriptType", "ENUM", "sql", "WORKSPACE", false, [
		"cmd",
		"bat",
		"sql",
		"ant",
	]),
	srcData: {
		prefix: "src",
		src: makeElement(
			"src",
			"FILE_OR_DIR",
			"resources/sql/sample.sql",
			"DATASET",
			true,
		),
		recursive: makeElement("recursive", "FLG", "false", "WORKSPACE", false),
		regInclude: makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
		regExclude: makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
		setting: makeElement("setting", "FILE", "", "SETTING", false),
		settingEncoding: makeElement(
			"settingEncoding",
			"TEXT",
			"UTF-8",
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
	},
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
	setting: makeElement("setting", "FILE", "", "SETTING", false),
	settingEncoding: makeElement(
		"settingEncoding",
		"TEXT",
		"UTF-8",
		"WORKSPACE",
		false,
	),
	imageOption: {
		prefix: "image",
		threshold: makeElement("threshold", "TEXT", "", "WORKSPACE", false),
		pixelToleranceLevel: makeElement(
			"pixelToleranceLevel",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
		allowingPercentOfDifferentPixels: makeElement(
			"allowingPercentOfDifferentPixels",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
		rectangleLineWidth: makeElement(
			"rectangleLineWidth",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
		minimalRectangleSize: makeElement(
			"minimalRectangleSize",
			"TEXT",
			"",
			"WORKSPACE",
			false,
		),
		maximalRectangleCount: makeElement(
			"maximalRectangleCount",
			"TEXT",
			"",
			"WORKSPACE",
			false,
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
		percentOpacityExcludedRectangles: makeElement(
			"percentOpacityExcludedRectangles",
			"TEXT",
			"",
			"WORKSPACE",
			false,
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
		percentOpacityDifferenceRectangles: makeElement(
			"percentOpacityDifferenceRectangles",
			"TEXT",
			"",
			"WORKSPACE",
			false,
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
	convertResult: makeCsvConvertResult(true),
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
	setting: makeElement("setting", "FILE", "", "SETTING", false),
	settingEncoding: makeElement(
		"settingEncoding",
		"TEXT",
		"UTF-8",
		"WORKSPACE",
		false,
	),
	newData: makeTableSrcData("new"),
	oldData: makeCsvSrcData("old", ""),
	convertResult: makeCsvConvertResult(true),
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
	setting: makeElement("setting", "FILE", "", "SETTING", false),
	settingEncoding: makeElement(
		"settingEncoding",
		"TEXT",
		"UTF-8",
		"WORKSPACE",
		false,
	),
	newData: makeCsvSrcData("new", ""),
	oldData: makeCsvSrcData("old", ""),
	convertResult: makeCsvConvertResult(true),
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
	]),
	unit: makeElement("unit", "ENUM", "record", "WORKSPACE", false, [
		"record",
		"table",
		"dataset",
	]),
	template: makeElement("template", "FILE", "", "TEMPLATE", true),
	result: makeElement("result", "DIR", "", "RESULT", false),
	resultPath: makeElement("resultPath", "TEXT", "result", "WORKSPACE", false),
	outputEncoding: makeElement(
		"outputEncoding",
		"TEXT",
		"UTF-8",
		"WORKSPACE",
		false,
	),
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
	baseDir: makeElement("baseDir", "TEXT", "", "WORKSPACE", false),
	srcData: {
		prefix: "src",
		src: makeElement("src", "FILE_OR_DIR", "", "DATASET", true),
		recursive: makeElement("recursive", "FLG", "false", "WORKSPACE", false),
		regInclude: makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
		regExclude: makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
		setting: makeElement("setting", "FILE", "", "SETTING", false),
		settingEncoding: makeElement(
			"settingEncoding",
			"TEXT",
			"UTF-8",
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
	},
} as unknown as RunOptions;

// run-refresh-scriptType-sql-response.json をもとにしたフィクスチャ
export const runRefreshScriptTypeSqlResponseFixture = {
	prefix: "",
	scriptType: makeElement("scriptType", "ENUM", "sql", "WORKSPACE", false, [
		"cmd",
		"bat",
		"sql",
		"ant",
	]),
	srcData: {
		prefix: "src",
		src: makeElement("src", "FILE_OR_DIR", "", "DATASET", true),
		recursive: makeElement("recursive", "FLG", "false", "WORKSPACE", false),
		regInclude: makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
		regExclude: makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
		setting: makeElement("setting", "FILE", "", "SETTING", false),
		settingEncoding: makeElement(
			"settingEncoding",
			"TEXT",
			"UTF-8",
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
	},
	templateOption: makeTemplateOption(),
	jdbcOption: {
		prefix: "jdbc",
		jdbcProperties: makeElement("jdbcProperties", "FILE", "", "JDBC", false),
		jdbcUrl: makeElement("jdbcUrl", "TEXT", "", "WORKSPACE", false),
		jdbcUser: makeElement("jdbcUser", "TEXT", "", "WORKSPACE", false),
		jdbcPass: makeElement("jdbcPass", "TEXT", "", "WORKSPACE", false),
	},
} as RunOptions;
