import type { GenerateParams } from "../../model/SelectParameter";
import CommandFormElements from "./section/CommandFormElement";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import TemplateFormSection from "./section/TemplateFormSection";

export function GenerateForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	generate: GenerateParams;
}) {
	const srcData = prop.generate.srcData;
	return (
		<>
			<fieldset className="border border-gray-200 p-3">
				<legend>generate</legend>
				<CommandFormElements
					handleTypeSelect={prop.handleTypeSelect}
					name={prop.name}
					prefix=""
					elements={prop.generate.elements}
				/>
			</fieldset>
			{prop.generate.templateOption && (
				<fieldset className="border border-gray-200 p-3">
					<legend>{prop.generate.templateOption.prefix}</legend>
					<TemplateFormSection
						commandParams={prop.generate.templateOption}
						name={prop.name}
					/>
				</fieldset>
			)}
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={srcData}
			/>
		</>
	);
}
