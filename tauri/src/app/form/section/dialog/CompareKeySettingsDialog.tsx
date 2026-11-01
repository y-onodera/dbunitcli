import { useState } from "react";
import {
	SettingDialog,
	SettingTable,
	StatusMessage,
} from "../../../../components/dialog";
import {
	useDatasetSettingsData,
	useSaveDatasetSettings,
} from "../../../../hooks/useDatasetSettings";
import {
	type DatasetSetting,
	type DatasetSettings,
	newDatasetSetting,
} from "../../../../model/DatasetSettings";
import ColumnDatalistDialog from "./ColumnDatalistDialog";
import { FilterOrderFields, TargetFields } from "./DatasetSettingFields";
import { useColumnCandidates } from "./useColumnCandidates";

const keysOnly = ["keys"] as const;

// 表の行は対象テーブルと比較キーを並べて表示する
function renderKeySetting(setting: DatasetSetting): string {
	const keys = setting.keys.filter((key) => key);
	return keys.length > 0
		? `${setting.displayName()} [${keys.join(", ")}]`
		: setting.displayName();
}

// compareの-setting（比較キー設定）の編集ダイアログ。
// テーブル名・カラム名の候補はDatasetSrcInfoProvider（CompareFormではold側のデータセット）から取得する
export default function CompareKeySettingsDialog(props: {
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const { settings, loading } = useDatasetSettingsData(props.fileName);
	if (loading) {
		return <StatusMessage kind="loading">Loading...</StatusMessage>;
	}
	return (
		<CompareKeySettingsEditDialog
			settings={settings}
			fileName={props.fileName}
			handleDialogClose={props.handleDialogClose}
			handleSave={props.handleSave}
		/>
	);
}

// 編集するのは対象テーブルとkeysだけ。ファイルにあるそれ以外の項目（filter・outputFilter・import等）は保存時もそのまま残る
export function CompareKeySettingsEditDialog(props: {
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
			title="Compare Key Settings"
			helpCommand="dialogs/compare-key-settings"
			helpLabel="Compare Key Settings"
			handleDialogClose={props.handleDialogClose}
			fileName={props.fileName}
			save={(path) => saveSettings(path, dataSettings)}
			handleSave={props.handleSave}
		>
			<SettingTable<DatasetSetting>
				caption="Key Settings"
				settings={dataSettings.settings}
				setSettings={(convertSettings) =>
					setDataSettings((cur) =>
						cur.replaceSettings(convertSettings(cur.settings)),
					)
				}
				renderSetting={renderKeySetting}
				SettingDialogComponent={CompareKeySettingDialog}
				newSetting={newDatasetSetting}
			/>
			<SettingTable<DatasetSetting>
				caption="Common Key Settings"
				settings={dataSettings.commonSettings}
				setSettings={(convertCommon) =>
					setDataSettings((cur) =>
						cur.replaceCommonSettings(convertCommon(cur.commonSettings)),
					)
				}
				renderSetting={renderKeySetting}
				SettingDialogComponent={CompareKeySettingDialog}
				newSetting={newDatasetSetting}
			/>
		</SettingDialog>
	);
}

// 1件分の比較キー設定。DatasetSettingDialogのTargetとkeysだけを使う
export function CompareKeySettingDialog(props: {
	setting: DatasetSetting;
	handleDialogClose: () => void;
	handleCommit: (newSettings: DatasetSetting) => void;
}) {
	const [target, setTarget] = useState(props.setting);
	const candidates = useColumnCandidates();

	return (
		<SettingDialog
			title="Compare Key Setting"
			helpCommand="dialogs/compare-key-settings"
			helpLabel="Compare Key Setting"
			setting={target}
			handleDialogClose={props.handleDialogClose}
			handleCommit={props.handleCommit}
		>
			<TargetFields
				target={target}
				setTarget={setTarget}
				tableNames={candidates.tableNames}
			/>
			<FilterOrderFields
				target={target}
				setTarget={setTarget}
				columnDatalist={candidates.columnDatalist}
				columnList={candidates.columnList}
				onLoadColumns={candidates.openColumnDialog}
				legend="Keys"
				fieldNames={keysOnly}
			/>
			{candidates.showColumnDialog && (
				<ColumnDatalistDialog
					tableNames={candidates.tableNames}
					target={target}
					onLoad={candidates.setColumnDatalist}
					onClose={candidates.closeColumnDialog}
				/>
			)}
		</SettingDialog>
	);
}
