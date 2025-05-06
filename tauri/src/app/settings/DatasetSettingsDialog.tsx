import { useEffect, useState } from 'react';
import ResourceFileDialog from '../../components/dialog/ResourceFileDialog';
import SettingTable from '../../components/dialog/SettingTable';
import { useLoadDatasetSettings, useSaveDatasetSettings } from '../../context/DatasetSettingsProvider';
import type { DatasetSetting } from "../../model/DatasetSettings";
import { DatasetSettings, newDatasetSetting } from "../../model/DatasetSettings";
import DatasetSettingDaialog from "./DatasetSettingDialog";

export default function DatasetSettingsDialog(props: {
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const loadSettings = useLoadDatasetSettings();
	const saveSettings = useSaveDatasetSettings();
	const [dataSettings, setDataSettings] = useState<DatasetSettings>(DatasetSettings.create());
	// biome-ignore lint/correctness/useExhaustiveDependencies: <explanation>
	useEffect(() => {
		loadSettings(props.fileName)
			.then((res) => {
				setDataSettings(res);
			})
			.catch((ex) => {
				alert(ex);
			});
	}, [props.fileName]);

	return (
		<ResourceFileDialog
			handleDialogClose={props.handleDialogClose}
			fileName={props.fileName}
			handleSave={async (fileName) => {
				const result = await saveSettings(fileName, dataSettings);
				if (result === 'success') {
					props.handleSave(fileName);
				}
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
				SettingDialogComponent={DatasetSettingDaialog}
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
				SettingDialogComponent={DatasetSettingDaialog}
				newSetting={newDatasetSetting}
				getKey={(setting) => setting.displayName()}
			/>
		</ResourceFileDialog>
	);
}