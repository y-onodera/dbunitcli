import type { ParameterizeParams } from "../../model/SelectParameter";
import CommandFormElements from "./section/CommandFormElement";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import TemplateFormSection from "./section/TemplateFormSection";

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
				{templateOption && (
					<TemplateFormSection
						commandParams={templateOption}
						handleTypeSelect={prop.handleTypeSelect}
						name={prop.name}
					/>
				)}
			</fieldset>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={paramData}
			/>
		</>
	);
}
