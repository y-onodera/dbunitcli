import { useMemo } from "react";
import {
	DatasetSrcInfoProvider,
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import type {
	CommandParam,
	DatasetSource,
	DatasetSrcInfo,
} from "../../../model/CommandParam";
import { buildDatasetSrcInfo } from "../../../model/CommandParam";
import DatasetCommandFormSection from "./DatasetCommandFormSection";
import DatasetSettingSection from "./DatasetSettingSection";
import JdbcFormSection from "./JdbcFormSection";
import SrcFormSection from "./SrcFormSection";

export function DatasetLoadForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	srcData: DatasetSource;
}) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();
	const handleToggleChecked = (element: CommandParam) => (checked: boolean) => {
		setDatasetSrcInfo({
			...datasetSrcInfo,
			[element.name]: checked,
		} as DatasetSrcInfo);
	};
	const handleValueChange = (element: CommandParam) => (newValue: string) => {
		if (datasetSrcInfo && element.name in datasetSrcInfo) {
			setDatasetSrcInfo({
				...datasetSrcInfo,
				[element.name]: newValue,
			} as DatasetSrcInfo);
		}
	};
	const srcElements = useMemo(
		() => prop.srcData.srcElements(),
		[prop.srcData.srcElements],
	);
	const srcTypeSettings = prop.srcData.srcTypeSettings();
	const settingElements = useMemo(
		() => prop.srcData.settingElements(),
		[prop.srcData.settingElements],
	);
	const initialDatasetSrcInfo = useMemo(
		() => buildDatasetSrcInfo(prop.srcData.elements),
		[prop.srcData.elements],
	);
	const jdbcOption = useMemo(
		() => prop.srcData.jdbcOption(),
		[prop.srcData.jdbcOption],
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
					handleValueChange={handleValueChange}
					handleToggleChecked={handleToggleChecked}
				/>
				{jdbcOption.elements.length > 0 && (
					<JdbcFormSection jdbcOption={jdbcOption} />
				)}
				<DatasetCommandFormSection
					commandParams={srcTypeSettings}
					handleValueChange={handleValueChange}
					handleToggleChecked={handleToggleChecked}
				/>
				<DatasetSettingSection
					settingElements={settingElements}
					handleValueChange={handleValueChange}
					handleToggleChecked={handleToggleChecked}
				/>
			</fieldset>
		</DatasetSrcInfoProvider>
	);
}
