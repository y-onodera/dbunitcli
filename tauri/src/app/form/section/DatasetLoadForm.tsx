import { useMemo } from "react";
import { DatasetSrcInfoProvider } from "../../../context/DatasetSrcInfoProvider";
import { JdbcConnectionProvider } from "../../../context/JdbcConnectionProvider";
import type { DatasetSource } from "../../../model/CommandParam";
import { buildDatasetSrcInfo } from "./CommandFormElement";
import DatasetCommandFormSection from "./DatasetCommandFormSection";
import DatasetSettingSection from "./DatasetSettingSection";
import JdbcFormSection from "./JdbcFormSection";
import SrcFormSection from "./SrcFormSection";

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
	const jdbcOption = prop.srcData.jdbcOption();
	return (
		<JdbcConnectionProvider>
			<DatasetSrcInfoProvider
				key={prop.name + prop.srcData.prefix}
				initialValue={initialDatasetSrcInfo}
			>
				<fieldset className="border border-gray-200 p-3">
					<legend>{prop.srcData.prefix}</legend>
					<SrcFormSection
						prefix={src.prefix}
						name={prop.name}
						elements={src.elements}
						handleTypeSelect={prop.handleTypeSelect}
					/>
					{jdbcOption.elements.length > 0 && (
						<JdbcFormSection jdbcOption={jdbcOption} />
					)}
					<DatasetCommandFormSection
						commandParams={srcTypeSettings}
						handleTypeSelect={prop.handleTypeSelect}
						name={prop.name}
					/>
					<DatasetSettingSection
						prefix={settingElements.prefix}
						name={prop.name}
						elements={settingElements.elements}
					/>
				</fieldset>
			</DatasetSrcInfoProvider>
		</JdbcConnectionProvider>
	);
}
