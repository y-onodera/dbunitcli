import type { GenerateParams } from "../../model/CommandParam";
import CommandFormElements from "./CommandFormElement";
import { DatasetLoadForm } from "./DatasetLoadForm";

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
					<CommandFormElements
						handleTypeSelect={prop.handleTypeSelect}
						name={prop.name}
						prefix={prop.generate.templateOption.prefix}
						elements={prop.generate.templateOption.elements}
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
