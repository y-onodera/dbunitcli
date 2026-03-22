import { useMemo } from "react";
import { DatasetSrcInfoProvider } from "../../../context/DatasetSrcInfoProvider";
import { JdbcConnectionProvider } from "../../../context/JdbcConnectionProvider";
import type { DatasetSource } from "../../../model/CommandParam";
import { buildDatasetSrcInfo } from "./CommandFormElement";
import DatasetCommandFormElements from "./DatasetCommandFormElement";
import JdbcFormSection from "./JdbcFormSection";

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
	const srcTypeSettingsJdbc = prop.srcData.jdbcElements();
	return (
		<JdbcConnectionProvider>
			<DatasetSrcInfoProvider
				key={prop.name + prop.srcData.prefix}
				initialValue={initialDatasetSrcInfo}
			>
				<fieldset className="border border-gray-200 p-3">
					<legend>{prop.srcData.prefix}</legend>
					<DatasetCommandFormElements
						handleTypeSelect={prop.handleTypeSelect}
						prefix={src.prefix}
						name={prop.name}
						elements={src.elements}
						optionCaption={src.optionCaption}
						optional={src.optional}
					/>
					{srcTypeSettingsJdbc.elements.length > 0 && (
						<JdbcFormSection
							prefix={srcTypeSettings.prefix}
							elements={srcTypeSettingsJdbc.elements}
						/>
					)}
					<DatasetCommandFormElements
						handleTypeSelect={prop.handleTypeSelect}
						prefix={srcTypeSettings.prefix}
						name={prop.name}
						elements={srcTypeSettings.elements}
						optionCaption={srcTypeSettings.optionCaption}
						optional={srcTypeSettings.optional}
					/>
					<DatasetCommandFormElements
						handleTypeSelect={prop.handleTypeSelect}
						prefix={settingElements.prefix}
						name={prop.name}
						elements={settingElements.elements}
						optionCaption={settingElements.optionCaption}
						optional={settingElements.optional}
					/>
				</fieldset>
			</DatasetSrcInfoProvider>
		</JdbcConnectionProvider>
	);
}
