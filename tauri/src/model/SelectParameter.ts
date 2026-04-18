import type {
	CommandOption,
	CommandOptions,
	DatasetSource,
	JdbcOption,
	ResultOption,
	TemplateOption,
} from "./CommandOption";
export type Command =
	| "convert"
	| "compare"
	| "generate"
	| "run"
	| "parameterize";
export class SelectParameter {
	readonly name: string;
	readonly command: Command;
	readonly options: Options;

	constructor(response: Options, command: Command, name: string) {
		this.name = name;
		this.command = command;
		this.options = response;
		this.options.command = this.command;
	}
}
export type Options =
	| ConvertOptions
	| CompareOptions
	| GenerateOptions
	| RunOptions
	| ParameterizeOptions;
export type ImageOption = CommandOptions & {
	threshold: CommandOption;
	pixelToleranceLevel: CommandOption;
	allowingPercentOfDifferentPixels: CommandOption;
	rectangleLineWidth: CommandOption;
	minimalRectangleSize: CommandOption;
	maximalRectangleCount: CommandOption;
	excludedAreas: CommandOption;
	drawExcludedRectangles: CommandOption;
	fillExcludedRectangles: CommandOption;
	percentOpacityExcludedRectangles: CommandOption;
	excludedRectangleColor: CommandOption;
	fillDifferenceRectangles: CommandOption;
	percentOpacityDifferenceRectangles: CommandOption;
	differenceRectangleColor: CommandOption;
};
export type ConvertOptions = {
	command: "convert";
	srcData: DatasetSource;
	convertResult: ResultOption;
};
export type CompareOptions = CommandOptions & {
	command: "compare";
	targetType: CommandOption;
	setting: CommandOption;
	settingEncoding: CommandOption;
	newData: DatasetSource;
	oldData: DatasetSource;
	imageOption: ImageOption;
	convertResult: ResultOption;
	expectData: DatasetSource;
};
export type GenerateOptions = CommandOptions & {
	command: "generate";
	generateType: CommandOption;
	unit: CommandOption;
	template: CommandOption;
	result: CommandOption;
	resultPath: CommandOption;
	outputEncoding: CommandOption;
	srcData: DatasetSource;
	templateOption?: TemplateOption;
};
export type RunOptions = CommandOptions & {
	command: "run";
	scriptType: CommandOption;
	baseDir: CommandOption;
	antTarget: CommandOption;
	srcData: DatasetSource;
	templateOption?: TemplateOption;
	jdbcOption?: JdbcOption;
};
export type ParameterizeOptions = CommandOptions & {
	command: "parameterize";
	unit: CommandOption;
	parameterize: CommandOption;
	ignoreFail: CommandOption;
	cmd: CommandOption;
	cmdParam: CommandOption;
	template: CommandOption;
	paramData: DatasetSource;
	templateOption?: TemplateOption;
};
