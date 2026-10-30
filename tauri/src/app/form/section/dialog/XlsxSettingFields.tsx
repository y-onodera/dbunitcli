import { type Dispatch, type SetStateAction, useState } from "react";
import { Check, Text } from "../../../../components/dialog";
import { BlueButton } from "../../../../components/element/Button";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useSrcInfoSheets } from "../../../../hooks/useXlsxSchema";
import {
	type DatasetSrcInfo,
	srcTypeOf,
	withoutSetting,
} from "../../../../model/CommandOption";
import type { XlsxSchema } from "../../../../model/XlsxSchema";
import { DatasetTableNamesPreviewDialog } from "./DatasetTableNamesPreviewButton";
import { CommaSeparatedText, TextWithDatalist } from "./DialogFields";

// RowSetting・CellSettingが共通で持つ項目
type XlsxCommonBuilder = {
	sheetName?: string;
	tableName?: string;
	header?: string[];
	addFileInfo?: boolean;
};

type XlsxCommonSetting<T> = Required<XlsxCommonBuilder> & {
	with(builder: XlsxCommonBuilder): T;
};

type XlsxFieldsProps<T> = {
	target: T;
	setTarget: Dispatch<SetStateAction<T>>;
};

// sheetName(シート名候補付き)・tableName・header
export function XlsxTableFields<T extends XlsxCommonSetting<T>>({
	target,
	setTarget,
}: XlsxFieldsProps<T>) {
	const sheetNames = useSrcInfoSheets(useDatasetSrcInfo());
	return (
		<>
			<TextWithDatalist
				name="sheetName"
				value={target.sheetName}
				options={sheetNames}
				handleChange={(ev) =>
					setTarget((cur) => cur.with({ sheetName: ev.target.value }))
				}
			/>
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

// SettingDialogのfooterActionsに置き、編集中の設定1件だけのスキーマで読み込まれるテーブルをプレビューする（保存はしない）
export function XlsxSettingPreviewButton({
	toSchema,
}: {
	toSchema: () => XlsxSchema;
}) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const [previewSrcInfo, setPreviewSrcInfo] = useState<DatasetSrcInfo | null>(
		null,
	);
	if (!srcTypeOf(datasetSrcInfo)) {
		return null;
	}
	return (
		<>
			<BlueButton
				title="Preview"
				handleClick={() =>
					setPreviewSrcInfo({
						...withoutSetting(datasetSrcInfo),
						inlineXlsxSchema: toSchema(),
					})
				}
			/>
			{previewSrcInfo !== null && (
				<DatasetTableNamesPreviewDialog
					datasetSrcInfo={previewSrcInfo}
					handleDialogClose={() => setPreviewSrcInfo(null)}
				/>
			)}
		</>
	);
}
