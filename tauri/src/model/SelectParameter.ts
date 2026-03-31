import type {
	CommandParam,
	ConvertResult,
	DatasetSource,
	GenerateElements,
	ImageOption,
	JdbcOption,
	ParameterizeElements,
	RunElements,
	TemplateOption,
} from "./CommandParam";

type RawParams = { prefix: string; elements: CommandParam[] };
import {
	ConvertResultImpl,
	DatasetSourceImpl,
	GenerateElementsImpl,
	ImageOptionImpl,
	JdbcOptionImpl,
	ParameterizeElementsImpl,
	RunElementsImpl,
	TemplateOptionImpl,
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
			const rawConvert = response as {
				srcData: RawParams;
				convertResult: RawParams & {
					jdbc?: RawParams;
				};
			};
			this.convert = {
				srcData: new DatasetSourceImpl(
					rawConvert.srcData.prefix,
					rawConvert.srcData.elements,
				),
				convertResult: new ConvertResultImpl(
					rawConvert.convertResult.prefix,
					rawConvert.convertResult.elements,
					rawConvert.convertResult.jdbc,
				),
			};
		}
		if (command === "compare") {
			const rawCompare = response as {
				elements: CommandParam[];
				newData: RawParams;
				oldData: RawParams;
				imageOption: RawParams;
				convertResult: RawParams;
				expectData: RawParams;
			};
			const findIn = (name: string) =>
				rawCompare.elements.find((e) => e.name === name);
			this.compare = {
				targetType: findIn("targetType"),
				setting: findIn("setting"),
				settingEncoding: findIn("settingEncoding"),
				newData: new DatasetSourceImpl(
					rawCompare.newData.prefix,
					rawCompare.newData.elements,
				),
				oldData: new DatasetSourceImpl(
					rawCompare.oldData.prefix,
					rawCompare.oldData.elements,
				),
				imageOption: new ImageOptionImpl(
					rawCompare.imageOption.prefix,
					rawCompare.imageOption.elements,
				),
				convertResult: new ConvertResultImpl(
					rawCompare.convertResult.prefix,
					rawCompare.convertResult.elements,
					undefined,
				),
				expectData: new DatasetSourceImpl(
					rawCompare.expectData.prefix,
					rawCompare.expectData.elements,
				),
			};
		}
		if (command === "generate") {
			const rawGenerate = response as unknown as {
				elements: CommandParam[];
				srcData: RawParams;
				templateOption?: RawParams;
			};
			this.generate = {
				commandElements: new GenerateElementsImpl("", rawGenerate.elements),
				srcData: new DatasetSourceImpl(
					rawGenerate.srcData.prefix,
					rawGenerate.srcData.elements,
				),
				templateOption: rawGenerate.templateOption
					? new TemplateOptionImpl(
							rawGenerate.templateOption.prefix,
							rawGenerate.templateOption.elements,
						)
					: undefined,
			};
		}
		if (command === "run") {
			const rawRun = response as unknown as {
				elements: CommandParam[];
				srcData: RawParams;
				templateOption?: RawParams;
				jdbcOption?: RawParams;
			};
			this.run = {
				commandElements: new RunElementsImpl("", rawRun.elements),
				srcData: new DatasetSourceImpl(
					rawRun.srcData.prefix,
					rawRun.srcData.elements,
				),
				templateOption: rawRun.templateOption
					? new TemplateOptionImpl(
							rawRun.templateOption.prefix,
							rawRun.templateOption.elements,
						)
					: undefined,
				jdbcOption: rawRun.jdbcOption
					? new JdbcOptionImpl(
							rawRun.jdbcOption.prefix,
							rawRun.jdbcOption.elements,
						)
					: undefined,
			};
		}
		if (command === "parameterize") {
			const rawParameterize = response as unknown as {
				elements: CommandParam[];
				paramData: RawParams;
				templateOption?: RawParams;
			};
			this.parameterize = {
				commandElements: new ParameterizeElementsImpl(
					"",
					rawParameterize.elements,
				),
				paramData: new DatasetSourceImpl(
					rawParameterize.paramData.prefix,
					rawParameterize.paramData.elements,
				),
				templateOption: rawParameterize.templateOption
					? new TemplateOptionImpl(
							rawParameterize.templateOption.prefix,
							rawParameterize.templateOption.elements,
						)
					: undefined,
			};
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
	targetType?: CommandParam;
	setting?: CommandParam;
	settingEncoding?: CommandParam;
	newData: DatasetSource;
	oldData: DatasetSource;
	imageOption: ImageOption;
	convertResult: ConvertResult;
	expectData: DatasetSource;
};
export type GenerateParams = {
	commandElements: GenerateElements;
	srcData: DatasetSource;
	templateOption?: TemplateOption;
};
export type RunParams = {
	commandElements: RunElements;
	srcData: DatasetSource;
	templateOption?: TemplateOption;
	jdbcOption?: JdbcOption;
};
export type ParameterizeParams = {
	commandElements: ParameterizeElements;
	paramData: DatasetSource;
	templateOption?: TemplateOption;
};
