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
	name: string;
	prefix: string;
	elements: CommandParam[];
	optionCaption?: { caption: string };
	optional?: (_: string) => boolean;
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
	name: string;
	prefix: string;
	elements: CommandParam[];
	private indexOfSetting: number;
	private indexExtension: number;
	private indexRegExclude: number;
	private src: string;
	constructor(name: string, prefix: string, elements: CommandParam[]) {
		this.name = name;
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
		return new SrcElementsImpl(this.name, this.prefix, elements);
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
		const srcTypeSettings = srcTypeDetail.get(this.src);
		return toCommandParams(
			this,
			this.srcTypeRange().filter((it) => !it.name.startsWith("jdbc")),
			srcTypeSettings?.optionCaption,
			srcTypeSettings?.optional,
		);
	}
	jdbcElements() {
		return toCommandParams(
			this,
			this.srcTypeRange().filter((it) => it.name.startsWith("jdbc")),
			undefined,
			undefined,
		);
	}
	settingElements(): SettingElements {
		const elements = this.elements.slice(this.indexOfSetting);
		return new SettingElementsImpl(this.name, this.prefix, elements);
	}
	jdbcOption(): JdbcOption {
		const jdbc = this.jdbcElements();
		return new JdbcOptionImpl(jdbc.name, jdbc.prefix, jdbc.elements);
	}
	templateOption(): TemplateOption {
		const elements = this.srcTypeRange().filter((it) =>
			it.name.startsWith("template"),
		);
		return new TemplateOptionImpl(this.name, this.prefix, elements);
	}
}
class SrcElementsImpl implements SrcElements {
	name: string;
	prefix: string;
	elements: CommandParam[];
	constructor(name: string, prefix: string, elements: CommandParam[]) {
		this.name = name;
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
	name: string;
	prefix: string;
	elements: CommandParam[];
	constructor(name: string, prefix: string, elements: CommandParam[]) {
		this.name = name;
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
	name: string;
	prefix: string;
	elements: CommandParam[];
	optionCaption?: { caption: string };
	optional?: (_: string) => boolean;
	constructor(name: string, prefix: string, elements: CommandParam[]) {
		this.name = name;
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
	name: string;
	prefix: string;
	elements: CommandParam[];
	optionCaption?: { caption: string };
	optional?: (_: string) => boolean;
	constructor(name: string, prefix: string, elements: CommandParam[]) {
		this.name = name;
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
	name: string;
	prefix: string;
	elements: CommandParam[];
	optionCaption?: { caption: string };
	optional?: (_: string) => boolean;
	constructor(name: string, prefix: string, elements: CommandParam[]) {
		this.name = name;
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
	name: string;
	prefix: string;
	elements: CommandParam[];
	optionCaption?: { caption: string };
	optional?: (_: string) => boolean;
	constructor(name: string, prefix: string, elements: CommandParam[]) {
		this.name = name;
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
export type TemplateOption = CommandParams & {
	encoding: CommandParam;
	templateGroup: CommandParam;
	templateParameterAttribute: CommandParam;
	templateVarStart: CommandParam;
	templateVarStop: CommandParam;
};
export class TemplateOptionImpl implements TemplateOption {
	name: string;
	prefix: string;
	elements: CommandParam[];
	optionCaption?: { caption: string };
	optional?: (_: string) => boolean;
	constructor(name: string, prefix: string, elements: CommandParam[]) {
		this.name = name;
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
const srcTypeDetail = new Map<
	string,
	{
		optionCaption: { caption: string };
		optional: (_: string) => boolean;
	}
>([
	[
		"table",
		{
			optionCaption: { caption: "table option" },
			optional: (name: string) =>
				name !== "encoding" && !name.startsWith("jdbc"),
		},
	],
	[
		"sql",
		{
			optionCaption: { caption: "sql option" },
			optional: (name: string) =>
				name !== "encoding" && !name.startsWith("jdbc"),
		},
	],
	[
		"csv",
		{
			optionCaption: { caption: "csv option" },
			optional: (name: string) => name !== "encoding",
		},
	],
	[
		"csvq",
		{
			optionCaption: { caption: "csvq option" },
			optional: (name: string) => name !== "encoding",
		},
	],
	[
		"reg",
		{
			optionCaption: { caption: "reg option" },
			optional: (name: string) =>
				name !== "regDataSplit" && name !== "regHeaderSplit",
		},
	],
	[
		"fixed",
		{
			optionCaption: { caption: "fixed option" },
			optional: (name: string) => name !== "fixedLength",
		},
	],
	[
		"xls",
		{
			optionCaption: { caption: "xls option" },
			optional: (_: string) => true,
		},
	],
	[
		"xlsx",
		{
			optionCaption: { caption: "xlsx option" },
			optional: (_: string) => true,
		},
	],
]);
function toCommandParams(
	srcData: CommandParams,
	elements: CommandParam[],
	optionCaption?: { caption: string },
	optional?: (_: string) => boolean,
): CommandParams {
	return {
		name: srcData.name,
		prefix: srcData.prefix,
		elements: elements,
		optionCaption: optionCaption,
		optional: optional,
	};
}
