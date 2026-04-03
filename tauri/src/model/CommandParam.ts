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
export type SrcTypeSettings =
	| CsvTypeSettings
	| CsvqTypeSettings
	| TableSqlTypeSettings
	| RegTypeSettings
	| FixedTypeSettings
	| XlsTypeSettings;
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
	srcType: string;
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
export type SrcElements = CommandParams & {
	srcType: CommandParam;
	src: CommandParam;
	encoding?: CommandParam;
	recursive: CommandParam;
	regInclude: CommandParam;
	regExclude: CommandParam;
	extension: CommandParam;
};
export type SettingElements = CommandParams & {
	setting: CommandParam;
	settingEncoding: CommandParam;
	regTableInclude: CommandParam;
	regTableExclude: CommandParam;
	loadData: CommandParam;
	includeMetaData: CommandParam;
};
export type DatasetSource = CommandParams & {
	srcElements: SrcElements;
	srcTypeSettings: SrcTypeSettings;
	settingElements: SettingElements;
	jdbcOption: JdbcOption | undefined;
	templateOption: TemplateOption | undefined;
};
export function buildDatasetSrcInfo(datasrc: DatasetSource): DatasetSrcInfo {
	const xlsxSchema =
		datasrc.srcElements.srcType.name === "xlsx" ||
		datasrc.srcElements.srcType.name === "xls"
			? (datasrc.srcTypeSettings as XlsTypeSettings).xlsxSchema.value
			: "";
	const fixedLength =
		datasrc.srcElements.srcType.name === "fixed"
			? (datasrc.srcTypeSettings as FixedTypeSettings).fixedLength.value
			: "";
	const regHeaderSplit =
		datasrc.srcElements.srcType.name === "reg"
			? (datasrc.srcTypeSettings as RegTypeSettings).regHeaderSplit.value
			: "";
	const regDataSplit =
		datasrc.srcElements.srcType.name === "reg"
			? (datasrc.srcTypeSettings as RegTypeSettings).regDataSplit.value
			: "";
	const delimiter =
		datasrc.srcElements.srcType.name === "csv"
			? (datasrc.srcTypeSettings as CsvTypeSettings).delimiter.value
			: "";
	const ignoreQuoted =
		datasrc.srcElements.srcType.name === "csv"
			? (datasrc.srcTypeSettings as CsvTypeSettings).ignoreQuoted.value
			: "";
	return {
		srcPath: datasrc.srcElements.src.value,
		encoding: datasrc.srcElements.encoding?.value || "",
		regInclude: datasrc.srcElements.regInclude.value,
		regExclude: datasrc.srcElements.regExclude.value,
		recursive: datasrc.srcElements.recursive.value,
		extension: datasrc.srcElements.extension.value,
		regTableInclude: datasrc.settingElements.regTableInclude.value,
		regTableExclude: datasrc.settingElements.regTableExclude.value,
		srcType: datasrc.srcElements.srcType.value,
		setting: datasrc.settingElements.setting.value,
		xlsxSchema: xlsxSchema,
		fixedLength: fixedLength,
		regHeaderSplit: regHeaderSplit,
		regDataSplit: regDataSplit,
		delimiter: delimiter,
		ignoreQuoted: ignoreQuoted === "true",
		headerName: datasrc.srcTypeSettings.headerName.value,
		startRow: datasrc.srcTypeSettings.startRow?.value || "",
		addFileInfo: datasrc.srcTypeSettings.addFileInfo.value === "true",
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
	headerName: CommandParam;
	startRow: CommandParam;
	addFileInfo: CommandParam;
	delimiter: CommandParam;
	ignoreQuoted: CommandParam;
};
export type CsvqTypeSettings = CommandParams & {
	headerName: CommandParam;
	startRow: undefined;
	addFileInfo: CommandParam;
	encoding: CommandParam;
	templateGroup: CommandParam;
	templateParameterAttribute: CommandParam;
	templateVarStart: CommandParam;
	templateVarStop: CommandParam;
};
export type TableSqlTypeSettings = CommandParams & {
	headerName: CommandParam;
	startRow: undefined;
	addFileInfo: CommandParam;
	useJdbcMetaData: CommandParam;
	templateGroup: CommandParam;
	templateParameterAttribute: CommandParam;
	templateVarStart: CommandParam;
	templateVarStop: CommandParam;
};
export type RegTypeSettings = CommandParams & {
	headerName: CommandParam;
	startRow: CommandParam;
	addFileInfo: CommandParam;
	regDataSplit: CommandParam;
	regHeaderSplit: CommandParam;
};
export type FixedTypeSettings = CommandParams & {
	headerName: CommandParam;
	startRow: CommandParam;
	addFileInfo: CommandParam;
	fixedLength: CommandParam;
};
export type XlsTypeSettings = CommandParams & {
	headerName: CommandParam;
	startRow: CommandParam;
	addFileInfo: CommandParam;
	xlsxSchema: CommandParam;
};
