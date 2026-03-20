import { useEffect, useRef, useState } from "react";
import { BlueButton, WhiteButton } from "../../components/element/Button";
import { useDatasetTableNames } from "../../hooks/useDatasetSettings";
import type { DatasetSrcInfo } from "../../model/CommandParam";

export default function DatasetTableNamesPreviewButton({
	title,
	datasetSrcInfo,
}: {
	title: string;
	datasetSrcInfo: DatasetSrcInfo;
}) {
	const [showDialog, setShowDialog] = useState(false);
	return (
		<>
			<BlueButton title={title} handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<DatasetTableNamesPreviewDialog
					datasetSrcInfo={datasetSrcInfo}
					handleDialogClose={() => setShowDialog(false)}
				/>
			)}
		</>
	);
}

function TableContent({
	tableNames,
	loading,
}: {
	tableNames: string[];
	loading: boolean;
}) {
	if (loading) {
		return <p className="text-sm text-gray-400 p-3">Loading...</p>;
	}
	if (tableNames.length === 0) {
		return <p className="text-sm text-gray-400 p-3">No tables found.</p>;
	}
	return (
		<ul className="text-sm bg-gray-50 border border-gray-300 rounded-lg p-3 overflow-auto max-h-96 space-y-1">
			{tableNames.map((name) => (
				<li key={name} className="text-gray-700">
					{name}
				</li>
			))}
		</ul>
	);
}

function DatasetTableNamesPreviewDialog({
	datasetSrcInfo,
	handleDialogClose,
}: {
	datasetSrcInfo: DatasetSrcInfo;
	handleDialogClose: () => void;
}) {
	const dialogRef = useRef<HTMLDialogElement>(null);
	const { tableNames, loading } = useDatasetTableNames(datasetSrcInfo);
	useEffect(() => {
		dialogRef.current?.showModal();
	}, []);

	return (
		<dialog
			ref={dialogRef}
			onClose={handleDialogClose}
			className="overflow-y-auto fixed top-0 right-0 left-0 z-50 bg-white border border-gray-200"
		>
			<div className="p-4 rounded-lg mt-2">
				<h2 className="text-lg font-bold mb-2">Table List Preview</h2>
				<TableContent tableNames={tableNames} loading={loading} />
			</div>
			<div className="flex items-center justify-end p-4 gap-2">
				<WhiteButton title="Close" handleClick={handleDialogClose} />
			</div>
		</dialog>
	);
}
