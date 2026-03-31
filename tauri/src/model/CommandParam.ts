export type DefaultPath =
	| "WORKSPACE"
	| "DATASET"
	| "RESULT"
	| "SETTING"
	| "TEMPLATE"
	| "PARAMETERIZE_TEMPLATE"
	| "JDBC"
	| "XLSX_SCHEMA";
export type Attribute = {
	type: string;
	required: boolean;
	selectOption: string[];
	defaultPath: DefaultPath;
};
export type CommandParam = {
	name: string;
	value: string;
	attribute: Attribute;
	optional: boolean;
};
export type CommandParams = {
	prefix: string;
	elements: CommandParam[];
};
export type SrcInfo = {
	srcPath: string;
	encoding?: string;
	regTableInclude: string;
	regTableExclude: string;
	recursive: string;
	regInclude: string;
	regExclude: string;
	extension: string;
};
export type DatasetSrcInfo = SrcInfo & {
	srcType: string;
	setting?: string;
	xlsxSchema: string;
	fixedLength: string;
	regHeaderSplit: string;
	regDataSplit: string;
	encoding: string;
	delimiter: string;
	ignoreQuoted: boolean;
	headerName: string;
	startRow: string;
	addFileInfo: boolean;
};
export type SrcElements = CommandParams & {
	srcType: CommandParam;
	src: CommandParam;
	encoding?: CommandParam;
	recursive: CommandParam;
	regInclude: CommandParam;
	regExclude: CommandParam;
	extension: CommandParam;
};
export type SettingElements = CommandParams & {
	setting: CommandParam;
	settingEncoding: CommandParam;
	regTableInclude: CommandParam;
	regTableExclude: CommandParam;
	loadData: CommandParam;
	includeMetaData: CommandParam;
};
export type DatasetSource = CommandParams & {
	srcType: () => string;
	srcElements: () => SrcElements;
	srcTypeSettings: () => CommandParams;
	jdbcElements: () => CommandParams;
	settingElements: () => SettingElements;
	jdbcOption: () => JdbcOption;
	templateOption: () => TemplateOption;
};
export class DatasetSourceImpl implements DatasetSource {
	prefix: string;
	elements: CommandParam[];
	private indexOfSetting: number;
	private indexExtension: number;
	private indexRegExclude: number;
	private src: string;
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
		this.indexOfSetting = -1;
		this.indexExtension = -1;
		this.indexRegExclude = -1;
		this.src = "";
		for (let i = 0; i < elements.length; i++) {
			if (elements[i].name === "setting") {
				this.indexOfSetting = i;
			}
			if (elements[i].name === "extension") {
				this.indexExtension = i;
			}
			if (elements[i].name === "regExclude") {
				this.indexRegExclude = i;
			}
			if (elements[i].name === "srcType") {
				this.src = elements[i].value;
			}
		}
	}
	srcType() {
		return this.src;
	}
	srcElements(): SrcElements {
		const elements = this.elements.slice(
			0,
			this.indexExtension === -1
				? this.indexRegExclude + 1
				: this.indexExtension + 1,
		);
		return new SrcElementsImpl(this.prefix, elements);
	}
	private srcTypeRange(): CommandParam[] {
		return this.elements.slice(
			this.indexExtension === -1
				? this.indexRegExclude + 1
				: this.indexExtension + 1,
			this.indexOfSetting,
		);
	}
	srcTypeSettings() {
		return toCommandParams(
			this,
			this.srcTypeRange().filter((it) => !it.name.startsWith("jdbc")),
		);
	}
	jdbcElements() {
		return toCommandParams(
			this,
			this.srcTypeRange().filter((it) => it.name.startsWith("jdbc")),
		);
	}
	settingElements(): SettingElements {
		const elements = this.elements.slice(this.indexOfSetting);
		return new SettingElementsImpl(this.prefix, elements);
	}
	jdbcOption(): JdbcOption {
		const jdbc = this.jdbcElements();
		if (jdbc.elements.length === 0) {
			return jdbc as unknown as JdbcOption;
		}
		return new JdbcOptionImpl(jdbc.prefix, jdbc.elements);
	}
	templateOption(): TemplateOption {
		const elements = this.srcTypeRange().filter((it) =>
			it.name.startsWith("template"),
		);
		return new TemplateOptionImpl(this.prefix, elements);
	}
}
class SrcElementsImpl implements SrcElements {
	prefix: string;
	elements: CommandParam[];
	readonly srcType: CommandParam;
	readonly src: CommandParam;
	readonly encoding: CommandParam | undefined;
	readonly recursive: CommandParam;
	readonly regInclude: CommandParam;
	readonly regExclude: CommandParam;
	readonly extension: CommandParam;
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
		this.srcType = findByName(elements, "srcType");
		this.src = findByName(elements, "src");
		this.encoding = findByNameOptional(elements, "encoding");
		this.recursive = findByName(elements, "recursive");
		this.regInclude = findByName(elements, "regInclude");
		this.regExclude = findByName(elements, "regExclude");
		this.extension = findByName(elements, "extension");
	}
}
class SettingElementsImpl implements SettingElements {
	prefix: string;
	elements: CommandParam[];
	readonly setting: CommandParam;
	readonly settingEncoding: CommandParam;
	readonly regTableInclude: CommandParam;
	readonly regTableExclude: CommandParam;
	readonly loadData: CommandParam;
	readonly includeMetaData: CommandParam;
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
		this.setting = findByName(elements, "setting");
		this.settingEncoding = findByName(elements, "settingEncoding");
		this.regTableInclude = findByName(elements, "regTableInclude");
		this.regTableExclude = findByName(elements, "regTableExclude");
		this.loadData = findByName(elements, "loadData");
		this.includeMetaData = findByName(elements, "includeMetaData");
	}
}
function findByName(elements: CommandParam[], name: string): CommandParam {
	const found = elements.find((e) => e.name === name);
	if (found === undefined) {
		throw new Error(`CommandParam '${name}' not found`);
	}
	return found;
}
function findByNameOptional(
	elements: CommandParam[],
	name: string,
): CommandParam | undefined {
	return elements.find((e) => e.name === name);
}

