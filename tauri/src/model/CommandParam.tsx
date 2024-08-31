export type Attribute = {
	type: string;
	required: boolean;
	selectOption: string[];
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
