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
export type CommandParam = {
	name: string;
	value: string;
	attribute: Attribute;
	optional: boolean;
};
export type CommandParams = {
	prefix: string;
};
export type SrcTypeParam =
	| NoneParam
	| CsvParam
	| FixedParam
	| RegParam
	| XlsParam
	| XlsxParam
	| TableParam
	| SqlParam
	| CsvqParam
	| FileParam
	| DirParam;
export type NoneParam = Omit<CommandParam, "value"> & {
	value: "none";
};
export type CsvParam = Omit<CommandParam, "value"> & {
	value: "csv";
};
export type FixedParam = Omit<CommandParam, "value"> & {
	value: "fixed";
};
export type RegParam = Omit<CommandParam, "value"> & {
	value: "reg";
};
export type XlsParam = Omit<CommandParam, "value"> & {
	value: "xls";
};
export type XlsxParam = Omit<CommandParam, "value"> & {
	value: "xlsx";
};
export type FileParam = Omit<CommandParam, "value"> & {
	value: "file";
};
export type DirParam = Omit<CommandParam, "value"> & {
	value: "dir";
};
export type TableParam = Omit<CommandParam, "value"> & {
	value: "table";
};
export type SqlParam = Omit<CommandParam, "value"> & {
	value: "sql";
};
export type CsvqParam = Omit<CommandParam, "value"> & {
	value: "csvq";
};
export type SrcType =
	| "none"
	| "csv"
	| "fixed"
	| "reg"
	| "xls"
	| "xlsx"
	| "file"
	| "dir"
	| "table"
	| "sql"
	| "csvq";
export type SrcTypeSettings =
	| CsvTypeSettings
	| CsvqTypeSettings
	| TableTypeSettings
	| SqlTypeSettings
	| RegTypeSettings
	| FixedTypeSettings
	| XlsTypeSettings
	| XlsxTypeSettings
	| NoneTypeSettings
	| FileTypeSettings
	| DirTypeSettings;
