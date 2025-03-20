import DatasetSettingsProvider from "../../context/DatasetSettingsProvider";
import XlsxSchemaProvider from "../../context/XlsxSchemaProvider";
import type { DatasetSource } from "../../model/CommandParam";
import CommandFormElements from "./CommandFormElement";

export function DatasetLoadForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	srcData: DatasetSource;
}) {
	const src = prop.srcData.srcElements();
	const srcTypeSettings = prop.srcData.srcTypeSettings();
	const settingElements = prop.srcData.settingElements();
	return (
		<DatasetSettingsProvider>
			<XlsxSchemaProvider>
				<fieldset className="border border-gray-200 p-3">
					<legend>{prop.srcData.prefix}</legend>
					<CommandFormElements
						handleTypeSelect={prop.handleTypeSelect}
						prefix={src.prefix}
						name={prop.name}
						elements={src.elements}
						optionCaption={src.optionCaption}
						optional={src.optional}
					/>
					<CommandFormElements
						handleTypeSelect={prop.handleTypeSelect}
						prefix={srcTypeSettings.prefix}
						name={prop.name}
						elements={srcTypeSettings.elements}
						optionCaption={srcTypeSettings.optionCaption}
						optional={srcTypeSettings.optional}
					/>
					<CommandFormElements
						handleTypeSelect={prop.handleTypeSelect}
						prefix={settingElements.prefix}
						name={prop.name}
						elements={settingElements.elements}
						optionCaption={settingElements.optionCaption}
						optional={settingElements.optional}
					/>
				</fieldset>
			</XlsxSchemaProvider>
		</DatasetSettingsProvider>
	);
}
