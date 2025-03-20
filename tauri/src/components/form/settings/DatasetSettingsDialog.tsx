import { saveDatasetSettings, useDatasetSettings, useSetDatasetSettings } from "../../../context/DatasetSettingsProvider";
import { useEnviroment } from "../../../context/EnviromentProvider";
import type { DatasetSetting } from "../../../model/DatasetSettings";
import { DatasetSettings, newDatasetSetting } from "../../../model/DatasetSettings";
import SettingDialog from "./DatasetSettingDialog";
import ResourceFileDialog from "./ResourceFileDialog";
import SettingTable from "./SettingTable";

export default function DatasetSettingsDialog(props: {
	fileName: string;
	setFileName: (fileName: string) => void;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const environment = useEnviroment();
	const dataSettings = useDatasetSettings();
	const setDataSettings = useSetDatasetSettings();

	return (
		<ResourceFileDialog
			handleDialogClose={props.handleDialogClose}
			fileName={props.fileName}
			setFileName={props.setFileName}
			handleSave={(fileName) => {
				saveDatasetSettings(environment.apiUrl, fileName, dataSettings);
				props.handleSave(fileName);
			}}
		>
			<SettingTable<DatasetSetting>
				caption="Add Metadata Settings"
				settings={dataSettings.settings}
				setSettings={convertSettings => setDataSettings((cur) => {
					const updatedSettings = convertSettings(cur.settings);
					return new DatasetSettings(updatedSettings, cur.commonSettings);
				})}
				addSettings={(current, settings) => [...current, settings]}
				updateSettings={(current, before, after) => current.map((setting) => (setting === before ? after : setting))}
				deleteSettings={(current, settings) => current.filter((setting) => setting !== settings)}
				renderSetting={(setting) => setting.displayName()}
				SettingDialogComponent={SettingDialog}
				newSetting={newDatasetSetting}
				getKey={(setting) => setting.displayName()}
			/>
			<SettingTable<DatasetSetting>
				caption="Common Settings"
				settings={dataSettings.commonSettings}
				setSettings={(convertCommon) => setDataSettings((cur) => {
					const updatedCommonSettings = convertCommon(cur.commonSettings);
					return new DatasetSettings(cur.settings, updatedCommonSettings);
				})}
				addSettings={(current, settings) => [...current, settings]}
				updateSettings={(current, before, after) => current.map((setting) => (setting === before ? after : setting))}
				deleteSettings={(current, settings) => current.filter((setting) => setting !== settings)}
				renderSetting={(setting) => setting.displayName()}
				SettingDialogComponent={SettingDialog}
				newSetting={newDatasetSetting}
				getKey={(setting) => setting.displayName()}
			/>
		</ResourceFileDialog>
	);
}