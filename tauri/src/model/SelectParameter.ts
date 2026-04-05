import type {
	CommandParam,
	CommandParams,
	ConvertResult,
	DatasetSource,
	JdbcOption,
	TemplateOption,
} from "./CommandParam";

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
	}
	currentParameter(): Parameter {
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
	srcData: DatasetSource;
	convertResult: ConvertResult;
};
export type CompareParams = CompareElements & {
	newData: DatasetSource;
	oldData: DatasetSource;
	imageOption: ImageOption;
	convertResult: ConvertResult;
	expectData: DatasetSource;
};
export type GenerateParams = GenerateElements & {
	srcData: DatasetSource;
	templateOption?: TemplateOption;
};
export type RunParams = RunElements & {
	srcData: DatasetSource;
	templateOption?: TemplateOption;
	jdbcOption?: JdbcOption;
};
export type ParameterizeParams = ParameterizeElements & {
	paramData: DatasetSource;
	templateOption?: TemplateOption;
};
