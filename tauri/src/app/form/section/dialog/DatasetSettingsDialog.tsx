import { useState } from "react";
import {
	SettingDialog,
	SettingTable,
	StatusMessage,
} from "../../../../components/dialog";
import {
	useDatasetSettingsData,
	useDeleteDatasetSettings,
	useSaveDatasetSettings,
} from "../../../../hooks/useDatasetSettings";
import type { DatasetSetting } from "../../../../model/DatasetSettings";
import {
	DatasetSettings,
	newDatasetSetting,
} from "../../../../model/DatasetSettings";
import { saveOnSuccess } from "../../../../utils/fetchUtils";
import DatasetSettingDialog from "./DatasetSettingDialog";
import { createEditButton, createRemoveButton } from "./ResourceEditButton";

export default function DatasetSettingsDialog(props: {
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const { settings, loading } = useDatasetSettingsData(props.fileName);
	if (loading) {
		return <StatusMessage kind="loading">Loading...</StatusMessage>;
	}
	return (
		<DatasetSettingsEditDialog
			settings={settings}
			fileName={props.fileName}
			handleDialogClose={props.handleDialogClose}
			handleSave={props.handleSave}
		/>
	);
}
export function DatasetSettingsEditDialog(props: {
	settings: DatasetSettings;
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const saveSettings = useSaveDatasetSettings();
	const [dataSettings, setDataSettings] = useState<DatasetSettings>(
		props.settings,
	);
	return (
		<SettingDialog
			title="Dataset Settings"
			helpCommand="dialogs/dataset-settings"
			helpLabel="Dataset Settings"
			handleDialogClose={props.handleDialogClose}
			fileName={props.fileName}
			handleSave={(fileName) =>
				saveOnSuccess(
					() => saveSettings(fileName, dataSettings),
					() => props.handleSave(fileName),
				)
			}
		>
			<SettingTable<DatasetSetting>
				caption="Add Metadata Settings"
				settings={dataSettings.settings}
				setSettings={(convertSettings) =>
					setDataSettings((cur) => {
						const updatedSettings = convertSettings(cur.settings);
						return new DatasetSettings(updatedSettings, cur.commonSettings);
					})
				}
				SettingDialogComponent={DatasetSettingDialog}
				newSetting={newDatasetSetting}
			/>
			<SettingTable<DatasetSetting>
				caption="Common Settings"
				settings={dataSettings.commonSettings}
				setSettings={(convertCommon) =>
					setDataSettings((cur) => {
						const updatedCommonSettings = convertCommon(cur.commonSettings);
						return new DatasetSettings(cur.settings, updatedCommonSettings);
					})
				}
				SettingDialogComponent={DatasetSettingDialog}
				newSetting={newDatasetSetting}
			/>
		</SettingDialog>
	);
}
export const DatasetSettingEditButton = createEditButton(DatasetSettingsDialog);
export const RemoveDatasetSettingButton = createRemoveButton(
	useDeleteDatasetSettings,
);
