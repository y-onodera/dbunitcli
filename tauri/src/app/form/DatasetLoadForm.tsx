import { useMemo } from "react";
import { DatasetSrcInfoProvider } from "../../context/DatasetSrcInfoProvider";
import { JdbcConnectionProvider } from "../../context/JdbcConnectionProvider";
import type { DatasetSource } from "../../model/CommandParam";
import CommandFormElements, { buildDatasetSrcInfo } from "./CommandFormElement";
import JdbcFormSection, { isJdbcField } from "./JdbcFormSection";

export function DatasetLoadForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	srcData: DatasetSource;
}) {
	const src = prop.srcData.srcElements();
	const srcTypeSettings = prop.srcData.srcTypeSettings();
	const settingElements = prop.srcData.settingElements();
	const initialDatasetSrcInfo = useMemo(
		() => buildDatasetSrcInfo(prop.srcData.elements),
		[prop.srcData.elements],
	);
	const srcJdbc = src.elements.filter((e) => isJdbcField(e.name));
	const srcTypeSettingsJdbc = srcTypeSettings.elements.filter((e) =>
		isJdbcField(e.name),
	);
	const settingElementsJdbc = settingElements.elements.filter((e) =>
		isJdbcField(e.name),
	);
	return (
		<JdbcConnectionProvider>
			<DatasetSrcInfoProvider
				key={prop.name + prop.srcData.prefix}
				initialValue={initialDatasetSrcInfo}
			>
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
					{srcJdbc.length > 0 && (
						<JdbcFormSection prefix={src.prefix} elements={srcJdbc} />
					)}
					<CommandFormElements
						handleTypeSelect={prop.handleTypeSelect}
						prefix={srcTypeSettings.prefix}
						name={prop.name}
						elements={srcTypeSettings.elements}
						optionCaption={srcTypeSettings.optionCaption}
						optional={srcTypeSettings.optional}
					/>
					{srcTypeSettingsJdbc.length > 0 && (
						<JdbcFormSection
							prefix={srcTypeSettings.prefix}
							elements={srcTypeSettingsJdbc}
						/>
					)}
					<CommandFormElements
						handleTypeSelect={prop.handleTypeSelect}
						prefix={settingElements.prefix}
						name={prop.name}
						elements={settingElements.elements}
						optionCaption={settingElements.optionCaption}
						optional={settingElements.optional}
					/>
					{settingElementsJdbc.length > 0 && (
						<JdbcFormSection
							prefix={settingElements.prefix}
							elements={settingElementsJdbc}
						/>
					)}
				</fieldset>
			</DatasetSrcInfoProvider>
		</JdbcConnectionProvider>
	);
}
