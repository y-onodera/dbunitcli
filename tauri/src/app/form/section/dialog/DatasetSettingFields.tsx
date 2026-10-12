import type { ComponentType, Dispatch, SetStateAction } from "react";
import { useState } from "react";
import type { SettingDialogComponentProps } from "../../../../components/dialog";
import {
	Arrays,
	Fieldset,
	KeyValues,
	Select,
	SettingTable,
	Text,
} from "../../../../components/dialog";
import {
	BlueButtonIcon,
	ExpandButton,
} from "../../../../components/element/ButtonIcon";
import { PreviewIcon } from "../../../../components/element/Icon";
import { ResourceDatalist } from "../../../../components/element/Input";
import type {
	DatasetSetting,
	DatasetSettingMode,
} from "../../../../model/DatasetSettings";
import { newDatasetSetting } from "../../../../model/DatasetSettings";

export type DatasetSettingFieldsProps = {
	target: DatasetSetting;
	setTarget: Dispatch<SetStateAction<DatasetSetting>>;
};

export function TargetFields({
	target,
	setTarget,
	tableNames,
}: DatasetSettingFieldsProps & { tableNames: string[] }) {
	const tableList = tableNames.length > 0 ? "tableName_list" : undefined;
	return (
		<Fieldset legend="Target">
			<Select
				name="target"
				defaultValue={target.handler()}
				handleOnChange={async (select) =>
					setTarget((current) => current.replace(select))
				}
			>
				<option value="name">name</option>
				<option value="pattern">pattern</option>
				<option value="outerJoin">outerJoin</option>
				<option value="innerJoin">innerJoin</option>
				<option value="fullJoin">fullJoin</option>
			</Select>
			<TargetContent
				target={target}
				setTarget={setTarget}
				tableList={tableList}
			/>
			{tableNames.length > 0 && (
				<ResourceDatalist id="tableName" resources={tableNames} />
			)}
		</Fieldset>
	);
}

export function ModeFields({
	target,
	setTarget,
	columnList,
	SeparateDialog,
}: DatasetSettingFieldsProps & {
	columnList: string | undefined;
	SeparateDialog: ComponentType<SettingDialogComponentProps<DatasetSetting>>;
}) {
	return (
		<Fieldset legend="Split / Rename / Separate">
			<Select
				name="mode"
				defaultValue={target.mode()}
				handleOnChange={async (v) =>
					setTarget((cur) => cur.withMode(v as DatasetSettingMode))
				}
			>
				<option value="rename">rename</option>
				<option value="split">split</option>
				<option value="separate">separate</option>
			</Select>
			<ModeContent
				target={target}
				setTarget={setTarget}
				columnList={columnList}
				SeparateDialog={SeparateDialog}
			/>
		</Fieldset>
	);
}

// 追加カラム(string/number/boolean/function)。モデルのreplace*/remove*をキー別に対応付ける
const additionalColumnFields = [
	{
		name: "string",
		replace: (s: DatasetSetting, i: number, v: { [prop: string]: string }) =>
			s.replaceString(i, v),
		remove: (s: DatasetSetting, i: number) => s.removeString(i),
	},
	{
		name: "number",
		replace: (s: DatasetSetting, i: number, v: { [prop: string]: string }) =>
			s.replaceNumber(i, v),
		remove: (s: DatasetSetting, i: number) => s.removeNumber(i),
	},
	{
		name: "boolean",
		replace: (s: DatasetSetting, i: number, v: { [prop: string]: string }) =>
			s.replaceBoolean(i, v),
		remove: (s: DatasetSetting, i: number) => s.removeBoolean(i),
	},
	{
		name: "function",
		replace: (s: DatasetSetting, i: number, v: { [prop: string]: string }) =>
			s.replaceFunction(i, v),
		remove: (s: DatasetSetting, i: number) => s.removeFunction(i),
	},
] as const;

