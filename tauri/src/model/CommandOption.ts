export type DefaultPath =
	| "WORKSPACE"
	| "DATASET"
	| "RESULT"
	| "SETTING"
	| "TEMPLATE"
	| "PARAMETERIZE_TEMPLATE"
	| "JDBC"
	| "XLSX_SCHEMA";
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
};
export type SettingElements = CommandOptions & {
	setting: CommandOption;
	settingEncoding: CommandOption;
	regTableInclude: CommandOption;
	regTableExclude: CommandOption;
	loadData: CommandOption;
	includeMetaData: CommandOption;
};
export type DatasetSource = CommandOptions &
	SrcElements &
	SrcTypeOptions &
	SettingElements &
	(JdbcOption | undefined) &
	(TemplateOption | undefined);
export type SrcInfo = {
	srcPath: string;
	encoding?: string;
	regTableInclude: string;
	regTableExclude: string;
	recursive: string;
	regInclude: string;
	regExclude: string;
	extension: string;
};
export type DatasetSrcInfo = SrcInfo & {
	srcType?: SrcType;
	setting?: string;
	xlsxSchema: string;
	fixedLength: string;
	regHeaderSplit: string;
	regDataSplit: string;
	encoding: string;
	delimiter: string;
	ignoreQuoted: boolean;
	headerName: string;
	startRow: string;
	addFileInfo: boolean;
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
type FileSrcOptions = CommandOptions & {
	headerName: CommandOption;
	startRow: CommandOption;
	addFileInfo: CommandOption;
};
export type CsvOptions = FileSrcOptions & {
	srcType: Csv;
	delimiter: CommandOption;
	ignoreQuoted: CommandOption;
};
export type RegOptions = FileSrcOptions & {
	srcType: Reg;
	regDataSplit: CommandOption;
	regHeaderSplit: CommandOption;
};
export type FixedOptions = FileSrcOptions & {
	srcType: Fixed;
	fixedLength: CommandOption;
};
type ExcelOptions = FileSrcOptions & {
	xlsxSchema: CommandOption;
};
export type XlsOptions = ExcelOptions & {
	srcType: Xls;
};
export type XlsxOptions = ExcelOptions & {
	srcType: Xlsx;
};
type QueryOptions = CommandOptions & {
	headerName: CommandOption;
	addFileInfo: CommandOption;
	encoding: CommandOption;
	templateGroup: CommandOption;
	templateParameterAttribute: CommandOption;
	templateVarStart: CommandOption;
	templateVarStop: CommandOption;
};
export type CsvqOptions = QueryOptions & {
	srcType: Csvq;
};
type DBTypeOptions = QueryOptions & {
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
	srcType?: File;
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
export function isCsvqType(options: SrcTypeOptions): options is CsvqOptions {
	return options.srcType?.value === "csvq";
}
export function isNoneType(options: SrcTypeOptions): options is NoneOptions {
	return options.srcType?.value === "none";
}
export function isFileType(options: SrcTypeOptions): options is FileOptions {
	return !options.srcType || options.srcType.value === "file";
}
export function isDirType(options: SrcTypeOptions): options is DirOptions {
	return options.srcType?.value === "dir";
}
export function buildDatasetSrcInfo(datasrc: DatasetSource): DatasetSrcInfo {
	const xlsxSchema =
		isXlsType(datasrc) || isXlsxType(datasrc) ? datasrc.xlsxSchema.value : "";
	const fixedLength = isFixedType(datasrc) ? datasrc.fixedLength.value : "";
	const regHeaderSplit = isRegType(datasrc) ? datasrc.regHeaderSplit.value : "";
	const regDataSplit = isRegType(datasrc) ? datasrc.regDataSplit.value : "";
	const delimiter = isCsvType(datasrc) ? datasrc.delimiter.value : "";
	const ignoreQuoted = isCsvType(datasrc) ? datasrc.ignoreQuoted.value : "";
	const startRow =
		isSqlType(datasrc) ||
		isCsvqType(datasrc) ||
		isTableType(datasrc) ||
		isNoneType(datasrc) ||
		isFileType(datasrc) ||
		isDirType(datasrc)
			? ""
			: datasrc.startRow.value;
	const headerName =
		isNoneType(datasrc) || isFileType(datasrc) || isDirType(datasrc)
			? ""
			: datasrc.headerName.value;
	const addFileInfo =
		isNoneType(datasrc) || isFileType(datasrc) || isDirType(datasrc)
			? false
			: datasrc.addFileInfo.value === "true";
	return {
		srcPath: datasrc.src.value,
		encoding: datasrc.encoding?.value || "",
		regInclude: datasrc.regInclude.value,
		regExclude: datasrc.regExclude.value,
		recursive: datasrc.recursive.value,
		extension: datasrc.extension?.value || "",
		regTableInclude: datasrc.regTableInclude.value,
		regTableExclude: datasrc.regTableExclude.value,
		srcType: datasrc.srcType?.value,
		setting: datasrc.setting.value,
		xlsxSchema: xlsxSchema,
		fixedLength: fixedLength,
		regHeaderSplit: regHeaderSplit,
		regDataSplit: regDataSplit,
		delimiter: delimiter,
		ignoreQuoted: ignoreQuoted === "true",
		headerName: headerName,
		startRow: startRow,
		addFileInfo: addFileInfo,
	};
}