export type JdbcOption = CommandParams & {
	jdbcProperties: CommandParam;
	jdbcUrl: CommandParam;
	jdbcUser: CommandParam;
	jdbcPass: CommandParam;
};
export class JdbcOptionImpl implements JdbcOption {
	prefix: string;
	elements: CommandParam[];
	readonly jdbcProperties: CommandParam;
	readonly jdbcUrl: CommandParam;
	readonly jdbcUser: CommandParam;
	readonly jdbcPass: CommandParam;
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
		this.jdbcProperties = findByName(elements, "jdbcProperties");
		this.jdbcUrl = findByName(elements, "jdbcUrl");
		this.jdbcUser = findByName(elements, "jdbcUser");
		this.jdbcPass = findByName(elements, "jdbcPass");
	}
}
export type GenerateElements = CommandParams & {
	generateType: CommandParam;
	unit: CommandParam;
	template: CommandParam;
	result: CommandParam;
	resultPath: CommandParam;
	outputEncoding: CommandParam;
};
export class GenerateElementsImpl implements GenerateElements {
	prefix: string;
	elements: CommandParam[];
	readonly generateType: CommandParam;
	readonly unit: CommandParam;
	readonly template: CommandParam;
	readonly result: CommandParam;
	readonly resultPath: CommandParam;
	readonly outputEncoding: CommandParam;
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
		this.generateType = findByName(elements, "generateType");
		this.unit = findByName(elements, "unit");
		this.template = findByName(elements, "template");
		this.result = findByName(elements, "result");
		this.resultPath = findByName(elements, "resultPath");
		this.outputEncoding = findByName(elements, "outputEncoding");
	}
}
export type RunElements = CommandParams & {
	scriptType: CommandParam;
};
export class RunElementsImpl implements RunElements {
	prefix: string;
	elements: CommandParam[];
	readonly scriptType: CommandParam;
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
		this.scriptType = findByName(elements, "scriptType");
	}
}
export type ParameterizeElements = CommandParams & {
	unit: CommandParam;
	parameterize: CommandParam;
	ignoreFail: CommandParam;
	cmd: CommandParam;
	cmdParam: CommandParam;
	template: CommandParam;
};
export class ParameterizeElementsImpl implements ParameterizeElements {
	prefix: string;
	elements: CommandParam[];
	readonly unit: CommandParam;
	readonly parameterize: CommandParam;
	readonly ignoreFail: CommandParam;
	readonly cmd: CommandParam;
	readonly cmdParam: CommandParam;
	readonly template: CommandParam;
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
		this.unit = findByName(elements, "unit");
		this.parameterize = findByName(elements, "parameterize");
		this.ignoreFail = findByName(elements, "ignoreFail");
		this.cmd = findByName(elements, "cmd");
		this.cmdParam = findByName(elements, "cmdParam");
		this.template = findByName(elements, "template");
	}
}
export type ConvertResult = CommandParams & {
	resultType: CommandParam;
	result: CommandParam;
	resultPath: CommandParam;
	exportEmptyTable: CommandParam;
	exportHeader: CommandParam;
	outputEncoding?: CommandParam;
	jdbc?: JdbcOption;
};
export class ConvertResultImpl implements ConvertResult {
	prefix: string;
	elements: CommandParam[];
	readonly jdbc?: JdbcOption;
	readonly resultType: CommandParam;
	readonly result: CommandParam;
	readonly resultPath: CommandParam;
	readonly exportEmptyTable: CommandParam;
	readonly exportHeader: CommandParam;
	readonly outputEncoding: CommandParam | undefined;
	constructor(
		prefix: string,
		elements: CommandParam[],
		rawJdbc?: { prefix: string; elements: CommandParam[] },
	) {
		this.prefix = prefix;
		this.elements = elements;
		this.jdbc = rawJdbc
			? new JdbcOptionImpl(rawJdbc.prefix, rawJdbc.elements)
			: undefined;
		this.resultType = findByName(elements, "resultType");
		this.result = findByName(elements, "result");
		this.resultPath = findByName(elements, "resultPath");
		this.exportEmptyTable = findByName(elements, "exportEmptyTable");
		this.exportHeader = findByName(elements, "exportHeader");
		this.outputEncoding = findByNameOptional(elements, "outputEncoding");
	}
}
export type TemplateOption = CommandParams & {
	encoding: CommandParam;
	templateGroup: CommandParam;
	templateParameterAttribute: CommandParam;
	templateVarStart: CommandParam;
	templateVarStop: CommandParam;
};
export class TemplateOptionImpl implements TemplateOption {
	prefix: string;
	elements: CommandParam[];
	readonly encoding: CommandParam;
	readonly templateGroup: CommandParam;
	readonly templateParameterAttribute: CommandParam;
	readonly templateVarStart: CommandParam;
	readonly templateVarStop: CommandParam;
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
		this.encoding = findByName(elements, "encoding");
		this.templateGroup = findByName(elements, "templateGroup");
		this.templateParameterAttribute = findByName(
			elements,
			"templateParameterAttribute",
		);
		this.templateVarStart = findByName(elements, "templateVarStart");
		this.templateVarStop = findByName(elements, "templateVarStop");
	}
}
function toCommandParams(
	srcData: CommandParams,
	elements: CommandParam[],
): CommandParams {
	return {
		prefix: srcData.prefix,
		elements: elements,
	};
}

