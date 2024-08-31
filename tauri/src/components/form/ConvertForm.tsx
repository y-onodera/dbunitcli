import type { ConvertParams } from "../../model/CommandParam";
import { DatasetLoadForm } from "./DatasetLoadForm";
import FormElements from "./FormElement";

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
			<FormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix={convertResult.prefix}
				elements={convertResult.elements}
			/>
			<FormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix={convertResult.prefix}
				elements={convertResult.jdbc ? convertResult.jdbc.elements : []}
			/>
		</>
	);
}
