import type React from "react";
import { useState } from "react";
import {
	Arrays,
	Fieldset,
	KeyValues,
	Select,
	SettingDialog,
	SettingTable,
	Text,
} from "../../../../components/dialog";
import {
	ButtonIcon,
	ExpandButton,
} from "../../../../components/element/ButtonIcon";
import { HelpIcon } from "../../../../components/element/Icon";
import { ResourceDatalist } from "../../../../components/element/Input";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useDatasetTableNames } from "../../../../hooks/useDatasetSettings";
import type {
	DatasetSetting,
	DatasetSettingMode,
} from "../../../../model/DatasetSettings";
import { newDatasetSetting } from "../../../../model/DatasetSettings";
import { openHelpWindow } from "../../../../utils/helpWindow";

export default function DatasetSettingDialog(props: {
	setting: DatasetSetting;
	handleDialogClose: () => void;
	handleCommit: (newSettings: DatasetSetting) => void;
}) {
	const [target, setTarget] = useState(props.setting);
	const handleTargetChange = async (select: string) =>
		setTarget((current) => current.replace(select));
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);
	const datasetSrcInfo = useDatasetSrcInfo();
	const { tableNames } = useDatasetTableNames(datasetSrcInfo);
	const tableList = tableNames.length > 0 ? "tableName_list" : undefined;

	return (
		<SettingDialog
			setting={target}
			handleDialogClose={props.handleDialogClose}
			handleCommit={props.handleCommit}
		>
			<div className="flex justify-end px-4 pt-2">
				<ButtonIcon
					title="Help"
					handleClick={() => {
						openHelpWindow("dataset-setting", "Dataset Setting");
					}}
				>
					<HelpIcon />
				</ButtonIcon>
			</div>
			<Fieldset legend="Target">
				<Select
					name="target"
					defaultValue={target.handler()}
					handleOnChange={handleTargetChange}
				>
					<option value="name">name</option>
					<option value="pattern">pattern</option>
					<option value="outerJoin">outerJoin</option>
					<option value="innerJoin">innerJoin</option>
					<option value="fullJoin">fullJoin</option>
				</Select>
				{renderTargetContent(target, setTarget, tableList)}
				{tableNames.length > 0 && (
					<ResourceDatalist id="tableName" resources={tableNames} />
				)}
			</Fieldset>
			<Fieldset legend="Split / Rename / Separate">
				<Select
					name="mode"
					defaultValue={target.mode()}
					handleOnChange={(v) =>
						setTarget((cur) => cur.withMode(v as DatasetSettingMode))
					}
				>
					<option value="rename">rename</option>
					<option value="split">split</option>
					<option value="separate">separate</option>
				</Select>
				{renderModeContent(target, setTarget)}
			</Fieldset>
			<Fieldset legend="Additional Columns">
				<ExpandButton
					toggleOptional={toggleOptional}
					showOptional={showOptional}
					caption="additional columns"
				/>
				{showOptional && (
					<>
						<KeyValues
							name="string"
							values={target.string}
							handleChange={(index, newValue) =>
								setTarget((cur) => cur.replaceString(index, newValue))
							}
							handleRemove={(index) =>
								setTarget((cur) => cur.removeString(index))
							}
						/>
						<KeyValues
							name="number"
							values={target.number}
							handleChange={(index, newValue) =>
								setTarget((cur) => cur.replaceNumber(index, newValue))
							}
							handleRemove={(index) =>
								setTarget((cur) => cur.removeNumber(index))
							}
						/>
						<KeyValues
							name="boolean"
							values={target.boolean}
							handleChange={(index, newValue) =>
								setTarget((cur) => cur.replaceBoolean(index, newValue))
							}
							handleRemove={(index) =>
								setTarget((cur) => cur.removeBoolean(index))
							}
						/>
						<KeyValues
							name="function"
							values={target.function}
							handleChange={(index, newValue) =>
								setTarget((cur) => cur.replaceFunction(index, newValue))
							}
							handleRemove={(index) =>
								setTarget((cur) => cur.removeFunction(index))
							}
						/>
					</>
				)}
			</Fieldset>
			<Fieldset legend="Filter / Order">
				<Arrays
					name="exclude"
					values={target.exclude}
					handleChange={(text, index) =>
						setTarget((cur) => cur.replaceExclude(text, index))
					}
					handleRemove={(index) => setTarget((cur) => cur.removeExclude(index))}
				/>
				<Arrays
					name="include"
					values={target.include}
					handleChange={(text, index) =>
						setTarget((cur) => cur.replaceInclude(text, index))
					}
					handleRemove={(index) => setTarget((cur) => cur.removeInclude(index))}
				/>
				<Arrays
					name="keys"
					values={target.keys}
					handleChange={(text, index) =>
						setTarget((cur) => cur.replaceKeys(text, index))
					}
					handleRemove={(index) => setTarget((cur) => cur.removeKeys(index))}
				/>
				<Arrays
					name="filter"
					values={target.filter}
					handleChange={(text, index) =>
						setTarget((cur) => cur.replaceFilter(text, index))
					}
					handleRemove={(index) => setTarget((cur) => cur.removeFilter(index))}
				/>
				<Arrays
					name="order"
					values={target.order}
					handleChange={(text, index) =>
						setTarget((cur) => cur.replaceOrder(text, index))
					}
					handleRemove={(index) => setTarget((cur) => cur.removeOrder(index))}
				/>
			</Fieldset>
		</SettingDialog>
	);
}

