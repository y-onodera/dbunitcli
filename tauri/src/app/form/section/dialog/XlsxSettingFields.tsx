import type { Dispatch, SetStateAction } from "react";
import { Arrays, Check, Select, Text } from "../../../../components/dialog";
import { ResourceDatalist } from "../../../../components/element/Input";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useSrcInfoSheets } from "../../../../hooks/useXlsxSchema";
import type { SheetPattern } from "../../../../model/XlsxSchema";
import { CommaSeparatedText, TextWithDatalist } from "./DialogFields";

// RowSetting・CellSettingが共通で持つ項目
type XlsxCommonBuilder = {
	sheetName?: string;
	sheetPattern?: SheetPattern;
	tableName?: string;
	header?: string[];
	addFileInfo?: boolean;
};

type XlsxCommonSetting<T> = Required<
	Omit<XlsxCommonBuilder, "sheetPattern">
> & {
	sheetPattern?: SheetPattern;
	with(builder: XlsxCommonBuilder): T;
};

type XlsxFieldsProps<T> = {
	target: T;
	setTarget: Dispatch<SetStateAction<T>>;
};

// sheet(名前またはパターン。シート名候補付き)・tableName・header
export function XlsxTableFields<T extends XlsxCommonSetting<T>>({
	target,
	setTarget,
}: XlsxFieldsProps<T>) {
	const sheetNames = useSrcInfoSheets(useDatasetSrcInfo());
	return (
		<>
			<Select
				name="sheet"
				defaultValue={target.sheetPattern ? "pattern" : "name"}
				handleOnChange={async (selected) =>
					setTarget((cur) =>
						cur.with({
							sheetPattern:
								selected === "pattern" ? { string: cur.sheetName } : undefined,
							sheetName:
								selected === "pattern" ? "" : (cur.sheetPattern?.string ?? ""),
						}),
					)
				}
			>
				<option value="name">name</option>
				<option value="pattern">pattern</option>
			</Select>
			{target.sheetPattern ? (
				<SheetPatternFields
					pattern={target.sheetPattern}
					sheetNames={sheetNames}
					handleChange={(change) =>
						setTarget((cur) =>
							cur.with({ sheetPattern: { ...cur.sheetPattern, ...change } }),
						)
					}
				/>
			) : (
				<TextWithDatalist
					name="sheetName"
					value={target.sheetName}
					options={sheetNames}
					handleChange={(ev) =>
						setTarget((cur) => cur.with({ sheetName: ev.target.value }))
					}
				/>
			)}
			<Text
				name="tableName"
				value={target.tableName}
				handleChange={(ev) =>
					setTarget((cur) => cur.with({ tableName: ev.target.value }))
				}
			/>
			<CommaSeparatedText
				name="header"
				values={target.header}
				handleChange={(header) => setTarget((cur) => cur.with({ header }))}
			/>
		</>
	);
}

export function AddFileInfoCheck<T extends XlsxCommonSetting<T>>({
	target,
	setTarget,
}: XlsxFieldsProps<T>) {
	return (
		<Check
			name="addFileInfo"
			value={target.addFileInfo ? "true" : "false"}
			handleOnChange={(checked) =>
				setTarget((cur) => cur.with({ addFileInfo: checked }))
			}
		/>
	);
}

function SheetPatternFields({
	pattern,
	sheetNames,
	handleChange,
}: {
	pattern: SheetPattern;
	sheetNames: string[];
	handleChange: (change: SheetPattern) => void;
}) {
	const exclude = pattern.exclude ?? [];
	const list = sheetNames.length > 0 ? "sheetPatternExclude_list" : undefined;
	return (
		<>
			<Text
				name="string"
				value={pattern.string ?? ""}
				handleChange={(ev) =>
					handleChange({ string: ev.target.value || undefined })
				}
			/>
			<Text
				name="regex"
				value={pattern.regex ?? ""}
				handleChange={(ev) =>
					handleChange({ regex: ev.target.value || undefined })
				}
			/>
			<Arrays
				name="exclude"
				values={exclude}
				list={list}
				handleChange={(text, index) =>
					handleChange({
						exclude:
							index < exclude.length
								? exclude.map((it, i) => (i === index ? text : it))
								: exclude.concat(text),
					})
				}
				handleRemove={(index) =>
					handleChange({ exclude: exclude.filter((_, i) => i !== index) })
				}
			/>
			{list && (
				<ResourceDatalist id="sheetPatternExclude" resources={sheetNames} />
			)}
		</>
	);
}
