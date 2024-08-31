import type { GenerateParams } from "../../model/CommandParam";
import { DatasetLoadForm } from "./DatasetLoadForm";
import FormElements from "./FormElement";

export function GenerateForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	generate: GenerateParams;
}) {
	const srcData = prop.generate.srcData;
	return (
		<>
			<FormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix=""
				elements={prop.generate.elements}
			/>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={srcData}
			/>
			{prop.generate.templateOption && (
				<FormElements
					handleTypeSelect={prop.handleTypeSelect}
					name={prop.name}
					prefix={prop.generate.templateOption.prefix}
					elements={prop.generate.templateOption.elements}
				/>
			)}
		</>
	);
}
