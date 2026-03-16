import { Suspense, use, useState } from "react";
import { SettingDialog, SettingTable } from "../../components/dialog";
import {
	useLoadDatasetSettings,
	useSaveDatasetSettings,
} from "../../hooks/useDatasetSettings";
import type { DatasetSrcInfo } from "../../model/CommandParam";
import type { DatasetSetting } from "../../model/DatasetSettings";
import {
	DatasetSettings,
	newDatasetSetting,
} from "../../model/DatasetSettings";
import { saveOnSuccess } from "../../utils/fetchUtils";
import DatasetSettingDialog from "./DatasetSettingDialog";

export default function DatasetSettingsDialog(props: {
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
	datasetSrcInfo?: DatasetSrcInfo;
}) {
	const loadSettings = useLoadDatasetSettings();
	return (
		<Suspense fallback={<div>Loading...</div>}>
			<Dialog
				promise={loadSettings(props.fileName)}
				fileName={props.fileName}
				handleDialogClose={props.handleDialogClose}
				handleSave={props.handleSave}
				datasetSrcInfo={props.datasetSrcInfo}
			/>
		</Suspense>
	);
}
function Dialog(props: {
	promise: Promise<DatasetSettings>;
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
	datasetSrcInfo?: DatasetSrcInfo;
}) {
	const saveSettings = useSaveDatasetSettings();
	const dataSettingsData = use(props.promise);
	const [dataSettings, setDataSettings] =
		useState<DatasetSettings>(dataSettingsData);
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
				SettingDialogComponent={({ setting, handleDialogClose, handleCommit }) => (
					<DatasetSettingDialog
						setting={setting}
						handleDialogClose={handleDialogClose}
						handleCommit={handleCommit}
						datasetSrcInfo={props.datasetSrcInfo}
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
				SettingDialogComponent={({ setting, handleDialogClose, handleCommit }) => (
					<DatasetSettingDialog
						setting={setting}
						handleDialogClose={handleDialogClose}
						handleCommit={handleCommit}
						datasetSrcInfo={props.datasetSrcInfo}
					/>
				)}
				newSetting={newDatasetSetting}
				getKey={(setting) => setting.displayName()}
			/>
		</SettingDialog>
	);
}
