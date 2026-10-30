import type { DatasetSetting } from "./DatasetSettings";
import type { DefaultPath } from "./WorkspaceResources";
import type { XlsxSchema } from "./XlsxSchema";

export type Attribute = {
	type: string;
	required: boolean;
	selectOption: string[];
	defaultPath: DefaultPath;
};
export type CommandOption = {
	name: string;
	value: string;
	attribute: Attribute;
	optional: boolean;
};
export type CommandOptions = {
	prefix: string;
};
export type SrcType =
	| "none"
	| "csv"
	| "fixed"
	| "reg"
	| "xls"
	| "xlsx"
	| "table"
	| "sql"
	| "csvq"
	| "file"
	| "dir";
export type SrcTypeOption =
	| None
	| Csv
	| Fixed
	| Reg
	| Xls
	| Xlsx
	| Table
	| Sql
	| Csvq
	| File
	| Dir;
export type None = Omit<CommandOption, "value"> & {
	value: "none";
};
export type Csv = Omit<CommandOption, "value"> & {
	value: "csv";
};
export type Fixed = Omit<CommandOption, "value"> & {
	value: "fixed";
};
export type Reg = Omit<CommandOption, "value"> & {
	value: "reg";
};
export type Xls = Omit<CommandOption, "value"> & {
	value: "xls";
};
export type Xlsx = Omit<CommandOption, "value"> & {
	value: "xlsx";
};
export type File = Omit<CommandOption, "value"> & {
	value: "file";
};
export type Dir = Omit<CommandOption, "value"> & {
	value: "dir";
};
export type Table = Omit<CommandOption, "value"> & {
	value: "table";
};
export type Sql = Omit<CommandOption, "value"> & {
	value: "sql";
};
export type Csvq = Omit<CommandOption, "value"> & {
	value: "csvq";
};
export type SrcTypeOptions =
	| CsvOptions
	| CsvqOptions
	| TableOptions
	| SqlOptions
	| RegOptions
	| FixedOptions
	| XlsOptions
	| XlsxOptions
	| NoneOptions
	| FileOptions
	| DirOptions;
export type SrcElements = CommandOptions & {
	src: CommandOption;
	encoding?: CommandOption;
	recursive: CommandOption;
	regInclude: CommandOption;
	regExclude: CommandOption;
	extension?: CommandOption;
	headerName?: CommandOption;
	startRow?: CommandOption;
	addFileInfo?: CommandOption;
	loadData: CommandOption;
	includeMetaData: CommandOption;
	regTableInclude: CommandOption;
	regTableExclude: CommandOption;
	setting: CommandOption;
	settingEncoding: CommandOption;
	settingTargetOnly: CommandOption;
};
export type DatasetSource = CommandOptions &
	SrcElements &
	SrcTypeOptions &
	(JdbcOption | undefined) &
	(TemplateOption | undefined);
// データセット読込フォームのsrc要素の値（オプション名→値）。sidecarへはそのまま-src.<オプション名>として渡す
export type DatasetSrcInfo = {
	values: Record<string, string>;
	// 未保存の設定1件。指定時はsettingより優先してこの設定だけを適用する
	inlineSetting?: DatasetSetting;
	// 未保存のxlsxスキーマ。指定時はxlsxSchemaより優先してこのスキーマの対象だけを読む
	inlineXlsxSchema?: XlsxSchema;
};
export type JdbcOption = CommandOptions & {
	jdbcProperties: CommandOption;
	jdbcUrl: CommandOption;
	jdbcUser: CommandOption;
	jdbcPass: CommandOption;
};
export type ResultOption = CommandOptions & {
	resultType: CommandOption;
	result: CommandOption;
	resultPath: CommandOption;
	exportEmptyTable: CommandOption;
	exportHeader: CommandOption;
	outputEncoding?: CommandOption;
	excelTable?: CommandOption;
	format?: CommandOption;
	outputExtension?: CommandOption;
	fixedColumnDef?: CommandOption;
	fixedLengthType?: CommandOption;
	parallel?: CommandOption;
	op?: CommandOption;
	jdbc?: JdbcOption;
};
export type TemplateOption = CommandOptions & {
	encoding: CommandOption;
	templateGroup: CommandOption;
	templateParameterAttribute: CommandOption;
	templateVarStart: CommandOption;
	templateVarStop: CommandOption;
};
export type CsvOptions = CommandOptions & {
	srcType: Csv;
	delimiter: CommandOption;
	ignoreQuoted: CommandOption;
};
export type RegOptions = CommandOptions & {
	srcType: Reg;
	regDataSplit: CommandOption;
	regHeaderSplit: CommandOption;
};
export type FixedOptions = CommandOptions & {
	srcType: Fixed;
	fixedLength: CommandOption;
	fixedLengthType: CommandOption;
};
type ExcelOptions = CommandOptions & {
	xlsxSchema: CommandOption;
};
export type XlsOptions = ExcelOptions & {
	srcType: Xls;
};
export type XlsxOptions = ExcelOptions & {
	srcType: Xlsx;
};
export type CsvqOptions = CommandOptions & {
	srcType: Csvq;
};
type DBTypeOptions = CommandOptions & {
	useJdbcMetaData: CommandOption;
};
export type SqlOptions = DBTypeOptions & {
	srcType: Sql;
};
export type TableOptions = DBTypeOptions & {
	srcType: Table;
};
export type NoneOptions = CommandOptions & {
	srcType: None;
};
export type FileOptions = CommandOptions & {
	srcType: File;
};
export type DirOptions = CommandOptions & {
	srcType: Dir;
};
export function isCsvType(options: SrcTypeOptions): options is CsvOptions {
	return options.srcType?.value === "csv";
}
export function isFixedType(options: SrcTypeOptions): options is FixedOptions {
	return options.srcType?.value === "fixed";
}
export function isRegType(options: SrcTypeOptions): options is RegOptions {
	return options.srcType?.value === "reg";
}
export function isXlsType(options: SrcTypeOptions): options is XlsOptions {
	return options.srcType?.value === "xls";
}
export function isXlsxType(options: SrcTypeOptions): options is XlsxOptions {
	return options.srcType?.value === "xlsx";
}
export function isSqlType(options: SrcTypeOptions): options is SqlOptions {
	return options.srcType?.value === "sql";
}
export function isTableType(options: SrcTypeOptions): options is TableOptions {
	return options.srcType?.value === "table";
}
export function buildDatasetSrcInfo(datasrc: DatasetSource): DatasetSrcInfo {
	const values: Record<string, string> = {};
	for (const element of Object.values(datasrc)) {
		if (isCommandOption(element)) {
			values[element.name] = element.value;
		}
	}
	return { values };
}

function isCommandOption(value: unknown): value is CommandOption {
	return typeof value === "object" && value !== null && "name" in value;
}

export function srcTypeOf(info: DatasetSrcInfo): SrcType | undefined {
	return info.values.srcType as SrcType | undefined;
}

// 一部のsrc要素の値だけを差し替える
export function withValues(
	info: DatasetSrcInfo,
	values: Record<string, string>,
): DatasetSrcInfo {
	return { ...info, values: { ...info.values, ...values } };
}

// settingを適用せずに読む（settingが無ければsettingTargetOnlyでは絞らない）
export function withoutSetting(info: DatasetSrcInfo): DatasetSrcInfo {
	return withValues(info, { setting: "" });
}

// 入力候補（テーブル名・カラム名）の取得用。settingの対象外のテーブルも候補にする
export function withAllTables(info: DatasetSrcInfo): DatasetSrcInfo {
	return withValues(info, { settingTargetOnly: "false" });
}
