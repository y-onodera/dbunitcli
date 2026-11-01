import { useState } from "react";
import {
	DialogBody,
	DialogFooter,
	DialogTitle,
	FullDialog,
	JobFailure,
	StatusMessage,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import {
	IconButton,
	PreviewButton,
} from "../../../../components/element/ButtonIcon";
import { JobProgress } from "../../../../components/element/JobProgress";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import {
	type UnitSettingRef,
	type UnitTablePreview,
	useDatasetUnitTablePreview,
} from "../../../../hooks/useDatasetSettings";
import {
	type DatasetSrcInfo,
	srcTypeOf,
} from "../../../../model/CommandOption";
import type { FormValues } from "../../../../utils/formValues";
import {
	DatasetTableNamesPreviewDialog,
	TablePreviewTable,
} from "./DatasetTableNamesPreviewButton";

type PreviewTarget = { srcInfo: DatasetSrcInfo; unitSetting: UnitSettingRef };

// generateの-unitSettingのプレビュー。srcのsettingを適用したテーブル一覧を出し、各テーブルの横のボタンで
// そのテーブルにunitSettingを適用した後のテーブル（rowsになるテーブル自身とsubTable）を確認する。
// srcとunitSettingEncodingは押した時点のフォームの値（formValues）を使う
export default function UnitSettingPreviewButton({
	unitSetting,
	formValues,
}: {
	unitSetting: string;
	formValues: () => FormValues;
}) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const [target, setTarget] = useState<PreviewTarget | null>(null);
	if (!unitSetting || !srcTypeOf(datasetSrcInfo)) {
		return null;
	}
	return (
		<>
			<BlueButton
				title="Preview Unit Settings"
				handleClick={() =>
					setTarget(toPreviewTarget(formValues(), unitSetting))
				}
			/>
			{target !== null && (
				<DatasetTableNamesPreviewDialog
					datasetSrcInfo={target.srcInfo}
					handleDialogClose={() => setTarget(null)}
					rowActions={(tableName) => (
						<UnitTablesButton
							srcInfo={target.srcInfo}
							tableName={tableName}
							unitSetting={target.unitSetting}
						/>
					)}
				/>
			)}
		</>
	);
}

// フォームの値（-src.<オプション名>・-unitSettingEncoding）からプレビューの読込条件を作る
function toPreviewTarget(
	values: FormValues,
	unitSetting: string,
): PreviewTarget {
	const encoding = values["-unitSettingEncoding"];
	const srcValues: Record<string, string> = {};
	for (const [key, value] of Object.entries(values)) {
		if (key.startsWith("-src.") && typeof value === "string") {
			srcValues[key.substring("-src.".length)] = value;
		}
	}
	return {
		srcInfo: { values: srcValues },
		unitSetting: {
			path: unitSetting,
			encoding: typeof encoding === "string" ? encoding : "",
		},
	};
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
