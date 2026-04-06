import {
	DatasetSrcInfoProvider,
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import type {
	CommandOption,
	DatasetSource,
	DatasetSrcInfo,
	SrcType,
} from "../../../model/CommandParam";
import { buildDatasetSrcInfo } from "../../../model/CommandParam";
import DatasetSettingSection from "./DatasetSettingSection";
import Select from "./element/Select";
import JdbcFormSection from "./JdbcFormSection";
import SrcFormSection from "./SrcFormSection";
import SrcTypeFormSection from "./SrcTypeFormSection";

export function DatasetLoadForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	srcData: DatasetSource;
	defalutType?: SrcType;
}) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();
	if (prop.srcData.srcType?.value === "none") {
		return (
			<fieldset className="border border-gray-200 p-3">
				<legend>{prop.srcData.prefix}</legend>
				<Select
					handleTypeSelect={prop.handleTypeSelect}
					prefix={prop.srcData.prefix}
					element={prop.srcData.srcType}
					hidden={false}
				/>
			</fieldset>
		);
	}
	const handleToggleChecked =
		(element: CommandOption) => (checked: boolean) => {
			setDatasetSrcInfo({
				...datasetSrcInfo,
				[element.name]: checked,
			} as DatasetSrcInfo);
		};
	const handleValueChange = (element: CommandOption) => (newValue: string) => {
		if (element.name in datasetSrcInfo) {
			setDatasetSrcInfo({
				...datasetSrcInfo,
				[element.name]: newValue,
			} as DatasetSrcInfo);
		}
	};
	const srcElements = prop.srcData;
	const srcTypeOptions = prop.srcData;
	const settingElements = prop.srcData;
	const jdbcOption = prop.srcData;
	const initialDatasetSrcInfo = buildDatasetSrcInfo(prop.srcData);
	return (
		<DatasetSrcInfoProvider
			key={prop.name + prop.srcData.prefix}
			initialValue={initialDatasetSrcInfo}
		>
			<fieldset className="border border-gray-200 p-3">
				<legend>{prop.srcData.prefix}</legend>
				{prop.srcData.srcType && (
					<Select
						handleTypeSelect={prop.handleTypeSelect}
						prefix={prop.srcData.prefix}
						element={prop.srcData.srcType}
						hidden={false}
					/>
				)}
				<SrcFormSection
					srcElements={srcElements}
					srcType={prop.defalutType || prop.srcData.srcType?.value}
					handleValueChange={handleValueChange}
					handleToggleChecked={handleToggleChecked}
				/>
				{jdbcOption.jdbcProperties && (
					<JdbcFormSection jdbcOption={jdbcOption} />
				)}
				<SrcTypeFormSection
					options={srcTypeOptions}
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
