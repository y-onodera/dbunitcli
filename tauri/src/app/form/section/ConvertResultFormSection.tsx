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
			<Text prefix={prefix} element={convertResult.outputEncoding} />
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
