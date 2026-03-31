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
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
	}
	get srcType(): CommandParam {
		return findByName(this.elements, "srcType");
	}
	get src(): CommandParam {
		return findByName(this.elements, "src");
	}
	get encoding(): CommandParam | undefined {
		return this.elements.find((e) => e.name === "encoding");
	}
	get recursive(): CommandParam {
		return findByName(this.elements, "recursive");
	}
	get regInclude(): CommandParam {
		return findByName(this.elements, "regInclude");
	}
	get regExclude(): CommandParam {
		return findByName(this.elements, "regExclude");
	}
	get extension(): CommandParam {
		return findByName(this.elements, "extension");
	}
}
class SettingElementsImpl implements SettingElements {
	prefix: string;
	elements: CommandParam[];
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
	}
	get setting(): CommandParam {
		return findByName(this.elements, "setting");
	}
	get settingEncoding(): CommandParam {
		return findByName(this.elements, "settingEncoding");
	}
	get regTableInclude(): CommandParam {
		return findByName(this.elements, "regTableInclude");
	}
	get regTableExclude(): CommandParam {
		return findByName(this.elements, "regTableExclude");
	}
	get loadData(): CommandParam {
		return findByName(this.elements, "loadData");
	}
	get includeMetaData(): CommandParam {
		return findByName(this.elements, "includeMetaData");
	}
}
function findByName(elements: CommandParam[], name: string): CommandParam {
	const found = elements.find((e) => e.name === name);
	if (found === undefined) {
		throw new Error(`CommandParam '${name}' not found`);
	}
	return found;
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
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
	}
	get jdbcProperties(): CommandParam {
		return findByName(this.elements, "jdbcProperties");
	}
	get jdbcUrl(): CommandParam {
		return findByName(this.elements, "jdbcUrl");
	}
	get jdbcUser(): CommandParam {
		return findByName(this.elements, "jdbcUser");
	}
	get jdbcPass(): CommandParam {
		return findByName(this.elements, "jdbcPass");
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
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
	}
	get generateType(): CommandParam {
		return findByName(this.elements, "generateType");
	}
	get unit(): CommandParam {
		return findByName(this.elements, "unit");
	}
	get template(): CommandParam {
		return findByName(this.elements, "template");
	}
	get result(): CommandParam {
		return findByName(this.elements, "result");
	}
	get resultPath(): CommandParam {
		return findByName(this.elements, "resultPath");
	}
	get outputEncoding(): CommandParam {
		return findByName(this.elements, "outputEncoding");
	}
}
export type RunElements = CommandParams & {
	scriptType: CommandParam;
};
export class RunElementsImpl implements RunElements {
	prefix: string;
	elements: CommandParam[];
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
	}
	get scriptType(): CommandParam {
		return findByName(this.elements, "scriptType");
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
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
	}
	get unit(): CommandParam {
		return findByName(this.elements, "unit");
	}
	get parameterize(): CommandParam {
		return findByName(this.elements, "parameterize");
	}
	get ignoreFail(): CommandParam {
		return findByName(this.elements, "ignoreFail");
	}
	get cmd(): CommandParam {
		return findByName(this.elements, "cmd");
	}
	get cmdParam(): CommandParam {
		return findByName(this.elements, "cmdParam");
	}
	get template(): CommandParam {
		return findByName(this.elements, "template");
	}
}
export type ConvertResult = CommandParams & {
	resultType: CommandParam;
	result: CommandParam;
	resultPath: CommandParam;
	exportEmptyTable: CommandParam;
	exportHeader: CommandParam;
	outputEncoding: CommandParam;
	jdbc?: JdbcOption;
};
export class ConvertResultImpl implements ConvertResult {
	prefix: string;
	elements: CommandParam[];
	readonly jdbc?: JdbcOption;
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
	}
	get resultType(): CommandParam {
		return findByName(this.elements, "resultType");
	}
	get result(): CommandParam {
		return findByName(this.elements, "result");
	}
	get resultPath(): CommandParam {
		return findByName(this.elements, "resultPath");
	}
	get exportEmptyTable(): CommandParam {
		return findByName(this.elements, "exportEmptyTable");
	}
	get exportHeader(): CommandParam {
		return findByName(this.elements, "exportHeader");
	}
	get outputEncoding(): CommandParam {
		return findByName(this.elements, "outputEncoding");
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
	constructor(prefix: string, elements: CommandParam[]) {
		this.prefix = prefix;
		this.elements = elements;
	}
	get encoding(): CommandParam {
		return findByName(this.elements, "encoding");
	}
	get templateGroup(): CommandParam {
		return findByName(this.elements, "templateGroup");
	}
	get templateParameterAttribute(): CommandParam {
		return findByName(this.elements, "templateParameterAttribute");
	}
	get templateVarStart(): CommandParam {
		return findByName(this.elements, "templateVarStart");
	}
	get templateVarStop(): CommandParam {
		return findByName(this.elements, "templateVarStop");
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
	constructor(
		public prefix: string,
		public elements: CommandParam[],
	) {}
	get headerName(): CommandParam {
		return findByName(this.elements, "headerName");
	}
	get startRow(): CommandParam {
		return findByName(this.elements, "startRow");
	}
	get addFileInfo(): CommandParam {
		return findByName(this.elements, "addFileInfo");
	}
	get delimiter(): CommandParam {
		return findByName(this.elements, "delimiter");
	}
	get ignoreQuoted(): CommandParam {
		return findByName(this.elements, "ignoreQuoted");
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
	constructor(
		public prefix: string,
		public elements: CommandParam[],
	) {}
	get headerName(): CommandParam {
		return findByName(this.elements, "headerName");
	}
	get addFileInfo(): CommandParam {
		return findByName(this.elements, "addFileInfo");
	}
	get encoding(): CommandParam {
		return findByName(this.elements, "encoding");
	}
	get templateGroup(): CommandParam {
		return findByName(this.elements, "templateGroup");
	}
	get templateParameterAttribute(): CommandParam {
		return findByName(this.elements, "templateParameterAttribute");
	}
	get templateVarStart(): CommandParam {
		return findByName(this.elements, "templateVarStart");
	}
	get templateVarStop(): CommandParam {
		return findByName(this.elements, "templateVarStop");
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
	constructor(
		public prefix: string,
		public elements: CommandParam[],
	) {}
	get headerName(): CommandParam {
		return findByName(this.elements, "headerName");
	}
	get addFileInfo(): CommandParam {
		return findByName(this.elements, "addFileInfo");
	}
	get useJdbcMetaData(): CommandParam {
		return findByName(this.elements, "useJdbcMetaData");
	}
	get templateGroup(): CommandParam {
		return findByName(this.elements, "templateGroup");
	}
	get templateParameterAttribute(): CommandParam {
		return findByName(this.elements, "templateParameterAttribute");
	}
	get templateVarStart(): CommandParam {
		return findByName(this.elements, "templateVarStart");
	}
	get templateVarStop(): CommandParam {
		return findByName(this.elements, "templateVarStop");
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
	constructor(
		public prefix: string,
		public elements: CommandParam[],
	) {}
	get headerName(): CommandParam {
		return findByName(this.elements, "headerName");
	}
	get startRow(): CommandParam {
		return findByName(this.elements, "startRow");
	}
	get addFileInfo(): CommandParam {
		return findByName(this.elements, "addFileInfo");
	}
	get regDataSplit(): CommandParam {
		return findByName(this.elements, "regDataSplit");
	}
	get regHeaderSplit(): CommandParam {
		return findByName(this.elements, "regHeaderSplit");
	}
}

export type FixedTypeSettings = CommandParams & {
	headerName: CommandParam;
	startRow: CommandParam;
	addFileInfo: CommandParam;
	fixedLength: CommandParam;
};
export class FixedTypeSettingsImpl implements FixedTypeSettings {
	constructor(
		public prefix: string,
		public elements: CommandParam[],
	) {}
	get headerName(): CommandParam {
		return findByName(this.elements, "headerName");
	}
	get startRow(): CommandParam {
		return findByName(this.elements, "startRow");
	}
	get addFileInfo(): CommandParam {
		return findByName(this.elements, "addFileInfo");
	}
	get fixedLength(): CommandParam {
		return findByName(this.elements, "fixedLength");
	}
}

export type XlsTypeSettings = CommandParams & {
	headerName: CommandParam;
	startRow: CommandParam;
	addFileInfo: CommandParam;
	xlsxSchema: CommandParam;
};
export class XlsTypeSettingsImpl implements XlsTypeSettings {
	constructor(
		public prefix: string,
		public elements: CommandParam[],
	) {}
	get headerName(): CommandParam {
		return findByName(this.elements, "headerName");
	}
	get startRow(): CommandParam {
		return findByName(this.elements, "startRow");
	}
	get addFileInfo(): CommandParam {
		return findByName(this.elements, "addFileInfo");
	}
	get xlsxSchema(): CommandParam {
		return findByName(this.elements, "xlsxSchema");
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
	constructor(
		public prefix: string,
		public elements: CommandParam[],
	) {}
	get threshold(): CommandParam {
		return findByName(this.elements, "threshold");
	}
	get pixelToleranceLevel(): CommandParam {
		return findByName(this.elements, "pixelToleranceLevel");
	}
	get allowingPercentOfDifferentPixels(): CommandParam {
		return findByName(this.elements, "allowingPercentOfDifferentPixels");
	}
	get rectangleLineWidth(): CommandParam {
		return findByName(this.elements, "rectangleLineWidth");
	}
	get minimalRectangleSize(): CommandParam {
		return findByName(this.elements, "minimalRectangleSize");
	}
	get maximalRectangleCount(): CommandParam {
		return findByName(this.elements, "maximalRectangleCount");
	}
	get excludedAreas(): CommandParam {
		return findByName(this.elements, "excludedAreas");
	}
	get drawExcludedRectangles(): CommandParam {
		return findByName(this.elements, "drawExcludedRectangles");
	}
	get fillExcludedRectangles(): CommandParam {
		return findByName(this.elements, "fillExcludedRectangles");
	}
	get percentOpacityExcludedRectangles(): CommandParam {
		return findByName(this.elements, "percentOpacityExcludedRectangles");
	}
	get excludedRectangleColor(): CommandParam {
		return findByName(this.elements, "excludedRectangleColor");
	}
	get fillDifferenceRectangles(): CommandParam {
		return findByName(this.elements, "fillDifferenceRectangles");
	}
	get percentOpacityDifferenceRectangles(): CommandParam {
		return findByName(this.elements, "percentOpacityDifferenceRectangles");
	}
	get differenceRectangleColor(): CommandParam {
		return findByName(this.elements, "differenceRectangleColor");
	}
}
