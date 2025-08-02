export type WorkspaceResources = {
	parameterList: ParameterListBuilder;
	resources: ResourcesSettings;
	context: WorkspaceContext;
};
export type WorkspaceContextBuilder = {
	workspace: string;
	datasetBase: string;
	resultBase: string;
	settingBase: string;
	templateBase: string;
	jdbcBase: string;
	xlsxSchemaBase: string;
};
export class WorkspaceContext {
	static from(builder: WorkspaceContextBuilder): WorkspaceContext {
		return new WorkspaceContext(
			builder.workspace,
			builder.datasetBase,
			builder.resultBase,
			builder.settingBase,
			builder.templateBase,
			builder.jdbcBase,
			builder.xlsxSchemaBase,
		);
	}
	readonly workspace: string;
	readonly datasetBase: string;
	readonly resultBase: string;
	readonly settingBase: string;
	readonly templateBase: string;
	readonly jdbcBase: string;
	readonly xlsxSchemaBase: string;
	constructor(
		workspace: string,
		datasetBase: string,
		resultBase: string,
		settingBase: string,
		templateBase: string,
		jdbcBase: string,
		xlsxSchemaBase: string,
	) {
		this.workspace = workspace;
		this.datasetBase = datasetBase;
		this.resultBase = resultBase;
		this.settingBase = settingBase;
		this.templateBase = templateBase;
		this.jdbcBase = jdbcBase;
		this.xlsxSchemaBase = xlsxSchemaBase;
	}
	static create(): WorkspaceContext {
		return new WorkspaceContext("", "", "", "", "", "", "");
	}
}
export type ParameterListBuilder = {
	convert: string[];
	compare: string[];
	generate: string[];
	run: string[];
	parameterize: string[];
};
export class ParameterList {
	readonly convert: string[];
	readonly compare: string[];
	readonly generate: string[];
	readonly run: string[];
	readonly parameterize: string[];
	constructor(
		convert: string[],
		compare: string[],
		generate: string[],
		run: string[],
		parameterize: string[],
	) {
		this.convert = convert;
		this.compare = compare;
		this.generate = generate;
		this.run = run;
		this.parameterize = parameterize;
	}
	static create(): ParameterList {
		return new ParameterList([], [], [], [], []);
	}
	static from(builder: ParameterListBuilder): ParameterList {
		return new ParameterList(
			builder.convert,
			builder.compare,
			builder.generate,
			builder.run,
			builder.parameterize,
		);
	}
	replace(command: string, menuList: string[]): ParameterList {
		return new ParameterList(
			command === "convert" ? menuList : this.convert,
			command === "compare" ? menuList : this.compare,
			command === "generate" ? menuList : this.generate,
			command === "run" ? menuList : this.run,
			command === "parameterize" ? menuList : this.parameterize,
		);
	}
}
export type ResourcesSettingsBuilder = {
	datasetSettings: string[];
	xlsxSchemas: string[];
	jdbcFiles: string[];
	templateFiles: string[];
};
export class ResourcesSettings {
	readonly datasetSettings: string[];
	readonly xlsxSchemas: string[];
	readonly jdbcFiles: string[];
	readonly templateFiles: string[];
	constructor(
		datasetSettings: string[],
		xlsxSchemas: string[],
		jdbcFiles: string[],
		templateFiles: string[],
	) {
		this.datasetSettings = datasetSettings;
		this.xlsxSchemas = xlsxSchemas;
		this.jdbcFiles = jdbcFiles;
		this.templateFiles = templateFiles;
	}
	static create(): ResourcesSettings {
		return new ResourcesSettings([], [], [], []);
	}
	static from(builder: ResourcesSettingsBuilder): ResourcesSettings {
		return new ResourcesSettings(
			builder.datasetSettings,
			builder.xlsxSchemas,
			builder.jdbcFiles,
			builder.templateFiles,
		);
	}
}
