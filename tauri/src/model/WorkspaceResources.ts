export type DefaultPath =
	| "WORKSPACE"
	| "DATASET"
	| "RESULT"
	| "SETTING"
	| "TEMPLATE"
	| "PARAMETERIZE_TEMPLATE"
	| "JDBC"
	| "XLSX_SCHEMA";
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
	parameterizeTemplateBase: string;
	jdbcBase: string;
	xlsxSchemaBase: string;
};
export class WorkspaceContext {
	getPath(defaultPath: DefaultPath): string {
		switch (defaultPath) {
			case "DATASET":
				return this.datasetBase;
			case "RESULT":
				return this.resultBase;
			case "SETTING":
				return this.settingBase;
			case "TEMPLATE":
				return this.templateBase;
			case "PARAMETERIZE_TEMPLATE":
				return this.parameterizeTemplateBase;
			case "JDBC":
				return this.jdbcBase;
			case "XLSX_SCHEMA":
				return this.xlsxSchemaBase;
			default:
				return this.workspace;
		}
	}
	static create(): WorkspaceContext {
		return new WorkspaceContext("", "", "", "", "", "", "", "");
	}
	static from(builder: WorkspaceContextBuilder): WorkspaceContext {
		return new WorkspaceContext(
			builder.workspace,
			builder.datasetBase,
			builder.resultBase,
			builder.settingBase,
			builder.templateBase,
			builder.parameterizeTemplateBase,
			builder.jdbcBase,
			builder.xlsxSchemaBase,
		);
	}
	readonly workspace: string;
	readonly datasetBase: string;
	readonly resultBase: string;
	readonly settingBase: string;
	readonly templateBase: string;
	readonly parameterizeTemplateBase: string;
	readonly jdbcBase: string;
	readonly xlsxSchemaBase: string;
	constructor(
		workspace: string,
		datasetBase: string,
		resultBase: string,
		settingBase: string,
		templateBase: string,
		parameterizeTemplateBase: string,
		jdbcBase: string,
		xlsxSchemaBase: string,
	) {
		this.workspace = workspace;
		this.datasetBase = datasetBase;
		this.resultBase = resultBase;
		this.settingBase = settingBase;
		this.templateBase = templateBase;
		this.parameterizeTemplateBase = parameterizeTemplateBase;
		this.jdbcBase = jdbcBase;
		this.xlsxSchemaBase = xlsxSchemaBase;
	}
	with(overrides: Partial<WorkspaceContextBuilder>): WorkspaceContext {
		return new WorkspaceContext(
			overrides.workspace ?? this.workspace,
			overrides.datasetBase ?? this.datasetBase,
			overrides.resultBase ?? this.resultBase,
			overrides.settingBase ?? this.settingBase,
			overrides.templateBase ?? this.templateBase,
			overrides.parameterizeTemplateBase ?? this.parameterizeTemplateBase,
			overrides.jdbcBase ?? this.jdbcBase,
			overrides.xlsxSchemaBase ?? this.xlsxSchemaBase,
		);
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
		this.convert = convert ? convert : [];
		this.compare = compare ? compare : [];
		this.generate = generate ? generate : [];
		this.run = run ? run : [];
		this.parameterize = parameterize ? parameterize : [];
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
	datasetSettings?: string[];
	xlsxSchemas?: string[];
	jdbcFiles?: string[];
	templateFiles?: string[];
};
export class ResourcesSettings {
	static create(): ResourcesSettings {
		return new ResourcesSettings({});
	}
	static from(builder: ResourcesSettingsBuilder): ResourcesSettings {
		return new ResourcesSettings(builder);
	}
	readonly datasetSettings: string[];
	readonly xlsxSchemas: string[];
	readonly jdbcFiles: string[];
	readonly templateFiles: string[];
	constructor(builder: ResourcesSettingsBuilder) {
		this.datasetSettings = builder.datasetSettings ?? [];
		this.xlsxSchemas = builder.xlsxSchemas ?? [];
		this.jdbcFiles = builder.jdbcFiles ?? [];
		this.templateFiles = builder.templateFiles ?? [];
	}
	with(builder: ResourcesSettingsBuilder): ResourcesSettings {
		return new ResourcesSettings({
			...this,
			...builder,
		});
	}
}
