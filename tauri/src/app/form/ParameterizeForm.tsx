import type { ParameterizeParams } from "../../model/CommandParam";
import CommandFormElements from "./CommandFormElement";
import { DatasetLoadForm } from "./DatasetLoadForm";

export function ParameterizeForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	parameterize: ParameterizeParams;
}) {
	const paramData = prop.parameterize.paramData;
	const templateOption = prop.parameterize.templateOption;
	return (
		<>
			<fieldset className="border border-gray-200 p-3">
				<legend>execute</legend>
				<CommandFormElements
					handleTypeSelect={prop.handleTypeSelect}
					name={prop.name}
					prefix=""
					elements={prop.parameterize.elements}
				/>
				<CommandFormElements
					handleTypeSelect={prop.handleTypeSelect}
					name={prop.name}
					prefix={templateOption.prefix}
					elements={templateOption.elements}
				/>
			</fieldset>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={paramData}
			/>
		</>
	);
}
