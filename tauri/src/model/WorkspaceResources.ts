export type DefaultPath =
	| "WORKSPACE"
	| "DATASET"
	| "RESULT"
	| "SETTING"
	| "TEMPLATE"
	| "PARAMETERIZE_TEMPLATE"
	| "JDBC"
	| "XLSX_SCHEMA"
	| "FIXED_COLUMN_DEF"
	| "SQL"
	| "TABLE"
	| "CSVQ";
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
	fixedColumnDefBase?: string;
	sqlBase?: string;
	tableBase?: string;
	csvqBase?: string;
	env?: string;
};
// 基準ディレクトリごとのWorkspaceContextの項目（WORKSPACEと未定義のものはworkspace）
const basePathKeys: Partial<
	Record<DefaultPath, keyof WorkspaceContextBuilder>
> = {
	DATASET: "datasetBase",
	RESULT: "resultBase",
	SETTING: "settingBase",
	TEMPLATE: "templateBase",
	PARAMETERIZE_TEMPLATE: "parameterizeTemplateBase",
	JDBC: "jdbcBase",
	XLSX_SCHEMA: "xlsxSchemaBase",
	FIXED_COLUMN_DEF: "fixedColumnDefBase",
	SQL: "sqlBase",
	TABLE: "tableBase",
	CSVQ: "csvqBase",
};
export class WorkspaceContext {
	getPath(defaultPath: DefaultPath): string {
		const key = basePathKeys[defaultPath];
		return key ? this[key] : this.workspace;
	}
	static create(): WorkspaceContext {
		return new WorkspaceContext({
			workspace: "",
			datasetBase: "",
			resultBase: "",
			settingBase: "",
			templateBase: "",
			parameterizeTemplateBase: "",
			jdbcBase: "",
			xlsxSchemaBase: "",
		});
	}
	static from(builder: WorkspaceContextBuilder): WorkspaceContext {
		return new WorkspaceContext(builder);
	}
	readonly workspace: string;
	readonly datasetBase: string;
	readonly resultBase: string;
	readonly settingBase: string;
	readonly templateBase: string;
	readonly parameterizeTemplateBase: string;
	readonly jdbcBase: string;
	readonly xlsxSchemaBase: string;
	readonly fixedColumnDefBase: string;
	readonly sqlBase: string;
	readonly tableBase: string;
	readonly csvqBase: string;
	readonly env: string;
	constructor(builder: WorkspaceContextBuilder) {
		this.workspace = builder.workspace;
		this.datasetBase = builder.datasetBase;
		this.resultBase = builder.resultBase;
		this.settingBase = builder.settingBase;
		this.templateBase = builder.templateBase;
		this.parameterizeTemplateBase = builder.parameterizeTemplateBase;
		this.jdbcBase = builder.jdbcBase;
		this.xlsxSchemaBase = builder.xlsxSchemaBase;
		this.fixedColumnDefBase = builder.fixedColumnDefBase ?? "";
		this.sqlBase = builder.sqlBase ?? "";
		this.tableBase = builder.tableBase ?? "";
		this.csvqBase = builder.csvqBase ?? "";
		this.env = builder.env ?? "";
	}
	// undefinedの項目は今の値を残す
	with(overrides: Partial<WorkspaceContextBuilder>): WorkspaceContext {
		const defined = Object.fromEntries(
			Object.entries(overrides).filter(([, value]) => value !== undefined),
		);
		return new WorkspaceContext({ ...this, ...defined });
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
	parameterizeTemplateFiles?: string[];
	fixedColumnDefs?: string[];
	sqlFiles?: string[];
	tableFiles?: string[];
	csvqFiles?: string[];
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
	readonly parameterizeTemplateFiles: string[];
	readonly fixedColumnDefs: string[];
	readonly sqlFiles: string[];
	readonly tableFiles: string[];
	readonly csvqFiles: string[];
	constructor(builder: ResourcesSettingsBuilder) {
		this.datasetSettings = builder.datasetSettings ?? [];
		this.xlsxSchemas = builder.xlsxSchemas ?? [];
		this.jdbcFiles = builder.jdbcFiles ?? [];
		this.templateFiles = builder.templateFiles ?? [];
		this.parameterizeTemplateFiles = builder.parameterizeTemplateFiles ?? [];
		this.fixedColumnDefs = builder.fixedColumnDefs ?? [];
		this.sqlFiles = builder.sqlFiles ?? [];
		this.tableFiles = builder.tableFiles ?? [];
		this.csvqFiles = builder.csvqFiles ?? [];
	}
	with(builder: ResourcesSettingsBuilder): ResourcesSettings {
		return new ResourcesSettings({
			...this,
			...builder,
		});
	}
}