function renderModeContent(
	target: DatasetSetting,
	setTarget: React.Dispatch<React.SetStateAction<DatasetSetting>>,
): React.ReactElement {
	const mode = target.mode();
	if (mode === "split") {
		return (
			<>
				<Text
					name="prefix"
					value={target.split?.prefix ?? ""}
					handleChange={(newVal) =>
						setTarget((cur) =>
							cur.replaceSplit({ prefix: newVal.target.value }),
						)
					}
				/>
				<Text
					name="tableName"
					value={target.split?.tableName ?? ""}
					handleChange={(newVal) =>
						setTarget((cur) =>
							cur.replaceSplit({ tableName: newVal.target.value }),
						)
					}
				/>
				<Text
					name="suffix"
					value={target.split?.suffix ?? ""}
					handleChange={(newVal) =>
						setTarget((cur) =>
							cur.replaceSplit({ suffix: newVal.target.value }),
						)
					}
				/>
				<Text
					name="limit"
					value={target.split?.limit ?? ""}
					handleChange={(newVal) =>
						setTarget((cur) =>
							cur.replaceSplit({ limit: newVal.target.value }),
						)
					}
				/>
				<Arrays
					name="breakKey"
					values={target.split?.breakKey ?? []}
					handleChange={(text, index) =>
						setTarget((cur) => cur.replaceSplitBreakKey(text, index))
					}
					handleRemove={(index) =>
						setTarget((cur) => cur.removeSplitBreakKey(index))
					}
				/>
			</>
		);
	}
	if (mode === "separate") {
		return (
			<SettingTable<DatasetSetting>
				caption="Separate"
				settings={target.separate}
				setSettings={(convert) =>
					setTarget((cur) => cur.with({ separate: convert(cur.separate) }))
				}
				renderSetting={(setting) => setting.displayName()}
				SettingDialogComponent={DatasetSettingDialog}
				newSetting={newDatasetSetting}
				getKey={(setting) => setting.displayName()}
			/>
		);
	}
	return (
		<>
			<Text
				name="prefix"
				value={target.prefix ?? ""}
				handleChange={(newVal) =>
					setTarget((cur) => cur.with({ prefix: newVal.target.value }))
				}
			/>
			<Text
				name="tableName"
				value={target.tableName ?? ""}
				handleChange={(newVal) =>
					setTarget((cur) => cur.with({ tableName: newVal.target.value }))
				}
			/>
			<Text
				name="suffix"
				value={target.suffix ?? ""}
				handleChange={(newVal) =>
					setTarget((cur) => cur.with({ suffix: newVal.target.value }))
				}
			/>
		</>
	);
}

function renderTargetContent(
	target: DatasetSetting,
	setTarget: React.Dispatch<React.SetStateAction<DatasetSetting>>,
	tableList: string | undefined,
): React.ReactElement {
	if (target.join()) {
		return (
			<>
				<Text
					name="left"
					value={target.join()?.left ?? ""}
					list={tableList}
					handleChange={(newVal) =>
						setTarget((cur) => cur.replaceJoin({ left: newVal.target.value }))
					}
				/>
				<Text
					name="right"
					value={target.join()?.right ?? ""}
					list={tableList}
					handleChange={(newVal) =>
						setTarget((cur) => cur.replaceJoin({ right: newVal.target.value }))
					}
				/>
				<Select
					name="condition"
					defaultValue={target.join()?.on ? "on" : "column"}
					handleOnChange={async (select) =>
						setTarget((cur) =>
							cur.replaceJoin({ [select]: select === "on" ? "" : [] }),
						)
					}
				>
					<option value="on">on</option>
					<option value="column">column</option>
				</Select>
				{target.join()?.on ? (
					<Text
						name="on"
						ignoreLabel={true}
						value={target.join()?.on ?? ""}
						handleChange={(newVal) =>
							setTarget((cur) => cur.replaceJoin({ on: newVal.target.value }))
						}
					/>
				) : (
					<Arrays
						name="column"
						ignoreLabel={true}
						values={target.join()?.column ?? []}
						handleChange={(text, index) =>
							setTarget((cur) => cur.replaceJoinColumn(text, index))
						}
						handleRemove={(index) =>
							setTarget((cur) => cur.removeJoinColumn(index))
						}
					/>
				)}
			</>
		);
	}
	if (target.pattern) {
		return (
			<>
				<Text
					name="pattern"
					ignoreLabel={true}
					value={target.pattern?.string ?? ""}
					handleChange={(text) =>
						setTarget((cur) =>
							cur.replacePattern({ string: text.target.value }),
						)
					}
				/>
				<Arrays
					name="patternExclue"
					values={target.pattern?.exclude ?? []}
					list={tableList}
					handleChange={(text, index) =>
						setTarget((cur) => cur.replacePatternExclude(text, index))
					}
					handleRemove={(index) =>
						setTarget((cur) => cur.removePatternExclude(index))
					}
				/>
			</>
		);
	}
	return (
		<>
			<Arrays
				name="name"
				values={target.name ?? []}
				list={tableList}
				handleChange={(text, index) =>
					setTarget((cur) => cur.replaceName(text, index))
				}
				handleRemove={(index) => setTarget((cur) => cur.removeName(index))}
			/>
			<Text
				name="filePath"
				value={target.filePath ?? ""}
				handleChange={(newVal) =>
					setTarget((cur) => cur.replaceFilePath(newVal.target.value))
				}
			/>
		</>
	);
}
