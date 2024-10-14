import MetadataSettingsProvider from "../../context/MetadataSettingsProvider";
import type { DatasetSource } from "../../model/CommandParam";
import CommandFormElements from "./CommandFormElement";

export function DatasetLoadForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	srcData: DatasetSource;
}) {
	return (
		<MetadataSettingsProvider>
			<CommandFormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix={prop.srcData.prefix}
				elements={prop.srcData.elements}
			/>
			<CommandFormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix={prop.srcData.prefix}
				elements={prop.srcData.jdbc ? prop.srcData.jdbc.elements : []}
			/>
			<CommandFormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix={prop.srcData.prefix}
				elements={
					prop.srcData.templateRender
						? prop.srcData.templateRender.elements
						: []
				}
			/>
		</MetadataSettingsProvider>
	);
}