export function buildSrcInfo(elements: CommandParam[]): SrcInfo {
	const find = (name: string) => elements.find((e) => e.name === name);
	return {
		srcPath: find("src")?.value ?? "",
		encoding: find("encoding")?.value,
		regTableInclude: find("regTableInclude")?.value ?? "",
		regTableExclude: find("regTableExclude")?.value ?? "",
		recursive: find("recursive")?.value ?? "",
		regInclude: find("regInclude")?.value ?? "",
		regExclude: find("regExclude")?.value ?? "",
		extension: find("extension")?.value ?? "",
	};
}

export function buildDatasetSrcInfo(elements: CommandParam[]): DatasetSrcInfo {
	const find = (name: string) => elements.find((e) => e.name === name);
	return {
		...buildSrcInfo(elements),
		srcType: find("srcType")?.value ?? "",
		xlsxSchema: find("xlsxSchema")?.value ?? "",
		fixedLength: find("fixedLength")?.value ?? "",
		regHeaderSplit: find("regHeaderSplit")?.value ?? "",
		regDataSplit: find("regDataSplit")?.value ?? "",
		encoding: find("encoding")?.value ?? "",
		delimiter: find("delimiter")?.value ?? "",
		ignoreQuoted: find("ignoreQuoted")?.value === "true",
		headerName: find("headerName")?.value ?? "",
		startRow: find("startRow")?.value ?? "",
		addFileInfo: find("addFileInfo")?.value === "true",
	};
}

