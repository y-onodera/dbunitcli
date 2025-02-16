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
		if (command === "convert") {
			this.convert = response as ConvertParams;
			setFunction(this.convert.srcData);
		}
		if (command === "compare") {
			this.compare = response as CompareParams;
			setFunction(this.compare.newData);
			setFunction(this.compare.oldData);
			setFunction(this.compare.expectData);
		}
		if (command === "generate") {
			this.generate = response as GenerateParams;
			setFunction(this.generate.srcData);
		}
		if (command === "run") {
			this.run = response as RunParams;
			setFunction(this.run.srcData);
		}
		if (command === "parameterize") {
			this.parameterize = response as ParameterizeParams;
			setFunction(this.parameterize.paramData);
		}
		this.command = command;
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
export type DefaultPath = "WORKSPACE" | "DATASET" | "RESULT" | "SETTING" | "TEMPLATE" | "JDBC" | "XLSX_SCHEMA"
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
	handleTypeSelect: () => Promise<void>;
	name: string;
	prefix: string;
	elements: CommandParam[];
	optionCaption?: { caption: string, display: (_: string) => boolean };
	optional?: (_: string) => boolean;
};
export type ConvertResult = CommandParams & {
	jdbc: CommandParams;
};
export type DatasetSource = CommandParams & {
	srcElements: () => CommandParams;
	srcTypeSettings: () => CommandParams;
	settingElements: () => CommandParams;
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
const traversaldetail = ["recursive", "regInclude", "regExclude", "extension"]
const datasetdetail = ["regTableInclude", "regTableExclude", "loadData", "includeMetaData"]
const srcTypeDetail = new Map<string, { optionCaption: { caption: string, display: (_: string) => boolean }, optional: (_: string) => boolean }>([
	["table", {
		optionCaption: { caption: "table option", display: (name: string) => name === "headerName" },
		optional: (name: string) => name !== "encoding" && !name.startsWith("jdbc")
	}],
	["sql", {
		optionCaption: { caption: "sql option", display: (name: string) => name === "headerName" },
		optional: (name: string) => name !== "encoding" && !name.startsWith("jdbc")
	}],
	["csv", {
		optionCaption: { caption: "csv option", display: (name: string) => name === "headerName" },
		optional: (name: string) => name !== "encoding"
	}],
	["csvq", {
		optionCaption: { caption: "csvq option", display: (name: string) => name === "headerName" },
		optional: (name: string) => name !== "encoding"
	}],
	["reg", {
		optionCaption: { caption: "reg option", display: (_: string) => false },
		optional: (_: string) => false
	}],
	["fixed", {
		optionCaption: { caption: "fixed option", display: (_: string) => false },
		optional: (_: string) => false
	}],
	["xls", {
		optionCaption: { caption: "xls option", display: (name: string) => name === "headerName" },
		optional: (_: string) => true
	}],
	["xlsx", {
		optionCaption: { caption: "xlsx option", display: (name: string) => name === "headerName" },
		optional: (_: string) => true
	}]
]);
function setFunction(srcData: DatasetSource) {
	let indexOfSetting = -1;
	let indexExtension = -1;
	let indexRegExclude = -1;
	let srcType = ""
	for (let i = 0; i < srcData.elements.length; i++) {
		if (srcData.elements[i].name === "setting") {
			indexOfSetting = i;
		}
		if (srcData.elements[i].name === "extension") {
			indexExtension = i;
		}
		if (srcData.elements[i].name === "regExclude") {
			indexRegExclude = i;
		}
		if (srcData.elements[i].name === "srcType") {
			srcType = srcData.elements[i].value;
		}
	}
	srcData.srcElements = () => {
		return toCommandParams(srcData, srcData.elements.slice(0, indexExtension === -1 ? indexRegExclude + 1 : indexExtension + 1)
			, { caption: "traversal option", display: (name) => name === "recursive" }
			, (param: string) => traversaldetail.includes(param)
		);
	}
	srcData.srcTypeSettings = () => {
		const srcTypeSettings = srcTypeDetail.get(srcType);
		return toCommandParams(srcData, srcData.elements.slice(indexExtension === -1 ? indexRegExclude + 1 : indexExtension + 1, indexOfSetting)
			, srcTypeSettings?.optionCaption
			, srcTypeSettings?.optional
		);
	}
	srcData.settingElements = () => {
		return toCommandParams(srcData, srcData.elements.slice(indexOfSetting)
			, { caption: "dataset option", display: (name) => name === "regTableInclude" }
			, (param: string) => datasetdetail.includes(param)
		);
	}
}
function toCommandParams(srcData: CommandParams, elements: CommandParam[]
	, optionCaption?: { caption: string, display: (_: string) => boolean }, optional?: (_: string) => boolean
): CommandParams {
	return {
		handleTypeSelect: srcData.handleTypeSelect
		, name: srcData.name
		, prefix: srcData.prefix
		, elements: elements
		, optionCaption: optionCaption
		, optional: optional
	}
}