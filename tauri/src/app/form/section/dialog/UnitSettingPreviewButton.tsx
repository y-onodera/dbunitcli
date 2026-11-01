import { useState } from "react";
import {
	DialogBody,
	DialogFooter,
	DialogTitle,
	FullDialog,
	JobFailure,
	type SaveThen,
	StatusMessage,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import {
	IconButton,
	PreviewButton,
} from "../../../../components/element/ButtonIcon";
import { JobProgress } from "../../../../components/element/JobProgress";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useUnitSettingEncoding } from "../../../../context/UnitSettingEncodingProvider";
import {
	type UnitSettingRef,
	type UnitTablePreview,
	useDatasetUnitTablePreview,
} from "../../../../hooks/useDatasetSettings";
import {
	type DatasetSrcInfo,
	srcTypeOf,
} from "../../../../model/CommandOption";
import DatasetSettingsDialog from "./DatasetSettingsDialog";
import {
	DatasetTableNamesPreviewDialog,
	TablePreviewTable,
} from "./DatasetTableNamesPreviewButton";

type PreviewTarget = { srcInfo: DatasetSrcInfo; unitSetting: UnitSettingRef };

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
	const datasetSrcInfo = useDatasetSrcInfo();
	const encoding = useUnitSettingEncoding();
	const [target, setTarget] = useState<PreviewTarget | null>(null);
	if (!unitSetting || !canPreview(datasetSrcInfo)) {
		return null;
	}
	return (
		<>
			<BlueButton
				title="Preview Unit Settings"
				handleClick={() =>
					setTarget({
						srcInfo: datasetSrcInfo,
						unitSetting: { path: unitSetting, encoding },
					})
				}
			/>
			{target !== null && (
				<UnitSettingTableNamesDialog
					target={target}
					handleClose={() => setTarget(null)}
				/>
			)}
		</>
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
				<UnitSettingSaveAndPreviewButton saveThen={saveThen} />
			)}
		/>
	);
}

function UnitSettingSaveAndPreviewButton({ saveThen }: { saveThen: SaveThen }) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const encoding = useUnitSettingEncoding();
	const [target, setTarget] = useState<PreviewTarget | null>(null);
	if (!canPreview(datasetSrcInfo)) {
		return null;
	}
	return (
		<>
			<BlueButton
				title="Save and Preview"
				handleClick={() =>
					saveThen((savedPath) =>
						setTarget({
							srcInfo: datasetSrcInfo,
							unitSetting: { path: savedPath, encoding },
						}),
					)
				}
			/>
			{target !== null && (
				<UnitSettingTableNamesDialog
					target={target}
					handleClose={() => setTarget(null)}
				/>
			)}
		</>
	);
}

// srcのsettingを適用したテーブル一覧。各テーブルの横にunitSetting適用後の一覧を開くボタンを出す
function UnitSettingTableNamesDialog({
	target,
	handleClose,
}: {
	target: PreviewTarget;
	handleClose: () => void;
}) {
	return (
		<DatasetTableNamesPreviewDialog
			datasetSrcInfo={target.srcInfo}
			handleDialogClose={handleClose}
			rowActions={(tableName) => (
				<UnitTablesButton
					srcInfo={target.srcInfo}
					tableName={tableName}
					unitSetting={target.unitSetting}
				/>
			)}
		/>
	);
}

function UnitTablesButton({
	srcInfo,
	tableName,
	unitSetting,
}: {
	srcInfo: DatasetSrcInfo;
	tableName: string;
	unitSetting: UnitSettingRef;
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
					srcInfo={srcInfo}
					tableName={tableName}
					unitSetting={unitSetting}
					handleClose={() => setOpen(false)}
				/>
			)}
		</>
	);
}

function UnitTablesDialog({
	srcInfo,
	tableName,
	unitSetting,
	handleClose,
}: {
	srcInfo: DatasetSrcInfo;
	tableName: string;
	unitSetting: UnitSettingRef;
	handleClose: () => void;
}) {
	const { previews, loading, logs, error } = useDatasetUnitTablePreview(
		srcInfo,
		tableName,
		unitSetting,
	);
	const [selected, setSelected] = useState<UnitTablePreview | null>(null);

	function renderContent() {
		if (loading) {
			return (
				<JobProgress title="Loading..." lines={logs} onAbandon={handleClose} />
			);
		}
		if (error) {
			return <JobFailure message={error} lines={logs} />;
		}
		if (previews.length === 0) {
			return <StatusMessage kind="empty">No tables found.</StatusMessage>;
		}
		return (
			<ul className="text-sm bg-surface-subtle border border-border rounded-lg p-3 overflow-auto max-h-96 space-y-1">
				{previews.map((preview) => (
					<li
						key={preview.tableName}
						className="flex items-center gap-2 text-content-secondary"
					>
						<span className="flex-1">{preview.tableName}</span>
						<PreviewButton handleClick={() => setSelected(preview)} />
					</li>
				))}
			</ul>
		);
	}

	return (
		<FullDialog onClose={handleClose}>
			<DialogBody size="fluid">
				<DialogTitle>{tableName} — Unit Table List Preview</DialogTitle>
				{renderContent()}
			</DialogBody>
			<DialogFooter>
				<WhiteButton title="Close" handleClick={handleClose} />
			</DialogFooter>
			{selected !== null && (
				<FullDialog onClose={() => setSelected(null)}>
					<DialogBody size="fluid">
						<DialogTitle>{selected.tableName} — Data Preview</DialogTitle>
						<TablePreviewTable preview={selected} />
					</DialogBody>
					<DialogFooter>
						<WhiteButton title="Close" handleClick={() => setSelected(null)} />
					</DialogFooter>
				</FullDialog>
			)}
		</FullDialog>
	);
}
