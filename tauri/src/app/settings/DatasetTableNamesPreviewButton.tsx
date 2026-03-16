import { useEffect, useRef, useState } from "react";
import { WhiteButton } from "../../components/element/Button";
import { PreviewButton } from "../../components/element/ButtonIcon";
import { useDatasetTableNames } from "../../hooks/useDatasetSettings";
import type { DatasetSrcInfo } from "../../model/CommandParam";

export default function DatasetTableNamesPreviewButton({
	datasetSrcInfo,
}: {
	datasetSrcInfo: DatasetSrcInfo;
}) {
	const [showDialog, setShowDialog] = useState(false);
	return (
		<>
			<PreviewButton handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<DatasetTableNamesPreviewDialog
					datasetSrcInfo={datasetSrcInfo}
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
	const dialogRef = useRef<HTMLDialogElement>(null);
	const tableNames = useDatasetTableNames(datasetSrcInfo);
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
				{tableNames.length === 0 ? (
					<p className="text-sm text-gray-400 p-3">No tables found.</p>
				) : (
					<ul className="text-sm bg-gray-50 border border-gray-300 rounded-lg p-3 overflow-auto max-h-96 space-y-1">
						{tableNames.map((name) => (
							<li key={name} className="text-gray-700">
								{name}
							</li>
						))}
					</ul>
				)}
			</div>
			<div className="flex items-center justify-end p-4 gap-2">
				<WhiteButton title="Close" handleClick={handleDialogClose} />
			</div>
		</dialog>
	);
}
