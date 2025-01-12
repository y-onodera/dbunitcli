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
		}
		if (command === "compare") {
			this.compare = response as CompareParams;
		}
		if (command === "generate") {
			this.generate = response as GenerateParams;
		}
		if (command === "run") {
			this.run = response as RunParams;
		}
		if (command === "parameterize") {
			this.parameterize = response as ParameterizeParams;
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
};
export type CommandParams = {
	handleTypeSelect: () => Promise<void>;
	name: string;
	prefix: string;
	elements: CommandParam[];
};
export type ConvertResult = CommandParams & {
	jdbc: CommandParams;
};
export type DatasetSource = CommandParams & {
	jdbc: CommandParams;
	templateRender: CommandParams;
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