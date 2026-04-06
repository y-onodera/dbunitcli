import type { ResultOption } from "../../../model/CommandOption";
import Check from "./element/Check";
import FileText from "./element/FileText";
import PlainText from "./element/PlainText";
import Select from "./element/Select";
import JdbcFormSection from "./JdbcFormSection";

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
			{excelTable && <PlainText prefix={prefix} element={excelTable} />}
			{outputEncoding && <PlainText prefix={prefix} element={outputEncoding} />}
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
