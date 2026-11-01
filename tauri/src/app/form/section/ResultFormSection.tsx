import type { CommandOption, ResultOption } from "../../../model/CommandOption";
import Check from "./element/Check";
import EncodingText from "./element/EncodingText";
import FileText from "./element/FileText";
import FixedColumnDefText from "./element/FixedColumnDefText";
import PlainText from "./element/PlainText";
import Select from "./element/Select";
import JdbcFormSection from "./JdbcFormSection";

const EXCEL_TABLE_TYPES = ["SHEET", "BOOK"];

// 環境変数などSHEET・BOOK以外の値が設定済みなら、消えないよう選択肢に残す
function excelTableElement(excelTable: CommandOption): CommandOption {
	const selectOption =
		excelTable.value && !EXCEL_TABLE_TYPES.includes(excelTable.value)
			? [...EXCEL_TABLE_TYPES, excelTable.value]
			: EXCEL_TABLE_TYPES;
	return {
		...excelTable,
		attribute: { ...excelTable.attribute, selectOption },
	};
}

export default function ResultFormSection({
	resultOption,
	handleTypeSelect,
}: {
	resultOption: ResultOption;
	handleTypeSelect: (selected: string) => Promise<void>;
}) {
	const prefix = resultOption.prefix;
	const excelTable = resultOption.excelTable;
	const outputEncoding = resultOption.outputEncoding;
	const format = resultOption.format;
	const outputExtension = resultOption.outputExtension;
	const fixedColumnDef = resultOption.fixedColumnDef;
	const fixedLengthType = resultOption.fixedLengthType;
	const parallel = resultOption.parallel;
	// SHEETのときはparallel、BOOKのときはresultPathを隠すため、どちらかの判定が変わったら再構成する
	const handleExcelTableChange = async (value: string) => {
		if (
			(value === "SHEET") !== (excelTable?.value === "SHEET") ||
			(value === "BOOK") !== (excelTable?.value === "BOOK")
		) {
			await handleTypeSelect(value);
		}
	};
	return (
		<>
			<Select
				handleTypeSelect={handleTypeSelect}
				prefix={prefix}
				element={resultOption.resultType}
			/>
			{resultOption.result && (
				<FileText prefix={prefix} element={resultOption.result} />
			)}
			{resultOption.resultPath && (
				<PlainText prefix={prefix} element={resultOption.resultPath} />
			)}
			{resultOption.exportEmptyTable && (
				<Check prefix={prefix} element={resultOption.exportEmptyTable} />
			)}
			{resultOption.exportHeader && (
				<Check prefix={prefix} element={resultOption.exportHeader} />
			)}
			{excelTable && (
				<Select
					prefix={prefix}
					element={excelTableElement(excelTable)}
					handleTypeSelect={handleExcelTableChange}
				/>
			)}
			{outputEncoding && (
				<EncodingText prefix={prefix} element={outputEncoding} />
			)}
			{format && <PlainText prefix={prefix} element={format} />}
			{outputExtension && (
				<PlainText prefix={prefix} element={outputExtension} />
			)}
			{fixedColumnDef && (
				<FixedColumnDefText prefix={prefix} element={fixedColumnDef} />
			)}
			{fixedLengthType && (
				<PlainText prefix={prefix} element={fixedLengthType} />
			)}
			{parallel && <PlainText prefix={prefix} element={parallel} />}
			{resultOption.op && (
				<Select
					prefix={prefix}
					element={resultOption.op}
					handleTypeSelect={(_: string) => {
						return new Promise((_) => {});
					}}
				/>
			)}
			{resultOption.jdbc && <JdbcFormSection jdbcOption={resultOption.jdbc} />}
		</>
	);
}
