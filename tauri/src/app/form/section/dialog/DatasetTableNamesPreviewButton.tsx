import { useState } from "react";
import {
	DataTable,
	DialogBody,
	DialogFooter,
	DialogTitle,
	FullDialog,
	StatusMessage,
	Td,
	Th,
	Tr,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { PreviewButton } from "../../../../components/element/ButtonIcon";
import { JobProgress } from "../../../../components/element/JobProgress";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import {
	useDatasetTableConvert,
	useDatasetTableNames,
	useDatasetTablePreview,
} from "../../../../hooks/useDatasetSettings";
import {
	type DatasetSrcInfo,
	withoutSetting,
	withValues,
} from "../../../../model/CommandOption";
import TableConvertDialog from "./TableConvertDialog";

export default function DatasetTableNamesPreviewButton({
	title,
	setting,
}: {
	title: string;
	setting: string;
}) {
	const [showDialog, setShowDialog] = useState(false);
	const datasetSrcInfo = useDatasetSrcInfo();
	return (
		<>
			<BlueButton title={title} handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<DatasetTableNamesPreviewDialog
					datasetSrcInfo={withPreviewSetting(datasetSrcInfo, setting)}
					handleDialogClose={() => setShowDialog(false)}
				/>
			)}
		</>
	);
}

// settingが空ならsettingを適用せず、指定があればそのファイルを適用して読む（それ以外のsrc要素はフォームのまま）
function withPreviewSetting(
	info: DatasetSrcInfo,
	setting: string,
): DatasetSrcInfo {
	return setting === "" ? withoutSetting(info) : withValues(info, { setting });
}

// SettingDialogのfooterActionsに置き、編集中の内容を保存できたらtoSrcInfo(保存先)で読み込まれるテーブル一覧をプレビューする（編集ダイアログは開いたまま）
export function SaveAndPreviewButton({
	saveThen,
	toSrcInfo,
}: {
	saveThen: (onSaved: (path: string) => void) => void;
	toSrcInfo: (savedPath: string) => DatasetSrcInfo;
}) {
	const [savedPath, setSavedPath] = useState<string | null>(null);
	return (
		<>
			<BlueButton
				title="Save and Preview"
				handleClick={() => saveThen(setSavedPath)}
			/>
			{savedPath !== null && (
				<DatasetTableNamesPreviewDialog
					datasetSrcInfo={toSrcInfo(savedPath)}
					handleDialogClose={() => setSavedPath(null)}
				/>
			)}
		</>
	);
}

export function DatasetTableNamesPreviewDialog({
	datasetSrcInfo,
	handleDialogClose,
}: {
	datasetSrcInfo: DatasetSrcInfo;
	handleDialogClose: () => void;
}) {
	const { tableNames, loading, logs } = useDatasetTableNames(
		datasetSrcInfo,
		true,
	);
	const [previewTable, setPreviewTable] = useState<string | null>(null);

	function renderContent() {
		if (loading) {
			return (
				<JobProgress
					title="Loading..."
					lines={logs}
					onAbandon={handleDialogClose}
				/>
			);
		}
		if (tableNames.length === 0) {
			return <StatusMessage kind="empty">No tables found.</StatusMessage>;
		}
		return (
			<ul className="text-sm bg-surface-subtle border border-border rounded-lg p-3 overflow-auto max-h-96 space-y-1">
				{tableNames.map((name) => (
					<li
						key={name}
						className="flex items-center gap-2 text-content-secondary"
					>
						<span className="flex-1">{name}</span>
						<PreviewButton handleClick={() => setPreviewTable(name)} />
					</li>
				))}
			</ul>
		);
	}

	return (
		<FullDialog onClose={handleDialogClose}>
			<DialogBody size="fluid">
				<DialogTitle>Table List Preview</DialogTitle>
				{renderContent()}
			</DialogBody>
			<DialogFooter>
				<WhiteButton title="Close" handleClick={handleDialogClose} />
			</DialogFooter>
			{previewTable !== null && (
				<TableDataPreviewDialog
					datasetSrcInfo={datasetSrcInfo}
					tableName={previewTable}
					handleClose={() => setPreviewTable(null)}
				/>
			)}
		</FullDialog>
	);
}

function TableDataPreviewDialog({
	datasetSrcInfo,
	tableName,
	handleClose,
}: {
	datasetSrcInfo: DatasetSrcInfo;
	tableName: string;
	handleClose: () => void;
}) {
	const { preview, loading, logs } = useDatasetTablePreview(
		datasetSrcInfo,
		tableName,
	);
	const convertTable = useDatasetTableConvert();
	const [showExport, setShowExport] = useState(false);

	function renderContent() {
		if (loading) {
			return (
				<JobProgress title="Loading..." lines={logs} onAbandon={handleClose} />
			);
		}
		if (!preview?.headers || preview.headers.length === 0) {
			return <StatusMessage kind="empty">No data found.</StatusMessage>;
		}
		const shownRows = preview.rows?.length ?? 0;
		const { totalRows } = preview;
		return (
			<>
				<p className="text-sm text-content-muted mb-2">
					{shownRows < totalRows
						? `Showing first ${shownRows} of ${totalRows} rows (partial preview)`
						: `${totalRows} rows`}
				</p>
				<DataTable
					head={preview.headers.map((header) => (
						<Th key={header} nowrap>
							{header}
						</Th>
					))}
				>
					{preview.rows?.map((row) => (
						<Tr key={row.join("\x00")}>
							{preview.headers.map((header, i) => (
								<Td key={header} nowrap>
									{row[i] ?? ""}
								</Td>
							))}
						</Tr>
					))}
				</DataTable>
			</>
		);
	}

	return (
		<FullDialog onClose={handleClose}>
			<DialogBody size="fluid">
				<DialogTitle>{tableName} — Data Preview</DialogTitle>
				{renderContent()}
			</DialogBody>
			<DialogFooter>
				<BlueButton
					title="Export"
					handleClick={() => setShowExport(true)}
					disabled={loading}
				/>
				<WhiteButton title="Close" handleClick={handleClose} />
			</DialogFooter>
			{showExport && (
				<TableConvertDialog
					tableName={tableName}
					convert={(result, fetchOptions) =>
						convertTable(datasetSrcInfo, tableName, result, fetchOptions)
					}
					onClose={() => setShowExport(false)}
				/>
			)}
		</FullDialog>
	);
}