export type SrcElements = CommandParams & {
	src: CommandParam;
	encoding?: CommandParam;
	recursive: CommandParam;
	regInclude: CommandParam;
	regExclude: CommandParam;
	extension?: CommandParam;
};
export type SettingElements = CommandParams & {
	setting: CommandParam;
	settingEncoding: CommandParam;
	regTableInclude: CommandParam;
	regTableExclude: CommandParam;
	loadData: CommandParam;
	includeMetaData: CommandParam;
};
export type DatasetSource = CommandParams &
	SrcElements &
	SrcTypeSettings &
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
export function isCsvType(
	settings: SrcTypeSettings,
): settings is CsvTypeSettings {
	return settings.srcType?.value === "csv";
}
export function isFixedType(
	settings: SrcTypeSettings,
): settings is FixedTypeSettings {
	return settings.srcType?.value === "fixed";
}
export function isRegType(
	settings: SrcTypeSettings,
): settings is RegTypeSettings {
	return settings.srcType?.value === "reg";
}
export function isXlsType(
	settings: SrcTypeSettings,
): settings is XlsTypeSettings {
	return settings.srcType?.value === "xls";
}
export function isXlsxType(
	settings: SrcTypeSettings,
): settings is XlsxTypeSettings {
	return settings.srcType?.value === "xlsx";
}
export function isSqlType(
	settings: SrcTypeSettings,
): settings is SqlTypeSettings {
	return settings.srcType?.value === "sql";
}
export function isTableType(
	settings: SrcTypeSettings,
): settings is TableTypeSettings {
	return settings.srcType?.value === "table";
}
export function isCsvqType(
	settings: SrcTypeSettings,
): settings is CsvqTypeSettings {
	return settings.srcType?.value === "csvq";
}
export function isNoneType(
	settings: SrcTypeSettings,
): settings is NoneTypeSettings {
	return settings.srcType?.value === "none";
}
export function isFileType(
	settings: SrcTypeSettings,
): settings is FileTypeSettings {
	return !settings.srcType || settings.srcType.value === "file";
}
export function isDirType(
	settings: SrcTypeSettings,
): settings is DirTypeSettings {
	return settings.srcType?.value === "dir";
}
export function buildDatasetSrcInfo(datasrc: DatasetSource): DatasetSrcInfo {
	const xlsxSchema =
		isXlsType(datasrc) || isXlsxType(datasrc) ? datasrc.xlsxSchema.value : "";
	const fixedLength = isFixedType(datasrc) ? datasrc.fixedLength.value : "";
	const regHeaderSplit = isRegType(datasrc) ? datasrc.regHeaderSplit.value : "";
	const regDataSplit = isRegType(datasrc) ? datasrc.regDataSplit.value : "";
	const delimiter = isCsvType(datasrc) ? datasrc.delimiter.value : "";
	const ignoreQuoted = isCsvType(datasrc) ? datasrc.ignoreQuoted.value : "";
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
		headerName: datasrc.headerName?.value || "",
		startRow: datasrc.startRow?.value || "",
		addFileInfo: datasrc.addFileInfo?.value === "true",
	};
}
export type JdbcOption = CommandParams & {
	jdbcProperties: CommandParam;
	jdbcUrl: CommandParam;
	jdbcUser: CommandParam;
	jdbcPass: CommandParam;
};
export type ConvertResult = CommandParams & {
	resultType: CommandParam;
	result: CommandParam;
	resultPath: CommandParam;
	exportEmptyTable: CommandParam;
	exportHeader: CommandParam;
	outputEncoding?: CommandParam;
	excelTable?: CommandParam;
	jdbc?: JdbcOption;
};
export type TemplateOption = CommandParams & {
	encoding: CommandParam;
	templateGroup: CommandParam;
	templateParameterAttribute: CommandParam;
	templateVarStart: CommandParam;
	templateVarStop: CommandParam;
};
export type CsvTypeSettings = CommandParams & {
	srcType: CsvParam;
	headerName: CommandParam;
	startRow: CommandParam;
	addFileInfo: CommandParam;
	delimiter: CommandParam;
	ignoreQuoted: CommandParam;
};
export type CsvqTypeSettings = CommandParams & {
	srcType: CsvqParam;
	headerName: CommandParam;
	startRow?: undefined;
	addFileInfo: CommandParam;
	encoding: CommandParam;
	templateGroup: CommandParam;
	templateParameterAttribute: CommandParam;
	templateVarStart: CommandParam;
	templateVarStop: CommandParam;
};
type DBTypeSettings = CommandParams & {
	headerName: CommandParam;
	startRow?: undefined;
	addFileInfo: CommandParam;
	useJdbcMetaData: CommandParam;
	templateGroup: CommandParam;
	templateParameterAttribute: CommandParam;
	templateVarStart: CommandParam;
	templateVarStop: CommandParam;
};
export type SqlTypeSettings = DBTypeSettings & {
	srcType: SqlParam;
};
export type TableTypeSettings = DBTypeSettings & {
	srcType: TableParam;
};
export type RegTypeSettings = CommandParams & {
	srcType: RegParam;
	headerName: CommandParam;
	startRow: CommandParam;
	addFileInfo: CommandParam;
	regDataSplit: CommandParam;
	regHeaderSplit: CommandParam;
};
export type FixedTypeSettings = CommandParams & {
	srcType: FixedParam;
	headerName: CommandParam;
	startRow: CommandParam;
	addFileInfo: CommandParam;
	fixedLength: CommandParam;
};
type ExcelTypeSettings = CommandParams & {
	headerName: CommandParam;
	startRow: CommandParam;
	addFileInfo: CommandParam;
	xlsxSchema: CommandParam;
};
export type XlsTypeSettings = ExcelTypeSettings & {
	srcType: XlsParam;
};
export type XlsxTypeSettings = ExcelTypeSettings & {
	srcType: XlsxParam;
};
export type NoSettings = CommandParams & {
	headerName?: undefined;
	startRow?: undefined;
	addFileInfo?: undefined;
};
export type NoneTypeSettings = NoSettings & {
	srcType: NoneParam;
};
export type FileTypeSettings = NoSettings & {
	srcType?: FileParam;
};
export type DirTypeSettings = NoSettings & {
	srcType: DirParam;
};
