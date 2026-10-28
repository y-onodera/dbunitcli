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
	useDatasetTableNames,
	useDatasetTablePreview,
} from "../../../../hooks/useDatasetSettings";
import type { DatasetSrcInfo } from "../../../../model/CommandOption";

export default function DatasetTableNamesPreviewButton({
	title,
	setting,
}: {
	title: string;
	setting?: string;
}) {
	const [showDialog, setShowDialog] = useState(false);
	const datasetSrcInfo = useDatasetSrcInfo();
	return (
		<>
			<BlueButton title={title} handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<DatasetTableNamesPreviewDialog
					datasetSrcInfo={
						setting !== undefined
							? { ...datasetSrcInfo, setting }
							: datasetSrcInfo
					}
					handleDialogClose={() => setShowDialog(false)}
				/>
			)}
		</>
	);
}

function DatasetTableNamesPreviewDialog({
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
				<WhiteButton title="Close" handleClick={handleClose} />
			</DialogFooter>
		</FullDialog>
	);
}
