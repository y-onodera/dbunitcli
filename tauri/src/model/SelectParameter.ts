import type {
	CommandParam,
	CommandParams,
	ConvertResult,
	DatasetSource,
	JdbcOption,
	TemplateOption,
} from "./CommandParam";
export type Command =
	| "convert"
	| "compare"
	| "generate"
	| "run"
	| "parameterize";
export class SelectParameter {
	readonly name: string;
	readonly command: Command;
	readonly parameter: Parameter;

	constructor(response: Parameter, command: Command, name: string) {
		this.name = name;
		this.command = command;
		this.parameter = response;
		this.parameter.command = this.command;
	}
	currentParameter(): Parameter {
		return this.parameter;
	}
}
export type Parameter =
	| ConvertParams
	| CompareParams
	| GenerateParams
	| RunParams
	| ParameterizeParams;
type CompareElements = CommandParams & {
	targetType: CommandParam;
	setting: CommandParam;
	settingEncoding: CommandParam;
};
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
type GenerateElements = CommandParams & {
	generateType: CommandParam;
	unit: CommandParam;
	template: CommandParam;
	result: CommandParam;
	resultPath: CommandParam;
	outputEncoding: CommandParam;
};
type RunElements = CommandParams & {
	scriptType: CommandParam;
};
type ParameterizeElements = CommandParams & {
	unit: CommandParam;
	parameterize: CommandParam;
	ignoreFail: CommandParam;
	cmd: CommandParam;
	cmdParam: CommandParam;
	template: CommandParam;
};
export type ConvertParams = {
	command: "convert";
	srcData: DatasetSource;
	convertResult: ConvertResult;
};
export type CompareParams = CompareElements & {
	command: "compare";
	newData: DatasetSource;
	oldData: DatasetSource;
	imageOption: ImageOption;
	convertResult: ConvertResult;
	expectData: DatasetSource;
};
export type GenerateParams = GenerateElements & {
	command: "generate";
	srcData: DatasetSource;
	templateOption?: TemplateOption;
};
export type RunParams = RunElements & {
	command: "run";
	srcData: DatasetSource;
	templateOption?: TemplateOption;
	jdbcOption?: JdbcOption;
};
export type ParameterizeParams = ParameterizeElements & {
	command: "parameterize";
	paramData: DatasetSource;
	templateOption?: TemplateOption;
};