export type CsvTypeSettings = CommandParams & {
	headerName: CommandParam;
	startRow: CommandParam;
	addFileInfo: CommandParam;
	delimiter: CommandParam;
	ignoreQuoted: CommandParam;
};
export class CsvTypeSettingsImpl implements CsvTypeSettings {
	readonly headerName: CommandParam;
	readonly startRow: CommandParam;
	readonly addFileInfo: CommandParam;
	readonly delimiter: CommandParam;
	readonly ignoreQuoted: CommandParam;
	constructor(
		public prefix: string,
		public elements: CommandParam[],
	) {
		this.headerName = findByName(elements, "headerName");
		this.startRow = findByName(elements, "startRow");
		this.addFileInfo = findByName(elements, "addFileInfo");
		this.delimiter = findByName(elements, "delimiter");
		this.ignoreQuoted = findByName(elements, "ignoreQuoted");
	}
}

export type CsvqTypeSettings = CommandParams & {
	headerName: CommandParam;
	addFileInfo: CommandParam;
	encoding: CommandParam;
	templateGroup: CommandParam;
	templateParameterAttribute: CommandParam;
	templateVarStart: CommandParam;
	templateVarStop: CommandParam;
};
export class CsvqTypeSettingsImpl implements CsvqTypeSettings {
	readonly headerName: CommandParam;
	readonly addFileInfo: CommandParam;
	readonly encoding: CommandParam;
	readonly templateGroup: CommandParam;
	readonly templateParameterAttribute: CommandParam;
	readonly templateVarStart: CommandParam;
	readonly templateVarStop: CommandParam;
	constructor(
		public prefix: string,
		public elements: CommandParam[],
	) {
		this.headerName = findByName(elements, "headerName");
		this.addFileInfo = findByName(elements, "addFileInfo");
		this.encoding = findByName(elements, "encoding");
		this.templateGroup = findByName(elements, "templateGroup");
		this.templateParameterAttribute = findByName(
			elements,
			"templateParameterAttribute",
		);
		this.templateVarStart = findByName(elements, "templateVarStart");
		this.templateVarStop = findByName(elements, "templateVarStop");
	}
}

export type TableSqlTypeSettings = CommandParams & {
	headerName: CommandParam;
	addFileInfo: CommandParam;
	useJdbcMetaData: CommandParam;
	templateGroup: CommandParam;
	templateParameterAttribute: CommandParam;
	templateVarStart: CommandParam;
	templateVarStop: CommandParam;
};
export class TableSqlTypeSettingsImpl implements TableSqlTypeSettings {
	readonly headerName: CommandParam;
	readonly addFileInfo: CommandParam;
	readonly useJdbcMetaData: CommandParam;
	readonly templateGroup: CommandParam;
	readonly templateParameterAttribute: CommandParam;
	readonly templateVarStart: CommandParam;
	readonly templateVarStop: CommandParam;
	constructor(
		public prefix: string,
		public elements: CommandParam[],
	) {
		this.headerName = findByName(elements, "headerName");
		this.addFileInfo = findByName(elements, "addFileInfo");
		this.useJdbcMetaData = findByName(elements, "useJdbcMetaData");
		this.templateGroup = findByName(elements, "templateGroup");
		this.templateParameterAttribute = findByName(
			elements,
			"templateParameterAttribute",
		);
		this.templateVarStart = findByName(elements, "templateVarStart");
		this.templateVarStop = findByName(elements, "templateVarStop");
	}
}

export type RegTypeSettings = CommandParams & {
	headerName: CommandParam;
	startRow: CommandParam;
	addFileInfo: CommandParam;
	regDataSplit: CommandParam;
	regHeaderSplit: CommandParam;
};
export class RegTypeSettingsImpl implements RegTypeSettings {
	readonly headerName: CommandParam;
	readonly startRow: CommandParam;
	readonly addFileInfo: CommandParam;
	readonly regDataSplit: CommandParam;
	readonly regHeaderSplit: CommandParam;
	constructor(
		public prefix: string,
		public elements: CommandParam[],
	) {
		this.headerName = findByName(elements, "headerName");
		this.startRow = findByName(elements, "startRow");
		this.addFileInfo = findByName(elements, "addFileInfo");
		this.regDataSplit = findByName(elements, "regDataSplit");
		this.regHeaderSplit = findByName(elements, "regHeaderSplit");
	}
}

