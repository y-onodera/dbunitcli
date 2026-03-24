import type {
	CommandParam,
	CommandParams,
	DatasetSource,
	GenerateElements,
	JdbcOption,
	ParameterizeElements,
	RunElements,
	TemplateOption,
} from "./CommandParam";
import {
	DatasetSourceImpl,
	GenerateElementsImpl,
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
			const rawGenerate = response as {
				elements: CommandParam[];
				srcData: DatasetSource;
				templateOption?: TemplateOption;
			};
			this.generate = {
				commandElements: new GenerateElementsImpl(
					"generate",
					"",
					rawGenerate.elements,
				),
				srcData: new DatasetSourceImpl(
					rawGenerate.srcData.name,
					rawGenerate.srcData.prefix,
					rawGenerate.srcData.elements,
				),
				templateOption: rawGenerate.templateOption
					? new TemplateOptionImpl(
							rawGenerate.templateOption.prefix,
							rawGenerate.templateOption.prefix,
							rawGenerate.templateOption.elements,
						)
					: undefined,
			};
		}
		if (command === "run") {
			const rawRun = response as {
				elements: CommandParam[];
				srcData: DatasetSource;
				templateOption?: TemplateOption;
				jdbcOption?: JdbcOption;
			};
			this.run = {
				commandElements: new RunElementsImpl("run", "", rawRun.elements),
				srcData: new DatasetSourceImpl(
					rawRun.srcData.name,
					rawRun.srcData.prefix,
					rawRun.srcData.elements,
				),
				templateOption: rawRun.templateOption
					? new TemplateOptionImpl(
							rawRun.templateOption.prefix,
							rawRun.templateOption.prefix,
							rawRun.templateOption.elements,
						)
					: undefined,
				jdbcOption: rawRun.jdbcOption
					? new JdbcOptionImpl(
							rawRun.jdbcOption.prefix,
							rawRun.jdbcOption.prefix,
							rawRun.jdbcOption.elements,
						)
					: undefined,
			};
		}
		if (command === "parameterize") {
			const rawParameterize = response as {
				elements: CommandParam[];
				paramData: DatasetSource;
				templateOption?: TemplateOption;
			};
			this.parameterize = {
				commandElements: new ParameterizeElementsImpl(
					"parameterize",
					"",
					rawParameterize.elements,
				),
				paramData: new DatasetSourceImpl(
					rawParameterize.paramData.name,
					rawParameterize.paramData.prefix,
					rawParameterize.paramData.elements,
				),
				templateOption: rawParameterize.templateOption
					? new TemplateOptionImpl(
							rawParameterize.templateOption.prefix,
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
