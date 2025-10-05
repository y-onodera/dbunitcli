export class SelectParameter {
	readonly name: string;
	readonly command: string;
	readonly convert: ConvertParams = {} as ConvertParams;
	readonly compare: CompareParams = {} as CompareParams;
	readonly generate: GenerateParams = {} as GenerateParams;
	readonly run: RunParams = {} as RunParams;
	readonly parameterize: ParameterizeParams = {} as ParameterizeParams;

	constructor(response: Parameter, command: string, name: string) {
		this.name = name;
		this.command = command;
		if (command === "convert") {
			this.convert = response as ConvertParams;
			this.convert.srcData = new DatasetSourceImpl(
				this.convert.srcData.name,
				this.convert.srcData.prefix,
				this.convert.srcData.elements,
			);
		}
		if (command === "compare") {
			this.compare = response as CompareParams;
			this.compare.newData = new DatasetSourceImpl(
				this.compare.newData.name,
				this.compare.newData.prefix,
				this.compare.newData.elements,
			);
			this.compare.oldData = new DatasetSourceImpl(
				this.compare.oldData.name,
				this.compare.oldData.prefix,
				this.compare.oldData.elements,
			);
			this.compare.expectData = new DatasetSourceImpl(
				this.compare.expectData.name,
				this.compare.expectData.prefix,
				this.compare.expectData.elements,
			);
		}
		if (command === "generate") {
			this.generate = response as GenerateParams;
			this.generate.srcData = new DatasetSourceImpl(
				this.generate.srcData.name,
				this.generate.srcData.prefix,
				this.generate.srcData.elements,
			);
		}
		if (command === "run") {
			this.run = response as RunParams;
			this.run.srcData = new DatasetSourceImpl(
				this.run.srcData.name,
				this.run.srcData.prefix,
				this.run.srcData.elements,
			);
		}
		if (command === "parameterize") {
			this.parameterize = response as ParameterizeParams;
			this.parameterize.paramData = new DatasetSourceImpl(
				this.parameterize.paramData.name,
				this.parameterize.paramData.prefix,
				this.parameterize.paramData.elements,
			);
		}
	}
	currentParameter() {
		if (this.command === "convert") {
			return this.convert;
		}
		if (this.command === "compare") {
			return this.compare;
		}
		if (this.command === "generate") {
			return this.generate;
		}
		if (this.command === "run") {
			return this.run;
		}
		return this.parameterize;
	}
}
export type DefaultPath =
	| "WORKSPACE"
	| "DATASET"
	| "RESULT"
	| "SETTING"
	| "TEMPLATE"
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
	optionCaption?: { caption: string; display: (_: string) => boolean };
	optional?: (_: string) => boolean;
};
export type DatasetSource = CommandParams & {
	srcType: () => string;
	srcElements: () => CommandParams;
	srcTypeSettings: () => CommandParams;
	settingElements: () => CommandParams;
};
class DatasetSourceImpl implements DatasetSource {
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
			{
				caption: "traversal option",
				display: (name) => name === "recursive",
			},
			(param: string) =>
				["recursive", "regInclude", "regExclude", "extension"].includes(param),
		);
	}
	srcTypeSettings() {
		const srcTypeSettings = srcTypeDetail.get(this.src);
		return toCommandParams(
			this,
			this.elements.slice(
				this.indexExtension === -1
					? this.indexRegExclude + 1
					: this.indexExtension + 1,
				this.indexOfSetting,
			),
			srcTypeSettings?.optionCaption,
			srcTypeSettings?.optional,
		);
	}
	settingElements() {
		return toCommandParams(
			this,
			this.elements.slice(this.indexOfSetting),
			{
				caption: "dataset option",
				display: (name) => name === "regTableInclude",
			},
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
export type ConvertResult = CommandParams & {
	jdbc: CommandParams;
};
export type Parameter =
	| ConvertParams
	| CompareParams
	| GenerateParams
	| RunParams
	| ParameterizeParams;
export type ConvertParams = {
	srcData: DatasetSource;
	convertResult: ConvertResult;
};
export type CompareParams = {
	elements: CommandParam[];
	newData: DatasetSource;
	oldData: DatasetSource;
	imageOption: CommandParams;
	convertResult: ConvertResult;
	expectData: DatasetSource;
};
export type GenerateParams = {
	elements: CommandParam[];
	srcData: DatasetSource;
	templateOption: CommandParams;
};
export type RunParams = {
	elements: CommandParam[];
	srcData: DatasetSource;
	templateOption: CommandParams;
	jdbcOption: CommandParams;
};
export type ParameterizeParams = {
	elements: CommandParam[];
	paramData: DatasetSource;
	templateOption: CommandParams;
};
const srcTypeDetail = new Map<
	string,
	{
		optionCaption: { caption: string; display: (_: string) => boolean };
		optional: (_: string) => boolean;
	}
>([
	[
		"table",
		{
			optionCaption: {
				caption: "table option",
				display: (name: string) => name === "useJdbcMetaData",
			},
			optional: (name: string) =>
				name !== "encoding" && !name.startsWith("jdbc"),
		},
	],
	[
		"sql",
		{
			optionCaption: {
				caption: "sql option",
				display: (name: string) => name === "useJdbcMetaData",
			},
			optional: (name: string) =>
				name !== "encoding" && !name.startsWith("jdbc"),
		},
	],
	[
		"csv",
		{
			optionCaption: {
				caption: "csv option",
				display: (name: string) => name === "headerName",
			},
			optional: (name: string) => name !== "encoding",
		},
	],
	[
		"csvq",
		{
			optionCaption: {
				caption: "csvq option",
				display: (name: string) => name === "headerName",
			},
			optional: (name: string) => name !== "encoding",
		},
	],
	[
		"reg",
		{
			optionCaption: {
				caption: "reg option",
				display: (name: string) => name === "headerName",
			},
			optional: (name: string) =>
				name !== "regDataSplit" && name !== "regHeaderSplit",
		},
	],
	[
		"fixed",
		{
			optionCaption: {
				caption: "fixed option",
				display: (name: string) => name === "headerName",
			},
			optional: (name: string) => name !== "fixedLength",
		},
	],
	[
		"xls",
		{
			optionCaption: {
				caption: "xls option",
				display: (name: string) => name === "headerName",
			},
			optional: (_: string) => true,
		},
	],
	[
		"xlsx",
		{
			optionCaption: {
				caption: "xlsx option",
				display: (name: string) => name === "headerName",
			},
			optional: (_: string) => true,
		},
	],
]);
function toCommandParams(
	srcData: CommandParams,
	elements: CommandParam[],
	optionCaption?: { caption: string; display: (_: string) => boolean },
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
