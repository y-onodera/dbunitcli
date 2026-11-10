import type { ReactNode } from "react";
import { SectionFieldset } from "../../../components/dialog";
import { SectionLegend } from "../../../components/dialog/SectionFieldset";
import {
	DatasetSrcInfoProvider,
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import { ResourceEncodingProvider } from "../../../context/ResourceEncodingProvider";
import type {
	CommandOption,
	DatasetSource,
	SrcType,
} from "../../../model/CommandOption";
import {
	buildDatasetSrcInfo,
	hasJdbcOption,
	hasTemplateOption,
	withValues,
} from "../../../model/CommandOption";
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
		setDatasetSrcInfo((current) =>
			withValues(current, { [element.name]: newValue }),
		);
	const handleToggleChecked = (element: CommandOption) => (checked: boolean) =>
		handleValueChange(element)(String(checked));
	const jdbcOption = hasJdbcOption(srcData) ? srcData : undefined;
	const templateOption = hasTemplateOption(srcData) ? srcData : undefined;
	const encoding = datasetSrcInfo.values.encoding ?? "";
	const settingEncoding = datasetSrcInfo.values.settingEncoding ?? "";
	return (
		<ResourceEncodingProvider
			encodings={{
				datasetSetting: settingEncoding,
				xlsxSchema: settingEncoding,
				sql: encoding,
				table: encoding,
				csvq: encoding,
				template: encoding,
			}}
		>
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
					queryOnly
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
		</ResourceEncodingProvider>
	);
}

// srcDataの入力値を初期値から作り直す単位
export const datasetSrcInfoResetKey = (name: string, srcData: DatasetSource) =>
	`${name}:${srcData.prefix}:${srcData.srcType?.value ?? ""}`;

// srcDataの入力値を共有する範囲。srcTypeが変わったら初期値から作り直す
export function DatasetSrcInfoScope({
	name,
	srcData,
	children,
}: {
	name: string;
	srcData: DatasetSource;
	children: ReactNode;
}) {
	return (
		<DatasetSrcInfoProvider
			key={datasetSrcInfoResetKey(name, srcData)}
			initialValue={buildDatasetSrcInfo(srcData)}
		>
			{children}
		</DatasetSrcInfoProvider>
	);
}

export function DatasetLoadForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	srcData: DatasetSource;
	defaultType?: SrcType;
	helpCommand?: string;
	helpLabel?: string;
	// trueなら自前のDatasetSrcInfoScopeを作らず、親のものへ入力値を反映する（同じsrcの入力値をフォーム内の別の欄でも使うとき）
	parentSrcInfo?: boolean;
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
	const content = (
		<DatasetLoadFormContent
			srcData={prop.srcData}
			handleTypeSelect={prop.handleTypeSelect}
			defaultType={prop.defaultType}
			helpCommand={prop.helpCommand}
			helpLabel={prop.helpLabel}
		/>
	);
	if (prop.parentSrcInfo) {
		return content;
	}
	return (
		<DatasetSrcInfoScope name={prop.name} srcData={prop.srcData}>
			{content}
		</DatasetSrcInfoScope>
	);
}
