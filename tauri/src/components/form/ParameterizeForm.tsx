import type { ParameterizeParams } from "../../model/CommandParam";
import { DatasetLoadForm } from "./DatasetLoadForm";
import FormElements from "./FormElement";

export function ParameterizeForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	parameterize: ParameterizeParams;
}) {
	const paramData = prop.parameterize.paramData;
	const templateOption = prop.parameterize.templateOption;
	return (
		<>
			<FormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix=""
				elements={prop.parameterize.elements}
			/>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={paramData}
			/>
			<FormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix={templateOption.prefix}
				elements={templateOption.elements}
			/>
		</>
	);
}
