import {
	DatasetSrcInfoProvider,
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import {
	buildDatasetSrcInfo,
	type CommandParam,
	type DatasetSource,
	type DatasetSrcInfo,
} from "../../../model/CommandParam";
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
		if (element.name in datasetSrcInfo) {
			setDatasetSrcInfo({
				...datasetSrcInfo,
				[element.name]: newValue,
			} as DatasetSrcInfo);
		}
	};
	const srcElements = prop.srcData.srcElements;
	const srcTypeSettings = prop.srcData.srcTypeSettings;
	const settingElements = prop.srcData.settingElements;
	const jdbcOption = prop.srcData.jdbcOption;
	const initialDatasetSrcInfo = buildDatasetSrcInfo(prop.srcData);
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
				{jdbcOption !== undefined && (
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
