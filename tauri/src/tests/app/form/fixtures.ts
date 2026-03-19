import type { CommandParam, DefaultPath } from "../../../model/CommandParam";

// CommandParam 要素を生成するヘルパー
function makeElement(
	name: string,
	type: string,
	value: string,
	defaultPath: DefaultPath,
	required: boolean,
	selectOption: string[] = [],
): CommandParam {
	return {
		name,
		value,
		attribute: { type, required, selectOption, defaultPath },
		optional: false,
	};
}

// generate / run / parameterize で共通のテンプレートオプション要素
const templateOptionElements: CommandParam[] = [
	makeElement("encoding", "TEXT", "UTF-8", "WORKSPACE", false),
	makeElement("templateGroup", "FILE", "", "TEMPLATE", false),
	makeElement(
		"templateParameterAttribute",
		"TEXT",
		"param",
		"WORKSPACE",
		false,
	),
	makeElement("templateVarStart", "TEXT", "$", "WORKSPACE", false),
	makeElement("templateVarStop", "TEXT", "$", "WORKSPACE", false),
];

// xlsx/xls型のsrcData要素（xlsxSchemaあり、CSV系フィールドなし）
function makeXlsxSrcDataElements(): CommandParam[] {
	return [
		makeElement("srcType", "ENUM", "xlsx", "WORKSPACE", false, [
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
		]),
		makeElement("src", "FILE_OR_DIR", "", "DATASET", true),
		makeElement("recursive", "FLG", "false", "WORKSPACE", false),
		makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
		makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
		makeElement("extension", "TEXT", "", "WORKSPACE", false),
		makeElement("headerName", "TEXT", "", "WORKSPACE", false),
		makeElement("startRow", "TEXT", "1", "WORKSPACE", false),
		makeElement("addFileInfo", "FLG", "false", "WORKSPACE", false),
		makeElement("xlsxSchema", "FILE", "", "XLSX_SCHEMA", false),
		makeElement("setting", "FILE", "", "SETTING", false),
		makeElement("settingEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
		makeElement("regTableInclude", "TEXT", "", "WORKSPACE", false),
		makeElement("regTableExclude", "TEXT", "", "WORKSPACE", false),
		makeElement("loadData", "FLG", "true", "WORKSPACE", false),
		makeElement("includeMetaData", "FLG", "false", "WORKSPACE", false),
	];
}

// table型のsrcData要素（JDBCフィールド・テンプレートフィールドあり、CSV系フィールドなし）
function makeTableSrcDataElements(): CommandParam[] {
	return [
		makeElement("srcType", "ENUM", "table", "WORKSPACE", false, [
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
		]),
		makeElement("src", "FILE_OR_DIR", "", "DATASET", true),
		makeElement("encoding", "TEXT", "UTF-8", "WORKSPACE", false),
		makeElement("recursive", "FLG", "false", "WORKSPACE", false),
		makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
		makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
		makeElement("extension", "TEXT", "", "WORKSPACE", false),
		makeElement("headerName", "TEXT", "", "WORKSPACE", false),
		makeElement("addFileInfo", "FLG", "false", "WORKSPACE", false),
		makeElement("jdbcProperties", "FILE", "", "JDBC", false),
		makeElement("jdbcUrl", "TEXT", "", "WORKSPACE", false),
		makeElement("jdbcUser", "TEXT", "", "WORKSPACE", false),
		makeElement("jdbcPass", "TEXT", "", "WORKSPACE", false),
		makeElement("useJdbcMetaData", "FLG", "false", "WORKSPACE", false),
		makeElement("templateGroup", "FILE", "", "TEMPLATE", false),
		makeElement(
			"templateParameterAttribute",
			"TEXT",
			"param",
			"WORKSPACE",
			false,
		),
		makeElement("templateVarStart", "TEXT", "$", "WORKSPACE", false),
		makeElement("templateVarStop", "TEXT", "$", "WORKSPACE", false),
		makeElement("setting", "FILE", "", "SETTING", false),
		makeElement("settingEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
		makeElement("regTableInclude", "TEXT", "", "WORKSPACE", false),
		makeElement("regTableExclude", "TEXT", "", "WORKSPACE", false),
		makeElement("loadData", "FLG", "true", "WORKSPACE", false),
		makeElement("includeMetaData", "FLG", "false", "WORKSPACE", false),
	];
}

// xlsx resultType のresult要素（excelTableあり）
function makeXlsxResultElements(): CommandParam[] {
	return [
		makeElement("resultType", "ENUM", "xlsx", "WORKSPACE", false, [
			"csv",
			"xls",
			"xlsx",
			"table",
		]),
		makeElement("result", "DIR", "", "RESULT", false),
		makeElement("resultPath", "TEXT", "", "WORKSPACE", false),
		makeElement("exportEmptyTable", "FLG", "true", "WORKSPACE", false),
		makeElement("exportHeader", "FLG", "true", "WORKSPACE", false),
		makeElement("excelTable", "TEXT", "SHEET", "WORKSPACE", false),
	];
}

// csv resultType のresult要素（outputEncodingあり）
function makeCsvResultElements(required = false): CommandParam[] {
	return [
		makeElement(
			"resultType",
			"ENUM",
			"csv",
			"WORKSPACE",
			required,
			required ? ["csv", "xls", "xlsx"] : ["csv", "xls", "xlsx", "table"],
		),
		makeElement("result", "DIR", "", "RESULT", false),
		makeElement("resultPath", "TEXT", "", "WORKSPACE", false),
		makeElement("exportEmptyTable", "FLG", "true", "WORKSPACE", false),
		makeElement("exportHeader", "FLG", "true", "WORKSPACE", false),
		makeElement("outputEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
	];
}

// convert / compare / generate / parameterize srcData の共通 CSV 要素
function makeCsvSrcDataElements(src: string): CommandParam[] {
	return [
		makeElement("srcType", "ENUM", "csv", "WORKSPACE", false, [
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
		]),
		makeElement("src", "FILE_OR_DIR", src, "DATASET", true),
		makeElement("encoding", "TEXT", "UTF-8", "WORKSPACE", false),
		makeElement("recursive", "FLG", "false", "WORKSPACE", false),
		makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
		makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
		makeElement("extension", "TEXT", "", "WORKSPACE", false),
		makeElement("headerName", "TEXT", "", "WORKSPACE", false),
		makeElement("startRow", "TEXT", "1", "WORKSPACE", false),
		makeElement("addFileInfo", "FLG", "false", "WORKSPACE", false),
		makeElement("delimiter", "TEXT", ",", "WORKSPACE", false),
		makeElement("ignoreQuoted", "FLG", "false", "WORKSPACE", false),
		makeElement("setting", "FILE", "", "SETTING", false),
		makeElement("settingEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
		makeElement("regTableInclude", "TEXT", "", "WORKSPACE", false),
		makeElement("regTableExclude", "TEXT", "", "WORKSPACE", false),
		makeElement("loadData", "FLG", "true", "WORKSPACE", false),
		makeElement("includeMetaData", "FLG", "false", "WORKSPACE", false),
	];
}

// convert-load-response.json をもとにしたフィクスチャ
export const convertLoadResponseFixture = {
	prefix: "",
	elements: [] as CommandParam[],
	srcData: {
		prefix: "src",
		elements: makeCsvSrcDataElements(
			"src/test/resources/workspace/sample/resources/src/csv",
		),
	},
	convertResult: {
		prefix: "result",
		elements: [
			makeElement("resultType", "ENUM", "xlsx", "WORKSPACE", false, [
				"csv",
				"xls",
				"xlsx",
				"table",
			]),
			makeElement("result", "DIR", "target/convert/result", "RESULT", false),
			makeElement("resultPath", "TEXT", "", "WORKSPACE", false),
			makeElement("exportEmptyTable", "FLG", "true", "WORKSPACE", false),
			makeElement("exportHeader", "FLG", "true", "WORKSPACE", false),
			makeElement("excelTable", "TEXT", "SHEET", "WORKSPACE", false),
		],
	},
};

// compare-load-response.json をもとにしたフィクスチャ
export const compareLoadResponseFixture = {
	prefix: "",
	elements: [
		makeElement("targetType", "ENUM", "data", "WORKSPACE", false, [
			"data",
			"image",
			"pdf",
		]),
		makeElement("setting", "FILE", "", "SETTING", false),
		makeElement("settingEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
	],
	newData: {
		prefix: "new",
		elements: makeCsvSrcDataElements("resources/src/csv/multi1.csv"),
	},
	oldData: {
		prefix: "old",
		elements: makeCsvSrcDataElements("resources/src/csv/multi2.csv"),
	},
	convertResult: {
		prefix: "result",
		elements: [
			makeElement("resultType", "ENUM", "csv", "WORKSPACE", true, [
				"csv",
				"xls",
				"xlsx",
			]),
			makeElement("result", "DIR", "target/compare/result", "RESULT", false),
			makeElement("resultPath", "TEXT", "", "WORKSPACE", false),
			makeElement("exportEmptyTable", "FLG", "true", "WORKSPACE", false),
			makeElement("exportHeader", "FLG", "true", "WORKSPACE", false),
			makeElement("outputEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
		],
	},
	expectData: {
		prefix: "expect",
		elements: [
			makeElement("srcType", "ENUM", "none", "WORKSPACE", false, [
				"none",
				"sql",
				"csv",
				"csvq",
				"xls",
				"xlsx",
			]),
		],
	},
};

// generate-load-response.json をもとにしたフィクスチャ
export const generateLoadResponseFixture = {
	prefix: "",
	elements: [
		makeElement("generateType", "ENUM", "txt", "WORKSPACE", false, [
			"txt",
			"xlsx",
			"xls",
			"settings",
			"sql",
			"xlsxTemplate",
		]),
		makeElement("unit", "ENUM", "record", "WORKSPACE", false, [
			"record",
			"table",
			"dataset",
		]),
		makeElement("template", "FILE", "sample.stg", "TEMPLATE", true),
		makeElement("result", "DIR", "target/generate/result", "RESULT", false),
		makeElement("resultPath", "TEXT", "result", "WORKSPACE", false),
		makeElement("outputEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
	],
	srcData: {
		prefix: "src",
		elements: makeCsvSrcDataElements("resources/src/csv"),
	},
	templateOption: {
		prefix: "template",
		elements: templateOptionElements,
	},
};

// run-load-response.json をもとにしたフィクスチャ
export const runLoadResponseFixture = {
	prefix: "",
	elements: [
		makeElement("scriptType", "ENUM", "sql", "WORKSPACE", false, [
			"cmd",
			"bat",
			"sql",
			"ant",
		]),
	],
	srcData: {
		prefix: "src",
		elements: [
			makeElement(
				"src",
				"FILE_OR_DIR",
				"resources/sql/sample.sql",
				"DATASET",
				true,
			),
			makeElement("recursive", "FLG", "false", "WORKSPACE", false),
			makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
			makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
			makeElement("setting", "FILE", "", "SETTING", false),
			makeElement("settingEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
			makeElement("regTableInclude", "TEXT", "", "WORKSPACE", false),
			makeElement("regTableExclude", "TEXT", "", "WORKSPACE", false),
		],
	},
	templateOption: {
		prefix: "template",
		elements: templateOptionElements,
	},
	jdbcOption: {
		prefix: "jdbc",
		elements: [
			makeElement("jdbcProperties", "FILE", "", "JDBC", false),
			makeElement("jdbcUrl", "TEXT", "", "WORKSPACE", false),
			makeElement("jdbcUser", "TEXT", "", "WORKSPACE", false),
			makeElement("jdbcPass", "TEXT", "", "WORKSPACE", false),
		],
	},
};

// parameterize-load-response.json をもとにしたフィクスチャ
export const parameterizeLoadResponseFixture = {
	prefix: "",
	elements: [
		makeElement("unit", "ENUM", "record", "WORKSPACE", false, [
			"record",
			"table",
			"dataset",
		]),
		makeElement("parameterize", "FLG", "true", "WORKSPACE", false),
		makeElement("ignoreFail", "FLG", "false", "WORKSPACE", false),
		makeElement("cmd", "TEXT", "convert", "WORKSPACE", false),
		makeElement("cmdParam", "TEXT", "", "WORKSPACE", false),
		makeElement(
			"template",
			"FILE",
			"csvToXlsx.txt",
			"PARAMETERIZE_TEMPLATE",
			false,
		),
	],
	paramData: {
		prefix: "param",
		elements: makeCsvSrcDataElements("csvToXlsx.csv"),
	},
	templateOption: {
		prefix: "template",
		elements: templateOptionElements,
	},
};

// --- refresh レスポンスフィクスチャ ---

// convert-refresh-srcType-xlsx-response.json をもとにしたフィクスチャ
export const convertRefreshSrcTypeXlsxResponseFixture = {
	prefix: "",
	elements: [] as CommandParam[],
	srcData: {
		prefix: "src",
		elements: makeXlsxSrcDataElements(),
	},
	convertResult: {
		prefix: "result",
		elements: makeCsvResultElements(),
	},
};

// convert-refresh-srcType-table-response.json をもとにしたフィクスチャ
export const convertRefreshSrcTypeTableResponseFixture = {
	prefix: "",
	elements: [] as CommandParam[],
	srcData: {
		prefix: "src",
		elements: makeTableSrcDataElements(),
	},
	convertResult: {
		prefix: "result",
		elements: makeCsvResultElements(),
	},
};

// convert-refresh-resultType-xlsx-response.json をもとにしたフィクスチャ
export const convertRefreshResultTypeXlsxResponseFixture = {
	prefix: "",
	elements: [] as CommandParam[],
	srcData: {
		prefix: "src",
		elements: makeCsvSrcDataElements(""),
	},
	convertResult: {
		prefix: "result",
		elements: makeXlsxResultElements(),
	},
};

// image/pdf targetType 時の file 型 srcData 要素（新旧データ共通）
function makeImageFileSrcDataElements(): CommandParam[] {
	return [
		makeElement("srcType", "ENUM", "file", "WORKSPACE", true, ["file"]),
		makeElement("src", "FILE_OR_DIR", "", "DATASET", true),
		makeElement("recursive", "FLG", "false", "WORKSPACE", false),
		makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
		makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
		makeElement("extension", "TEXT", "png", "WORKSPACE", false),
		makeElement("setting", "FILE", "", "SETTING", false),
		makeElement("settingEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
		makeElement("regTableInclude", "TEXT", "", "WORKSPACE", false),
		makeElement("regTableExclude", "TEXT", "", "WORKSPACE", false),
		makeElement("loadData", "FLG", "true", "WORKSPACE", false),
		makeElement("includeMetaData", "FLG", "false", "WORKSPACE", false),
	];
}

// compare-refresh-targetType-image-response.json をもとにしたフィクスチャ
export const compareRefreshTargetTypeImageResponseFixture = {
	prefix: "",
	elements: [
		makeElement("targetType", "ENUM", "image", "WORKSPACE", false, [
			"data",
			"image",
			"pdf",
		]),
		makeElement("setting", "FILE", "", "SETTING", false),
		makeElement("settingEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
	],
	imageOption: {
		prefix: "image",
		elements: [
			makeElement("threshold", "TEXT", "", "WORKSPACE", false),
			makeElement("pixelToleranceLevel", "TEXT", "", "WORKSPACE", false),
			makeElement(
				"allowingPercentOfDifferentPixels",
				"TEXT",
				"",
				"WORKSPACE",
				false,
			),
			makeElement("rectangleLineWidth", "TEXT", "", "WORKSPACE", false),
			makeElement("minimalRectangleSize", "TEXT", "", "WORKSPACE", false),
			makeElement("maximalRectangleCount", "TEXT", "", "WORKSPACE", false),
			makeElement("excludedAreas", "TEXT", "", "WORKSPACE", false),
			makeElement("drawExcludedRectangles", "FLG", "true", "WORKSPACE", false),
			makeElement("fillExcludedRectangles", "FLG", "false", "WORKSPACE", false),
			makeElement(
				"percentOpacityExcludedRectangles",
				"TEXT",
				"",
				"WORKSPACE",
				false,
			),
			makeElement("excludedRectangleColor", "TEXT", "", "WORKSPACE", false),
			makeElement(
				"fillDifferenceRectangles",
				"FLG",
				"false",
				"WORKSPACE",
				false,
			),
			makeElement(
				"percentOpacityDifferenceRectangles",
				"TEXT",
				"",
				"WORKSPACE",
				false,
			),
			makeElement("differenceRectangleColor", "TEXT", "", "WORKSPACE", false),
		],
	},
	newData: {
		prefix: "new",
		elements: makeImageFileSrcDataElements(),
	},
	oldData: {
		prefix: "old",
		elements: makeImageFileSrcDataElements(),
	},
	convertResult: {
		prefix: "result",
		elements: makeCsvResultElements(true),
	},
	expectData: {
		prefix: "expect",
		elements: [
			makeElement("srcType", "ENUM", "none", "WORKSPACE", false, [
				"none",
				"sql",
				"csv",
				"csvq",
				"xls",
				"xlsx",
			]),
		],
	},
};

// compare-refresh-newSrcType-table-response.json をもとにしたフィクスチャ
export const compareRefreshNewSrcTypeTableResponseFixture = {
	prefix: "",
	elements: [
		makeElement("targetType", "ENUM", "data", "WORKSPACE", false, [
			"data",
			"image",
			"pdf",
		]),
		makeElement("setting", "FILE", "", "SETTING", false),
		makeElement("settingEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
	],
	newData: {
		prefix: "new",
		elements: makeTableSrcDataElements(),
	},
	oldData: {
		prefix: "old",
		elements: makeCsvSrcDataElements(""),
	},
	convertResult: {
		prefix: "result",
		elements: makeCsvResultElements(true),
	},
	expectData: {
		prefix: "expect",
		elements: [
			makeElement("srcType", "ENUM", "none", "WORKSPACE", false, [
				"none",
				"sql",
				"csv",
				"csvq",
				"xls",
				"xlsx",
			]),
		],
	},
};

// compare-refresh-expectSrcType-csv-response.json をもとにしたフィクスチャ
export const compareRefreshExpectSrcTypeCsvResponseFixture = {
	prefix: "",
	elements: [
		makeElement("targetType", "ENUM", "data", "WORKSPACE", false, [
			"data",
			"image",
			"pdf",
		]),
		makeElement("setting", "FILE", "", "SETTING", false),
		makeElement("settingEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
	],
	newData: {
		prefix: "new",
		elements: makeCsvSrcDataElements(""),
	},
	oldData: {
		prefix: "old",
		elements: makeCsvSrcDataElements(""),
	},
	convertResult: {
		prefix: "result",
		elements: makeCsvResultElements(true),
	},
	expectData: {
		prefix: "expect",
		elements: [
			makeElement("srcType", "ENUM", "csv", "WORKSPACE", false, [
				"none",
				"sql",
				"csv",
				"csvq",
				"xls",
				"xlsx",
			]),
			makeElement("src", "FILE_OR_DIR", "", "DATASET", true),
			makeElement("encoding", "TEXT", "UTF-8", "WORKSPACE", false),
			makeElement("recursive", "FLG", "false", "WORKSPACE", false),
			makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
			makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
			makeElement("extension", "TEXT", "", "WORKSPACE", false),
			makeElement("headerName", "TEXT", "", "WORKSPACE", false),
			makeElement("startRow", "TEXT", "1", "WORKSPACE", false),
			makeElement("addFileInfo", "FLG", "false", "WORKSPACE", false),
			makeElement("delimiter", "TEXT", ",", "WORKSPACE", false),
			makeElement("ignoreQuoted", "FLG", "false", "WORKSPACE", false),
			makeElement("setting", "FILE", "", "SETTING", false),
			makeElement("settingEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
			makeElement("regTableInclude", "TEXT", "", "WORKSPACE", false),
			makeElement("regTableExclude", "TEXT", "", "WORKSPACE", false),
			makeElement("loadData", "FLG", "true", "WORKSPACE", false),
			makeElement("includeMetaData", "FLG", "false", "WORKSPACE", false),
		],
	},
};

// generate-refresh-srcType-table-response.json をもとにしたフィクスチャ
export const generateRefreshSrcTypeTableResponseFixture = {
	prefix: "",
	elements: [
		makeElement("generateType", "ENUM", "txt", "WORKSPACE", false, [
			"txt",
			"xlsx",
			"xls",
			"settings",
			"sql",
			"xlsxTemplate",
		]),
		makeElement("unit", "ENUM", "record", "WORKSPACE", false, [
			"record",
			"table",
			"dataset",
		]),
		makeElement("template", "FILE", "", "TEMPLATE", true),
		makeElement("result", "DIR", "", "RESULT", false),
		makeElement("resultPath", "TEXT", "result", "WORKSPACE", false),
		makeElement("outputEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
	],
	srcData: {
		prefix: "src",
		elements: makeTableSrcDataElements(),
	},
	templateOption: {
		prefix: "template",
		elements: templateOptionElements,
	},
};

// run-refresh-scriptType-cmd-response.json をもとにしたフィクスチャ
export const runRefreshScriptTypeCmdResponseFixture = {
	prefix: "",
	elements: [
		makeElement("scriptType", "ENUM", "cmd", "WORKSPACE", false, [
			"cmd",
			"bat",
			"sql",
			"ant",
		]),
		makeElement("baseDir", "TEXT", "", "WORKSPACE", false),
	],
	srcData: {
		prefix: "src",
		elements: [
			makeElement("src", "FILE_OR_DIR", "", "DATASET", true),
			makeElement("recursive", "FLG", "false", "WORKSPACE", false),
			makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
			makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
			makeElement("setting", "FILE", "", "SETTING", false),
			makeElement("settingEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
			makeElement("regTableInclude", "TEXT", "", "WORKSPACE", false),
			makeElement("regTableExclude", "TEXT", "", "WORKSPACE", false),
		],
	},
};

// run-refresh-scriptType-sql-response.json をもとにしたフィクスチャ
export const runRefreshScriptTypeSqlResponseFixture = {
	prefix: "",
	elements: [
		makeElement("scriptType", "ENUM", "sql", "WORKSPACE", false, [
			"cmd",
			"bat",
			"sql",
			"ant",
		]),
	],
	srcData: {
		prefix: "src",
		elements: [
			makeElement("src", "FILE_OR_DIR", "", "DATASET", true),
			makeElement("recursive", "FLG", "false", "WORKSPACE", false),
			makeElement("regInclude", "TEXT", "", "WORKSPACE", false),
			makeElement("regExclude", "TEXT", "", "WORKSPACE", false),
			makeElement("setting", "FILE", "", "SETTING", false),
			makeElement("settingEncoding", "TEXT", "UTF-8", "WORKSPACE", false),
			makeElement("regTableInclude", "TEXT", "", "WORKSPACE", false),
			makeElement("regTableExclude", "TEXT", "", "WORKSPACE", false),
		],
	},
	templateOption: {
		prefix: "template",
		elements: templateOptionElements,
	},
	jdbcOption: {
		prefix: "jdbc",
		elements: [
			makeElement("jdbcProperties", "FILE", "", "JDBC", false),
			makeElement("jdbcUrl", "TEXT", "", "WORKSPACE", false),
			makeElement("jdbcUser", "TEXT", "", "WORKSPACE", false),
			makeElement("jdbcPass", "TEXT", "", "WORKSPACE", false),
		],
	},
};
