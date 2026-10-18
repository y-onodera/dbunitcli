import { useState } from "react";
import {
	Arrays,
	Fieldset,
	Select,
	SettingDialog,
	SettingTable,
	StatusMessage,
	Text,
} from "../../../../components/dialog";
import {
	useDatasetSettingsData,
	useDeleteDatasetSettings,
	useSaveDatasetSettings,
} from "../../../../hooks/useDatasetSettings";
import type {
	DatasetSetting,
	OutputFilter,
} from "../../../../model/DatasetSettings";
import {
	type DatasetSettings,
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
						return cur.replaceSettings(updatedSettings);
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
						return cur.replaceCommonSettings(updatedCommonSettings);
					})
				}
				SettingDialogComponent={DatasetSettingDialog}
				newSetting={newDatasetSetting}
			/>
			<OutputFilterFields
				outputFilter={dataSettings.outputFilter}
				setOutputFilter={(outputFilter) =>
					setDataSettings((cur) => cur.replaceOutputFilter(outputFilter))
				}
			/>
		</SettingDialog>
	);
}

// separate/join後の出力テーブルを絞る。nameは完全一致、patternは部分一致(exclude付き)
function OutputFilterFields(props: {
	outputFilter: OutputFilter;
	setOutputFilter: (outputFilter: OutputFilter) => void;
}) {
	const { outputFilter, setOutputFilter } = props;
	const kind = outputFilterKind(outputFilter);
	return (
		<Fieldset legend="Output Filter">
			<Select
				name="outputFilter"
				defaultValue={kind}
				handleOnChange={async (select) => {
					if (select === "name") {
						setOutputFilter({ name: [""] });
					} else if (select === "pattern") {
						setOutputFilter({ pattern: { string: "" } });
					} else {
						setOutputFilter({});
					}
				}}
			>
				<option value="">none</option>
				<option value="name">name</option>
				<option value="pattern">pattern</option>
			</Select>
			{kind === "name" && (
				<Arrays
					name="name"
					values={outputFilter.name ?? []}
					handleChange={(text, index) =>
						setOutputFilter({
							name: replaceAt(outputFilter.name ?? [], text, index),
						})
					}
					handleRemove={(index) =>
						setOutputFilter({
							name: (outputFilter.name ?? []).filter((_, i) => i !== index),
						})
					}
				/>
			)}
			{kind === "pattern" && (
				<>
					<Text
						name="pattern"
						ignoreLabel={true}
						value={outputFilter.pattern?.string ?? ""}
						handleChange={(text) =>
							setOutputFilter({
								pattern: { ...outputFilter.pattern, string: text.target.value },
							})
						}
					/>
					<Arrays
						name="patternExclude"
						values={outputFilter.pattern?.exclude ?? []}
						handleChange={(text, index) =>
							setOutputFilter({
								pattern: {
									string: outputFilter.pattern?.string ?? "",
									exclude: replaceAt(
										outputFilter.pattern?.exclude ?? [],
										text,
										index,
									),
								},
							})
						}
						handleRemove={(index) =>
							setOutputFilter({
								pattern: {
									string: outputFilter.pattern?.string ?? "",
									exclude: (outputFilter.pattern?.exclude ?? []).filter(
										(_, i) => i !== index,
									),
								},
							})
						}
					/>
				</>
			)}
		</Fieldset>
	);
}

function outputFilterKind(outputFilter: OutputFilter): string {
	if (outputFilter.pattern) {
		return "pattern";
	}
	return outputFilter.name ? "name" : "";
}

function replaceAt(array: string[], text: string, index: number): string[] {
	const result = [...array];
	result[index] = text;
	return result;
}

export const DatasetSettingEditButton = createEditButton(DatasetSettingsDialog);
export const RemoveDatasetSettingButton = createRemoveButton(
	useDeleteDatasetSettings,
);
