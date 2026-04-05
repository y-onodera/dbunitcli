import type { ConvertResult } from "../../../model/CommandParam";
import Check from "./element/Check";
import FileText from "./element/FileText";
import PlainText from "./element/PlainText";
import Select from "./element/Select";
import JdbcFormSection from "./JdbcFormSection";

export default function ConvertResultFormSection({
	convertResult,
	handleTypeSelect,
}: {
	convertResult: ConvertResult;
	handleTypeSelect: (selected: string) => Promise<void>;
}) {
	const prefix = convertResult.prefix;
	const excelTable = convertResult.excelTable;
	const outputEncoding = convertResult.outputEncoding;
	return (
		<>
			<Select
				handleTypeSelect={handleTypeSelect}
				prefix={prefix}
				element={convertResult.resultType}
			/>
			{convertResult.result && (
				<FileText prefix={prefix} element={convertResult.result} />
			)}
			{convertResult.resultPath && (
				<PlainText prefix={prefix} element={convertResult.resultPath} />
			)}
			{convertResult.exportEmptyTable && (
				<Check prefix={prefix} element={convertResult.exportEmptyTable} />
			)}
			{convertResult.exportHeader && (
				<Check prefix={prefix} element={convertResult.exportHeader} />
			)}
			{excelTable && <PlainText prefix={prefix} element={excelTable} />}
			{outputEncoding && <PlainText prefix={prefix} element={outputEncoding} />}
			{convertResult.op && (
				<Select
					prefix={prefix}
					element={convertResult.op}
					handleTypeSelect={(_: string) => {
						return new Promise((_) => {});
					}}
				/>
			)}
			{convertResult.jdbc && (
				<JdbcFormSection jdbcOption={convertResult.jdbc} />
			)}
		</>
	);
}
