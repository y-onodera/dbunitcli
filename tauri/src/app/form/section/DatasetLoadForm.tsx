import { useMemo } from "react";
import { DatasetSrcInfoProvider } from "../../../context/DatasetSrcInfoProvider";
import { JdbcConnectionProvider } from "../../../context/JdbcConnectionProvider";
import type { DatasetSource } from "../../../model/CommandParam";
import { buildDatasetSrcInfo } from "./CommandFormElement";
import DatasetCommandFormSection from "./DatasetCommandFormSection";
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
					<DatasetCommandFormSection
						commandParams={src}
						handleTypeSelect={prop.handleTypeSelect}
						name={prop.name}
					/>
					{srcTypeSettingsJdbc.elements.length > 0 && (
						<JdbcFormSection
							prefix={srcTypeSettings.prefix}
							elements={srcTypeSettingsJdbc.elements}
						/>
					)}
					<DatasetCommandFormSection
						commandParams={srcTypeSettings}
						handleTypeSelect={prop.handleTypeSelect}
						name={prop.name}
					/>
					<DatasetCommandFormSection
						commandParams={settingElements}
						handleTypeSelect={prop.handleTypeSelect}
						name={prop.name}
					/>
				</fieldset>
			</DatasetSrcInfoProvider>
		</JdbcConnectionProvider>
	);
}
