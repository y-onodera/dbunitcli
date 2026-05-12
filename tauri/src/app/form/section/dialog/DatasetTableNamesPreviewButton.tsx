import { useState } from "react";
import { DialogFooter, DialogTitle, FullDialog } from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { PreviewButton } from "../../../../components/element/ButtonIcon";
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
	const { tableNames, loading } = useDatasetTableNames(datasetSrcInfo);
	const [previewTable, setPreviewTable] = useState<string | null>(null);

	function renderContent() {
		if (loading) {
			return <p className="text-sm text-content-disabled p-3">Loading...</p>;
		}
		if (tableNames.length === 0) {
			return (
				<p className="text-sm text-content-disabled p-3">No tables found.</p>
			);
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
			<div className="p-4 rounded-lg mt-2">
				<DialogTitle>Table List Preview</DialogTitle>
				{renderContent()}
			</div>
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
	const { preview, loading } = useDatasetTablePreview(
		datasetSrcInfo,
		tableName,
	);

	function renderContent() {
		if (loading) {
			return <p className="text-sm text-content-disabled p-3">Loading...</p>;
		}
		if (!preview?.headers || preview.headers.length === 0) {
			return (
				<p className="text-sm text-content-disabled p-3">No data found.</p>
			);
		}
		return (
			<div className="overflow-x-auto max-h-64 border border-border rounded-lg">
				<table className="text-sm text-left w-full">
					<thead className="bg-surface-muted sticky top-0">
						<tr>
							{preview.headers.map((header) => (
								<th
									key={header}
									className="px-3 py-2 font-medium text-content-secondary whitespace-nowrap border-b border-border"
								>
									{header}
								</th>
							))}
						</tr>
					</thead>
					<tbody>
						{preview.rows?.map((row) => (
							<tr
								key={row.join("\x00")}
								className="border-t border-border-faint hover:bg-surface-subtle"
							>
								{preview.headers.map((header, i) => (
									<td
										key={header}
										className="px-3 py-1.5 text-content-muted whitespace-nowrap"
									>
										{row[i] ?? ""}
									</td>
								))}
							</tr>
						))}
					</tbody>
				</table>
			</div>
		);
	}

	return (
		<FullDialog onClose={handleClose}>
			<div className="p-4 rounded-lg mt-2">
				<DialogTitle>{tableName} — Data Preview</DialogTitle>
				{renderContent()}
			</div>
			<DialogFooter>
				<WhiteButton title="Close" handleClick={handleClose} />
			</DialogFooter>
		</FullDialog>
	);
}
