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
	AddButton,
	RemoveButton,
} from "../../../../components/element/ButtonIcon";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import {
	useDatasetSettingsData,
	useSaveDatasetSettings,
} from "../../../../hooks/useDatasetSettings";
import { srcTypeOf, withValues } from "../../../../model/CommandOption";
import type {
	DatasetSetting,
	ImportSetting,
	OutputFilter,
} from "../../../../model/DatasetSettings";
import {
	type DatasetSettings,
	newDatasetSetting,
} from "../../../../model/DatasetSettings";
import DatasetSettingDialog from "./DatasetSettingDialog";
import { SaveAndPreviewButton } from "./DatasetTableNamesPreviewButton";

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
	const datasetSrcInfo = useDatasetSrcInfo();
	const save = (path: string) => saveSettings(path, dataSettings);
	return (
		<SettingDialog
			title="Dataset Settings"
			helpCommand="dialogs/dataset-settings"
			helpLabel="Dataset Settings"
			handleDialogClose={props.handleDialogClose}
			fileName={props.fileName}
			save={save}
			handleSave={props.handleSave}
			footerActions={(fileName) =>
				// 保存したファイルをsettingに適用してプレビューする（それ以外のsrc要素はフォームのまま）
				srcTypeOf(datasetSrcInfo) && (
					<SaveAndPreviewButton
						path={fileName}
						save={save}
						toSrcInfo={(path) => withValues(datasetSrcInfo, { setting: path })}
					/>
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
			<ImportFields
				imports={dataSettings.imports}
				setImports={(imports) =>
					setDataSettings((cur) => cur.replaceImports(imports))
				}
			/>
		</SettingDialog>
	);
}

// 他の設定ファイルの取り込み。outputFilterCombineは取り込み先のoutputFilterとの結合方法(既定and)
function ImportFields(props: {
	imports: ImportSetting[];
	setImports: (imports: ImportSetting[]) => void;
}) {
	const { imports, setImports } = props;
	const rows = imports.length > 0 ? imports : [{ path: "" }];
	const update = (index: number, row: ImportSetting) =>
		setImports(rows.map((it, i) => (i === index ? row : it)));
	return (
		<Fieldset legend="Import">
			{rows.map((row, index) => (
				// biome-ignore lint/suspicious/noArrayIndexKey: 行は追加・削除のみで並び替えない
				<div key={index}>
					<Text
						name={`importPath${index}`}
						value={row.path}
						handleChange={(text) =>
							update(index, { ...row, path: text.target.value })
						}
					/>
					<Select
						name={`importOutputFilterCombine${index}`}
						defaultValue={row.outputFilterCombine ?? "and"}
						handleOnChange={async (select) =>
							update(index, {
								...row,
								outputFilterCombine: select === "or" ? "or" : undefined,
							})
						}
					>
						<option value="and">and</option>
						<option value="or">or</option>
					</Select>
					{rows.length > 1 && (
						<RemoveButton
							handleClick={() => setImports(rows.filter((_, i) => i !== index))}
						/>
					)}
				</div>
			))}
			{rows[rows.length - 1].path && (
				<AddButton handleClick={() => setImports([...rows, { path: "" }])} />
			)}
		</Fieldset>
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
						value={outputFilter.pattern?.string ?? ""}
						handleChange={(text) =>
							setOutputFilter({
								pattern: { ...outputFilter.pattern, string: text.target.value },
							})
						}
					/>
					<Text
						name="regex"
						value={outputFilter.pattern?.regex ?? ""}
						handleChange={(text) =>
							setOutputFilter({
								pattern: {
									...outputFilter.pattern,
									regex: text.target.value || undefined,
								},
							})
						}
					/>
					<Arrays
						name="patternExclude"
						values={outputFilter.pattern?.exclude ?? []}
						handleChange={(text, index) =>
							setOutputFilter({
								pattern: {
									...outputFilter.pattern,
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
									...outputFilter.pattern,
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
