import type { ComponentType } from "react";
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

export type DatasetSettingsEditDialogComponent = ComponentType<{
	settings: DatasetSettings;
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (fileName: string) => void;
}>;

// 生成後は渡された編集ダイアログで内容確認・命名保存する
function datasetSettingGenerateConfig(
	EditDialog: DatasetSettingsEditDialogComponent,
): ModelGenerateConfig<DatasetSettingsBuilder, DatasetSettings> {
	return {
		generateType: "settings",
		formId: "datasetSettingGenerateForm",
		name: "datasetSettingGenerate",
		command: "dialogs/dataset-setting-generate",
		label: "Dataset Setting Generate",
		title: "Generate Dataset Setting",
		endpoint: "dataset-setting/generate",
		build: DatasetSettings.build,
		renderEditDialog: (settings, props) => (
			<EditDialog settings={settings} {...props} />
		),
	};
}

// editDialogを省略するとDataset Settingsで確認する（compareの比較キーはCompare Key Settingsを渡す）
export function DatasetSettingGenerateButton({
	setPath,
	editDialog = DatasetSettingsEditDialog,
}: GenerateButtonProps & { editDialog?: DatasetSettingsEditDialogComponent }) {
	return (
		<ModelGenerateButton
			config={datasetSettingGenerateConfig(editDialog)}
			setPath={setPath}
		/>
	);
}
