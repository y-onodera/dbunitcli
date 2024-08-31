import type { DatasetSource } from "../../model/CommandParam";
import FormElements from "./FormElement";

export function DatasetLoadForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	srcData: DatasetSource;
}) {
	return (
		<>
			<FormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix={prop.srcData.prefix}
				elements={prop.srcData.elements}
			/>
			<FormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix={prop.srcData.prefix}
				elements={prop.srcData.jdbc ? prop.srcData.jdbc.elements : []}
			/>
			<FormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix={prop.srcData.prefix}
				elements={
					prop.srcData.templateRender
						? prop.srcData.templateRender.elements
						: []
				}
			/>
		</>
	);
}
