import { Suspense, use, useEffect, useRef, useState } from "react";
import { BlueButton, WhiteButton } from "../../components/element/Button";
import { useJdbcConnectionState } from "../../context/JdbcConnectionProvider";
import { useDatasetSrcInfo } from "../../context/DatasetSrcInfoProvider";
import { useDatasetTableNamesApi } from "../../hooks/useDatasetSettings";
import type { DatasetSrcInfo } from "../../model/CommandOption";

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
	const { jdbcValues, connectionOk } = useJdbcConnectionState();
	const loadTableNames = useDatasetTableNamesApi();

	const srcPath = datasetSrcInfo.srcPath;
	const srcType = datasetSrcInfo.srcType;
	const sqlNotReady = srcType === "sql" && !connectionOk;

	const promise =
		!srcPath || !srcType || srcType === "none" || sqlNotReady
			? Promise.resolve<string[]>([])
			: loadTableNames(datasetSrcInfo, jdbcValues);

	return (
		<Suspense fallback={<div>Loading...</div>}>
			<TableNamesContent promise={promise} handleDialogClose={handleDialogClose} />
		</Suspense>
	);
}

function TableNamesContent({
	promise,
	handleDialogClose,
}: {
	promise: Promise<string[]>;
	handleDialogClose: () => void;
}) {
	const dialogRef = useRef<HTMLDialogElement>(null);
	const tableNames = use(promise);

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
