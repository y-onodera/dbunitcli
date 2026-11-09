import type { Dispatch, SetStateAction } from "react";
import { Arrays, Check, Select, Text } from "../../../../components/dialog";
import { ResourceDatalist } from "../../../../components/element/Input";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useSrcInfoSheets } from "../../../../hooks/useXlsxSchema";
import {
	replaceSheetPattern,
	type SheetPattern,
	switchSheetTarget,
} from "../../../../model/XlsxSchema";
import { removeArray, replaceArray } from "../../../../utils/arrayUtils";
import { CommaSeparatedText, TextWithDatalist } from "./DialogFields";

// RowSetting・CellSettingが共通で持つ項目
type XlsxCommonBuilder = {
	sheetName?: string;
	sheetPattern?: SheetPattern;
	tableName?: string;
	header?: string[];
	addFileInfo?: boolean;
};

type XlsxCommonSetting<T> = Required<Omit<XlsxCommonBuilder, "sheetPattern">> &
	Pick<XlsxCommonBuilder, "sheetPattern"> & {
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
					setTarget((cur) => switchSheetTarget(cur, selected))
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
						setTarget((cur) => replaceSheetPattern(cur, change))
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
	const datalistId = "sheetPatternExclude";
	const list = sheetNames.length > 0 ? `${datalistId}_list` : undefined;
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
					handleChange({ exclude: replaceArray(exclude, text, index) })
				}
				handleRemove={(index) =>
					handleChange({ exclude: removeArray(exclude, index) })
				}
			/>
			{list && <ResourceDatalist id={datalistId} resources={sheetNames} />}
		</>
	);
}