export function AdditionalColumnsFields({
	target,
	setTarget,
}: DatasetSettingFieldsProps) {
	const [showOptional, setShowOptional] = useState(false);
	return (
		<Fieldset legend="Additional Columns">
			<ExpandButton
				toggleOptional={() => setShowOptional(!showOptional)}
				showOptional={showOptional}
				caption="additional columns"
			/>
			{showOptional &&
				additionalColumnFields.map((field) => (
					<KeyValues
						key={field.name}
						name={field.name}
						values={target[field.name]}
						handleChange={(index, newValue) =>
							setTarget((cur) => field.replace(cur, index, newValue))
						}
						handleRemove={(index) =>
							setTarget((cur) => field.remove(cur, index))
						}
					/>
				))}
		</Fieldset>
	);
}

// Filter/Orderの配列項目。モデルのreplace*/remove*をキー別に対応付ける
const filterOrderFields = [
	{
		name: "exclude",
		replace: (s: DatasetSetting, t: string, i: number) =>
			s.replaceExclude(t, i),
		remove: (s: DatasetSetting, i: number) => s.removeExclude(i),
	},
	{
		name: "include",
		replace: (s: DatasetSetting, t: string, i: number) =>
			s.replaceInclude(t, i),
		remove: (s: DatasetSetting, i: number) => s.removeInclude(i),
	},
	{
		name: "keys",
		replace: (s: DatasetSetting, t: string, i: number) => s.replaceKeys(t, i),
		remove: (s: DatasetSetting, i: number) => s.removeKeys(i),
	},
	{
		name: "filter",
		replace: (s: DatasetSetting, t: string, i: number) => s.replaceFilter(t, i),
		remove: (s: DatasetSetting, i: number) => s.removeFilter(i),
	},
	{
		name: "order",
		replace: (s: DatasetSetting, t: string, i: number) => s.replaceOrder(t, i),
		remove: (s: DatasetSetting, i: number) => s.removeOrder(i),
	},
] as const;

export function FilterOrderFields({
	target,
	setTarget,
	columnDatalist,
	columnList,
	onLoadColumns,
}: DatasetSettingFieldsProps & {
	columnDatalist: string[];
	columnList: string | undefined;
	onLoadColumns: () => void;
}) {
	return (
		<Fieldset legend="Filter / Order">
			<div className="flex justify-end pb-1">
				<BlueButtonIcon
					title="Load column datalist"
					handleClick={onLoadColumns}
				>
					<PreviewIcon title="Load column datalist" fill="white" />
				</BlueButtonIcon>
			</div>
			{filterOrderFields.map((field) => (
				<Arrays
					key={field.name}
					name={field.name}
					values={target[field.name]}
					list={columnList}
					handleChange={(text, index) =>
						setTarget((cur) => field.replace(cur, text, index))
					}
					handleRemove={(index) => setTarget((cur) => field.remove(cur, index))}
				/>
			))}
			{columnDatalist.length > 0 && (
				<ResourceDatalist id="column_datalist" resources={columnDatalist} />
			)}
		</Fieldset>
	);
}

function ModeContent({
	target,
	setTarget,
	columnList,
	SeparateDialog,
}: DatasetSettingFieldsProps & {
	columnList: string | undefined;
	// 再帰的に自身(DatasetSettingDialog)を使うため循環importを避けて注入する
	SeparateDialog: ComponentType<SettingDialogComponentProps<DatasetSetting>>;
}) {
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
						setTarget((cur) => cur.replaceSplit({ limit: newVal.target.value }))
					}
				/>
				<Arrays
					name="breakKey"
					values={target.split?.breakKey ?? []}
					list={columnList}
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
				SettingDialogComponent={SeparateDialog}
				newSetting={newDatasetSetting}
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

function TargetContent({
	target,
	setTarget,
	tableList,
}: DatasetSettingFieldsProps & { tableList: string | undefined }) {
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
					hideCaption={true}
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
				hideCaption={true}
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
