import { useMemo } from "react";
import { DatasetSrcInfoProvider } from "../../../context/DatasetSrcInfoProvider";
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
	const srcElements = useMemo(
		() => prop.srcData.srcElements(),
		[prop.srcData.elements],
	);
	const srcTypeSettings = prop.srcData.srcTypeSettings();
	const settingElements = useMemo(
		() => prop.srcData.settingElements(),
		[prop.srcData.elements],
	);
	const initialDatasetSrcInfo = useMemo(
		() => buildDatasetSrcInfo(prop.srcData.elements),
		[prop.srcData.elements],
	);
	const jdbcOption = useMemo(
		() => prop.srcData.jdbcOption(),
		[prop.srcData.elements],
	);
	return (
		<DatasetSrcInfoProvider
			key={prop.name + prop.srcData.prefix}
			initialValue={initialDatasetSrcInfo}
		>
			<fieldset className="border border-gray-200 p-3">
				<legend>{prop.srcData.prefix}</legend>
				<SrcFormSection
					srcElements={srcElements}
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
				<DatasetSettingSection settingElements={settingElements} />
			</fieldset>
		</DatasetSrcInfoProvider>
	);
}
