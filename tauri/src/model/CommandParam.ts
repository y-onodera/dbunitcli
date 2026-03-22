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
export type DatasetSource = CommandParams & {
	srcType: () => string;
	srcElements: () => CommandParams;
	srcTypeSettings: () => CommandParams;
	jdbcElements: () => CommandParams;
	settingElements: () => CommandParams;
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
	srcElements() {
		return toCommandParams(
			this,
			this.elements.slice(
				0,
				this.indexExtension === -1
					? this.indexRegExclude + 1
					: this.indexExtension + 1,
			),
			{ caption: "traversal option" },
			(param: string) =>
				["recursive", "regInclude", "regExclude", "extension"].includes(param),
		);
	}
	srcTypeSettings() {
		const srcTypeSettings = srcTypeDetail.get(this.src);
		return toCommandParams(
			this,
			this.elements
				.slice(
					this.indexExtension === -1
						? this.indexRegExclude + 1
						: this.indexExtension + 1,
					this.indexOfSetting,
				)
				.filter((it) => !it.name.startsWith("jdbc")),
			srcTypeSettings?.optionCaption,
			srcTypeSettings?.optional,
		);
	}
	jdbcElements() {
		return toCommandParams(
			this,
			this.elements
				.slice(
					this.indexExtension === -1
						? this.indexRegExclude + 1
						: this.indexExtension + 1,
					this.indexOfSetting,
				)
				.filter((it) => it.name.startsWith("jdbc")),
			undefined,
			undefined,
		);
	}
	settingElements() {
		return toCommandParams(
			this,
			this.elements.slice(this.indexOfSetting),
			{ caption: "dataset option" },
			(param: string) =>
				[
					"regTableInclude",
					"regTableExclude",
					"loadData",
					"includeMetaData",
				].includes(param),
		);
	}
}
function findByName(elements: CommandParam[], name: string): CommandParam {
	return elements.find((e) => e.name === name)!;
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
