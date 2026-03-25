import type { ConvertResult } from "../../../model/CommandParam";
import Check from "./Check";
import Select from "./Select";
import Text from "./TextFormElement";

export default function ConvertResultFormSection({
	convertResult,
	handleTypeSelect,
}: {
	convertResult: ConvertResult;
	handleTypeSelect: (selected: string) => Promise<void>;
}) {
	const prefix = convertResult.prefix;
	const excelTable = convertResult.elements.find((e) => e.name === "excelTable");
	const outputEncoding = convertResult.elements.find(
		(e) => e.name === "outputEncoding",
	);
	return (
		<>
			<Select
				handleTypeSelect={handleTypeSelect}
				prefix={prefix}
				element={convertResult.resultType}
			/>
			<Text prefix={prefix} element={convertResult.result} />
			<Text prefix={prefix} element={convertResult.resultPath} />
			<Check prefix={prefix} element={convertResult.exportEmptyTable} />
			<Check prefix={prefix} element={convertResult.exportHeader} />
			{excelTable && <Text prefix={prefix} element={excelTable} />}
			{outputEncoding && <Text prefix={prefix} element={outputEncoding} />}
			{convertResult.jdbc && (
				<>
					<Text prefix={prefix} element={convertResult.jdbc.jdbcProperties} />
					<Text prefix={prefix} element={convertResult.jdbc.jdbcUrl} />
					<Text prefix={prefix} element={convertResult.jdbc.jdbcUser} />
					<Text prefix={prefix} element={convertResult.jdbc.jdbcPass} />
				</>
			)}
		</>
	);
}
