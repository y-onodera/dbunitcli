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
	static create(): WorkspaceContext {
		return new WorkspaceContext("", "", "", "", "", "", "");
	}
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
	metadataSetting?: string[];
	xlsxSchemas?: string[];
	jdbcFiles?: string[];
	templateFiles?: string[];
	queryFiles?: QueryFilesBuilder;
};
export class ResourcesSettings {
	static create(): ResourcesSettings {
		return new ResourcesSettings({});
	}
	readonly metadataSetting: string[];
	readonly xlsxSchemas: string[];
	readonly jdbcFiles: string[];
	readonly templateFiles: string[];
	readonly queryFiles: QueryFiles;
	constructor(builder: ResourcesSettingsBuilder) {
		this.metadataSetting = builder.metadataSetting ?? [];
		this.xlsxSchemas = builder.xlsxSchemas ?? [];
		this.jdbcFiles = builder.jdbcFiles ?? [];
		this.templateFiles = builder.templateFiles ?? [];
		this.queryFiles = new QueryFiles(builder.queryFiles ?? {});
	}
	with(builder: ResourcesSettingsBuilder): ResourcesSettings {
		return new ResourcesSettings({
			...this,
			...builder,
		});
	}
	querys(srcType: string | undefined): string[] {
		return this.queryFiles.of(srcType);
	}
}
export type QueryFilesBuilder = {
	sql?: string[];
	table?: string[];
	csvq?: string[];
};
export class QueryFiles {
	static create(): QueryFiles {
		return new QueryFiles({});
	}
	readonly sql: string[];
	readonly table: string[];
	readonly csvq: string[];
	constructor(builder: QueryFilesBuilder) {
		this.sql = builder.sql ?? [];
		this.table = builder.table ?? [];
		this.csvq = builder.csvq ?? [];
	}
	of(srcType: string | undefined): string[] {
		if (srcType === "sql") {
			return this.sql;
		}
		if (srcType === "table") {
			return this.table;
		}
		if (srcType === "csvq") {
			return this.csvq;
		}
		return [];
	}
	replace(type: string, files: string[]): QueryFiles {
		return new QueryFiles({
			...this,
			[type]: files,
		});
	}
}
