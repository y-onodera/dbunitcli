import { useEffect, useRef, useState } from "react";
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
	const dialogRef = useRef<HTMLDialogElement>(null);

	useEffect(() => {
		dialogRef.current?.showModal();
	}, []);

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
		<dialog
			ref={dialogRef}
			onClose={handleDialogClose}
			className="overflow-y-auto fixed top-0 right-0 left-0 z-50 bg-surface border border-border-subtle"
		>
			<div className="p-4 rounded-lg mt-2">
				<h2 className="text-lg font-bold mb-2">Table List Preview</h2>
				{renderContent()}
			</div>
			<div className="flex items-center justify-end p-4 gap-2">
				<WhiteButton title="Close" handleClick={handleDialogClose} />
			</div>
			{previewTable !== null && (
				<TableDataPreviewDialog
					datasetSrcInfo={datasetSrcInfo}
					tableName={previewTable}
					handleClose={() => setPreviewTable(null)}
				/>
			)}
		</dialog>
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
	const dialogRef = useRef<HTMLDialogElement>(null);

	useEffect(() => {
		dialogRef.current?.showModal();
	}, []);

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
		<dialog
			ref={dialogRef}
			onClose={handleClose}
			className="overflow-y-auto fixed top-0 right-0 left-0 z-50 bg-surface border border-border-subtle"
		>
			<div className="p-4 rounded-lg mt-2">
				<h2 className="text-lg font-bold mb-2">{tableName} — Data Preview</h2>
				{renderContent()}
			</div>
			<div className="flex items-center justify-end p-4 gap-2">
				<WhiteButton title="Close" handleClick={handleClose} />
			</div>
		</dialog>
	);
}
