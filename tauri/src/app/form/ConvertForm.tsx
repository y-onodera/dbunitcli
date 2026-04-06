import type { ConvertOptions } from "../../model/SelectParameter";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import ResultFormSection from "./section/ResultFormSection";

export function ConvertForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	convert: ConvertOptions;
}) {
	const srcData = prop.convert.srcData;
	const convertResult = prop.convert.convertResult;
	return (
		<>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={srcData}
			/>
			<fieldset className="border border-gray-200 p-3">
				<legend>{convertResult.prefix}</legend>
				<ResultFormSection
					resultOption={convertResult}
					handleTypeSelect={prop.handleTypeSelect}
				/>
			</fieldset>
		</>
	);
}
