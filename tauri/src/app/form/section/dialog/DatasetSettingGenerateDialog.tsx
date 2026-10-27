import {
	DatasetSettings,
	type DatasetSettingsBuilder,
} from "../../../../model/DatasetSettings";
import { DatasetSettingsEditDialog } from "./DatasetSettingsDialog";
import {
	type GenerateButtonProps,
	ModelGenerateButton,
	type ModelGenerateConfig,
} from "./GenerateDialog";

const datasetSettingGenerateConfig: ModelGenerateConfig<
	DatasetSettingsBuilder,
	DatasetSettings
> = {
	generateType: "settings",
	formId: "datasetSettingGenerateForm",
	name: "datasetSettingGenerate",
	command: "dialogs/dataset-setting-generate",
	label: "Dataset Setting Generate",
	title: "Generate Dataset Setting",
	endpoint: "dataset-setting/generate",
	build: DatasetSettings.build,
	// 生成後は既存の編集ダイアログで内容確認・命名保存する
	renderEditDialog: (settings, props) => (
		<DatasetSettingsEditDialog settings={settings} {...props} />
	),
};

export function DatasetSettingGenerateButton({ setPath }: GenerateButtonProps) {
	return (
		<ModelGenerateButton
			config={datasetSettingGenerateConfig}
			setPath={setPath}
		/>
	);
}
