import type {
	CommandParam,
	CommandParams,
	DatasetSource,
} from "./CommandParam";
import { DatasetSourceImpl } from "./CommandParam";

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