export type FixedTypeSettings = CommandParams & {
	headerName: CommandParam;
	startRow: CommandParam;
	addFileInfo: CommandParam;
	fixedLength: CommandParam;
};
export class FixedTypeSettingsImpl implements FixedTypeSettings {
	readonly headerName: CommandParam;
	readonly startRow: CommandParam;
	readonly addFileInfo: CommandParam;
	readonly fixedLength: CommandParam;
	constructor(
		public prefix: string,
		public elements: CommandParam[],
	) {
		this.headerName = findByName(elements, "headerName");
		this.startRow = findByName(elements, "startRow");
		this.addFileInfo = findByName(elements, "addFileInfo");
		this.fixedLength = findByName(elements, "fixedLength");
	}
}

export type XlsTypeSettings = CommandParams & {
	headerName: CommandParam;
	startRow: CommandParam;
	addFileInfo: CommandParam;
	xlsxSchema: CommandParam;
};
export class XlsTypeSettingsImpl implements XlsTypeSettings {
	readonly headerName: CommandParam;
	readonly startRow: CommandParam;
	readonly addFileInfo: CommandParam;
	readonly xlsxSchema: CommandParam;
	constructor(
		public prefix: string,
		public elements: CommandParam[],
	) {
		this.headerName = findByName(elements, "headerName");
		this.startRow = findByName(elements, "startRow");
		this.addFileInfo = findByName(elements, "addFileInfo");
		this.xlsxSchema = findByName(elements, "xlsxSchema");
	}
}
export type ImageOption = CommandParams & {
	threshold: CommandParam;
	pixelToleranceLevel: CommandParam;
	allowingPercentOfDifferentPixels: CommandParam;
	rectangleLineWidth: CommandParam;
	minimalRectangleSize: CommandParam;
	maximalRectangleCount: CommandParam;
	excludedAreas: CommandParam;
	drawExcludedRectangles: CommandParam;
	fillExcludedRectangles: CommandParam;
	percentOpacityExcludedRectangles: CommandParam;
	excludedRectangleColor: CommandParam;
	fillDifferenceRectangles: CommandParam;
	percentOpacityDifferenceRectangles: CommandParam;
	differenceRectangleColor: CommandParam;
};
export class ImageOptionImpl implements ImageOption {
	readonly threshold: CommandParam;
	readonly pixelToleranceLevel: CommandParam;
	readonly allowingPercentOfDifferentPixels: CommandParam;
	readonly rectangleLineWidth: CommandParam;
	readonly minimalRectangleSize: CommandParam;
	readonly maximalRectangleCount: CommandParam;
	readonly excludedAreas: CommandParam;
	readonly drawExcludedRectangles: CommandParam;
	readonly fillExcludedRectangles: CommandParam;
	readonly percentOpacityExcludedRectangles: CommandParam;
	readonly excludedRectangleColor: CommandParam;
	readonly fillDifferenceRectangles: CommandParam;
	readonly percentOpacityDifferenceRectangles: CommandParam;
	readonly differenceRectangleColor: CommandParam;
	constructor(
		public prefix: string,
		public elements: CommandParam[],
	) {
		this.threshold = findByName(elements, "threshold");
		this.pixelToleranceLevel = findByName(elements, "pixelToleranceLevel");
		this.allowingPercentOfDifferentPixels = findByName(
			elements,
			"allowingPercentOfDifferentPixels",
		);
		this.rectangleLineWidth = findByName(elements, "rectangleLineWidth");
		this.minimalRectangleSize = findByName(elements, "minimalRectangleSize");
		this.maximalRectangleCount = findByName(elements, "maximalRectangleCount");
		this.excludedAreas = findByName(elements, "excludedAreas");
		this.drawExcludedRectangles = findByName(
			elements,
			"drawExcludedRectangles",
		);
		this.fillExcludedRectangles = findByName(
			elements,
			"fillExcludedRectangles",
		);
		this.percentOpacityExcludedRectangles = findByName(
			elements,
			"percentOpacityExcludedRectangles",
		);
		this.excludedRectangleColor = findByName(
			elements,
			"excludedRectangleColor",
		);
		this.fillDifferenceRectangles = findByName(
			elements,
			"fillDifferenceRectangles",
		);
		this.percentOpacityDifferenceRectangles = findByName(
			elements,
			"percentOpacityDifferenceRectangles",
		);
		this.differenceRectangleColor = findByName(
			elements,
			"differenceRectangleColor",
		);
	}
}
