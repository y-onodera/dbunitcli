import { useState } from "react";
import { SettingDialog, SettingTable } from "../../../../components/dialog";
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
import ResourceEditButton, {
	RemoveResource,
	type ResourceEditButtonProp,
} from "./ResourceEditButton";

export default function DatasetSettingsDialog(props: {
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const { settings, loading } = useDatasetSettingsData(props.fileName);
	if (loading) {
		return <div>Loading...</div>;
	}
	return (
		<Dialog
			settings={settings}
			fileName={props.fileName}
			handleDialogClose={props.handleDialogClose}
			handleSave={props.handleSave}
		/>
	);
}
function Dialog(props: {
	settings: DatasetSettings;
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const saveSettings = useSaveDatasetSettings();
	const [dataSettings, setDataSettings] =
		useState<DatasetSettings>(props.settings);
	return (
		<SettingDialog
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
				renderSetting={(setting) => setting.displayName()}
				SettingDialogComponent={({
					setting,
					handleDialogClose,
					handleCommit,
				}) => (
					<DatasetSettingDialog
						setting={setting}
						handleDialogClose={handleDialogClose}
						handleCommit={handleCommit}
					/>
				)}
				newSetting={newDatasetSetting}
				getKey={(setting) => setting.displayName()}
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
				renderSetting={(setting) => setting.displayName()}
				SettingDialogComponent={({
					setting,
					handleDialogClose,
					handleCommit,
				}) => (
					<DatasetSettingDialog
						setting={setting}
						handleDialogClose={handleDialogClose}
						handleCommit={handleCommit}
					/>
				)}
				newSetting={newDatasetSetting}
				getKey={(setting) => setting.displayName()}
			/>
		</SettingDialog>
	);
}
export function DatasetSettingEditButton({
	path,
	setPath,
}: ResourceEditButtonProp) {
	const renderDialog = (open: boolean, closeDialog: () => void) => {
		if (!open) {
			return null;
		}
		return (
			<DatasetSettingsDialog
				fileName={path}
				handleDialogClose={closeDialog}
				handleSave={(newPath: string) => {
					setPath(newPath);
					closeDialog();
				}}
			/>
		);
	};

	return <ResourceEditButton renderDialog={renderDialog} />;
}
export function RemoveDatasetSettingButton({
	path,
	setPath,
}: ResourceEditButtonProp) {
	const deleteSettings = useDeleteDatasetSettings();
	return (
		<RemoveResource
			path={path}
			setPath={setPath}
			deleteResource={deleteSettings}
		/>
	);
}
