import { SectionFieldset } from "../../../components/dialog";
import { SectionLegend } from "../../../components/dialog/SectionFieldset";
import {
	DatasetSrcInfoProvider,
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import type {
	CommandOption,
	DatasetSource,
	SrcType,
} from "../../../model/CommandOption";
import { buildDatasetSrcInfo, withValues } from "../../../model/CommandOption";
import DataLoadFormSection from "./DataLoadFormSection";
import Select from "./element/Select";
import SettingFormSection from "./SettingFormSection";
import SrcFormSection from "./SrcFormSection";
import SrcTypeFormSection from "./SrcTypeFormSection";
import TemplateFormSection from "./TemplateFormSection";
import TraversalFormSection from "./TraversalFormSection";

function DatasetLoadFormContent({
	srcData,
	handleTypeSelect,
	defaultType,
	helpCommand,
	helpLabel,
}: {
	srcData: DatasetSource;
	handleTypeSelect: () => Promise<void>;
	defaultType?: SrcType;
	helpCommand?: string;
	helpLabel?: string;
}) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();

	// src要素の値はすべてそのままsidecarへ渡すため、項目を選ばずに反映する
	const handleValueChange = (element: CommandOption) => (newValue: string) =>
		setDatasetSrcInfo(withValues(datasetSrcInfo, { [element.name]: newValue }));
	const handleToggleChecked = (element: CommandOption) => (checked: boolean) =>
		handleValueChange(element)(String(checked));
	const jdbcOption = srcData.jdbcProperties ? srcData : undefined;
	const templateOption = srcData.templateGroup ? srcData : undefined;
	return (
		<SectionFieldset>
			<SectionLegend
				title={srcData.prefix}
				command={helpCommand}
				label={helpLabel}
			/>
			{srcData.srcType && (
				<Select
					handleTypeSelect={handleTypeSelect}
					prefix={srcData.prefix}
					element={srcData.srcType}
					hidden={false}
				/>
			)}
			<SrcFormSection
				srcElements={srcData}
				srcType={defaultType ?? srcData.srcType?.value}
				jdbcOption={jdbcOption}
				handleValueChange={handleValueChange}
			/>
			<TraversalFormSection
				srcElements={srcData}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
			{templateOption && (
				<TemplateFormSection
					templateOption={templateOption}
					showEncoding={!srcData.encoding}
					handleValueChange={handleValueChange}
				/>
			)}
			<DataLoadFormSection
				options={srcData}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
			<SrcTypeFormSection
				options={srcData}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
			<SettingFormSection
				settingElements={srcData}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		</SectionFieldset>
	);
}

export function DatasetLoadForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	srcData: DatasetSource;
	defalutType?: SrcType;
	helpCommand?: string;
	helpLabel?: string;
}) {
	if (prop.srcData.srcType?.value === "none") {
		return (
			<SectionFieldset>
				<SectionLegend
					title={prop.srcData.prefix}
					command={prop.helpCommand}
					label={prop.helpLabel}
				/>
				<Select
					handleTypeSelect={prop.handleTypeSelect}
					prefix={prop.srcData.prefix}
					element={prop.srcData.srcType}
					hidden={false}
				/>
			</SectionFieldset>
		);
	}
	const initialDatasetSrcInfo = buildDatasetSrcInfo(prop.srcData);
	return (
		<DatasetSrcInfoProvider
			key={`${prop.name}:${prop.srcData.prefix}:${prop.srcData.srcType?.value ?? ""}`}
			initialValue={initialDatasetSrcInfo}
		>
			<DatasetLoadFormContent
				srcData={prop.srcData}
				handleTypeSelect={prop.handleTypeSelect}
				defaultType={prop.defalutType}
				helpCommand={prop.helpCommand}
				helpLabel={prop.helpLabel}
			/>
		</DatasetSrcInfoProvider>
	);
}
