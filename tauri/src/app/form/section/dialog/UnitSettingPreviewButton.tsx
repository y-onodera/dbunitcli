import { useState } from "react";
import {
	DialogBody,
	DialogFooter,
	DialogTitle,
	FullDialog,
	type SaveThen,
} from "../../../../components/dialog";
import { WhiteButton } from "../../../../components/element/Button";
import { IconButton } from "../../../../components/element/ButtonIcon";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useUnitSettingEncoding } from "../../../../context/UnitSettingEncodingProvider";
import {
	type UnitTablePreview,
	useDatasetUnitTablePreview,
} from "../../../../hooks/useDatasetSettings";
import {
	type DatasetSrcInfo,
	srcTypeOf,
} from "../../../../model/CommandOption";
import DatasetSettingsDialog from "./DatasetSettingsDialog";
import {
	DataPreviewDialog,
	SaveAndPreviewButton,
	TableNameList,
	TablePreviewTable,
} from "./DatasetTableNamesPreviewButton";

// srcTypeとsrcが入力されていればプレビューできる
function canPreview(srcInfo: DatasetSrcInfo): boolean {
	return !!srcTypeOf(srcInfo) && !!srcInfo.values.src;
}

// generateの-unitSettingのプレビュー。srcのsettingを適用したテーブル一覧を出し、各テーブルの横のボタンで
// そのテーブルにunitSettingを適用した後のテーブル（rowsになるテーブル自身とsubTable）を確認する。
// srcはDatasetSrcInfoProvider、文字コードはUnitSettingEncodingProviderの入力中の値を使う
export default function UnitSettingPreviewButton({
	unitSetting,
}: {
	unitSetting: string;
}) {
	if (!unitSetting) {
		return null;
	}
	return (
		<UnitPreviewLauncher
			title="Preview Unit Settings"
			saveThen={(onSaved) => onSaved(unitSetting)}
		/>
	);
}

// unitSettingの編集ダイアログ（Dataset Settings）。Save and Previewは、保存したファイルを
// srcのsettingではなくunitSettingとして適用してプレビューする（Preview Unit Settingsと同じ動作）
export function UnitSettingsDialog(props: {
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	return (
		<DatasetSettingsDialog
			{...props}
			saveAndPreview={(saveThen) => (
				<UnitPreviewLauncher title="Save and Preview" saveThen={saveThen} />
			)}
		/>
	);
}

// 押すとsaveThenでunitSettingのファイル（指定済みのもの・保存したもの）を受け取り、プレビューを開く
function UnitPreviewLauncher({
	title,
	saveThen,
}: {
	title: string;
	saveThen: SaveThen;
}) {
	const datasetSrcInfo = useDatasetSrcInfo();
	if (!canPreview(datasetSrcInfo)) {
		return null;
	}
	return (
		<SaveAndPreviewButton
			title={title}
			saveThen={saveThen}
			toSrcInfo={() => datasetSrcInfo}
			rowActions={(unitSetting, tableName) => (
				<UnitTablesButton tableName={tableName} unitSetting={unitSetting} />
			)}
		/>
	);
}

function UnitTablesButton({
	tableName,
	unitSetting,
}: {
	tableName: string;
	unitSetting: string;
}) {
	const [open, setOpen] = useState(false);
	return (
		<>
			<IconButton
				iconType="settingsApplication"
				title="Unit Preview"
				handleClick={() => setOpen(true)}
			/>
			{open && (
				<UnitTablesDialog
					tableName={tableName}
					unitSetting={unitSetting}
					handleClose={() => setOpen(false)}
				/>
			)}
		</>
	);
}

function UnitTablesDialog({
	tableName,
	unitSetting,
	handleClose,
}: {
	tableName: string;
	unitSetting: string;
	handleClose: () => void;
}) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const encoding = useUnitSettingEncoding();
	const { previews, loading, logs, error } = useDatasetUnitTablePreview(
		datasetSrcInfo,
		tableName,
		unitSetting,
		encoding,
	);
	const [selected, setSelected] = useState<UnitTablePreview | null>(null);

	return (
		<FullDialog onClose={handleClose}>
			<DialogBody size="fluid">
				<DialogTitle>{tableName} — Unit Table List Preview</DialogTitle>
				<TableNameList
					names={previews.map((it) => it.tableName)}
					loading={loading}
					logs={logs}
					error={error}
					onAbandon={handleClose}
					onPreview={(name) =>
						setSelected(previews.find((it) => it.tableName === name) ?? null)
					}
				/>
			</DialogBody>
			<DialogFooter>
				<WhiteButton title="Close" handleClick={handleClose} />
			</DialogFooter>
			{selected !== null && (
				<DataPreviewDialog
					tableName={selected.tableName}
					handleClose={() => setSelected(null)}
					body={<TablePreviewTable preview={selected} />}
				/>
			)}
		</FullDialog>
	);
}
