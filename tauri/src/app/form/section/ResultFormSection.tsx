import type { ResultOption } from "../../../model/CommandOption";
import Check from "./element/Check";
import EncodingText from "./element/EncodingText";
import FileText from "./element/FileText";
import FixedColumnDefText from "./element/FixedColumnDefText";
import PlainText from "./element/PlainText";
import Select from "./element/Select";
import Text from "./element/Text";
import JdbcFormSection from "./JdbcFormSection";

const EXCEL_TABLE_TYPES = ["SHEET", "BOOK"];

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
	// 環境変数で値を渡せるよう自由入力のまま。SHEETのときだけparallelを隠すため、SHEETかどうかが変わったら再構成する
	const handleExcelTableChange = (value: string) => {
		if ((value === "SHEET") !== (excelTable?.value === "SHEET")) {
			void handleTypeSelect(value);
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
				<Text
					prefix={prefix}
					element={excelTable}
					resourceFiles={EXCEL_TABLE_TYPES}
					handleValueChange={handleExcelTableChange}
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
