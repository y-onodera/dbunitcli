import type { ConvertParams } from "../../model/SelectParameter";
import ConvertResultFormSection from "./section/ConvertResultFormSection";
import { DatasetLoadForm } from "./section/DatasetLoadForm";

export function ConvertForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	convert: ConvertParams;
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
				<ConvertResultFormSection
					convertResult={convertResult}
					handleTypeSelect={prop.handleTypeSelect}
				/>
			</fieldset>
		</>
	);
}
