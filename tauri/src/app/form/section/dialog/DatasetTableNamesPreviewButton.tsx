import { type ReactNode, useState } from "react";
import {
	DataTable,
	DialogBody,
	DialogFooter,
	DialogTitle,
	FullDialog,
	JobFailure,
	type SaveThen,
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
	type TablePreview,
	useDatasetTableConvert,
	useDatasetTableNames,
	useDatasetTablePreview,
} from "../../../../hooks/useDatasetSettings";
import {
	type DatasetSrcInfo,
	srcTypeOf,
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

// SettingDialogのfooterActionsに置き、編集中の内容を保存できたらtoSrcInfo(保存先)で読み込まれるテーブル一覧をプレビューする（編集ダイアログは開いたまま）。
// saveThenが保存せずにファイルを渡すだけなら、指定済みのファイルでプレビューするボタンにもなる。
// rowActions: 各テーブル名の横に足すボタン（保存先を受け取る）
export function SaveAndPreviewButton({
	title = "Save and Preview",
	saveThen,
	toSrcInfo,
	rowActions,
}: {
	title?: string;
	saveThen: SaveThen;
	toSrcInfo: (savedPath: string) => DatasetSrcInfo;
	rowActions?: (savedPath: string, tableName: string) => ReactNode;
}) {
	const [savedPath, setSavedPath] = useState<string | null>(null);
	return (
		<>
			<BlueButton title={title} handleClick={() => saveThen(setSavedPath)} />
			{savedPath !== null && (
				<DatasetTableNamesPreviewDialog
					datasetSrcInfo={toSrcInfo(savedPath)}
					handleDialogClose={() => setSavedPath(null)}
					rowActions={
						rowActions && ((tableName) => rowActions(savedPath, tableName))
					}
				/>
			)}
		</>
	);
}

// SettingDialogのfooterActionsに置き、編集中の未保存の設定（inline）だけをsettingなしで適用して読み込まれるテーブル一覧をプレビューする（保存はしない）。
// singleTable: 読み込まれるテーブルが1つと決まっていれば、一覧を出さずにそのテーブルのデータをプレビューする
export function InlinePreviewButton({
	toInline,
	singleTable,
}: {
	toInline: () => Omit<DatasetSrcInfo, "values">;
	singleTable?: string;
}) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const [previewSrcInfo, setPreviewSrcInfo] = useState<DatasetSrcInfo | null>(
		null,
	);
	if (!srcTypeOf(datasetSrcInfo)) {
		return null;
	}
	function renderPreview(srcInfo: DatasetSrcInfo) {
		const handleClose = () => setPreviewSrcInfo(null);
		if (singleTable !== undefined) {
			return (
				<TableDataPreviewDialog
					datasetSrcInfo={srcInfo}
					tableName={singleTable}
					handleClose={handleClose}
				/>
			);
		}
		return (
			<DatasetTableNamesPreviewDialog
				datasetSrcInfo={srcInfo}
				handleDialogClose={handleClose}
			/>
		);
	}
	return (
		<>
			<BlueButton
				title="Preview"
				handleClick={() =>
					setPreviewSrcInfo({
						...withoutSetting(datasetSrcInfo),
						...toInline(),
					})
				}
			/>
			{previewSrcInfo !== null && renderPreview(previewSrcInfo)}
		</>
	);
}

// rowActions: 各テーブル名の横にプレビューボタンに加えて出すボタン（generateの-unitSettingのプレビューなど）
export function DatasetTableNamesPreviewDialog({
	datasetSrcInfo,
	handleDialogClose,
	rowActions,
}: {
	datasetSrcInfo: DatasetSrcInfo;
	handleDialogClose: () => void;
	rowActions?: (tableName: string) => ReactNode;
}) {
	const { tableNames, loading, logs, error } = useDatasetTableNames(
		datasetSrcInfo,
		true,
	);
	const [previewTable, setPreviewTable] = useState<string | null>(null);

	return (
		<FullDialog onClose={handleDialogClose}>
			<DialogBody size="fluid">
				<DialogTitle>Table List Preview</DialogTitle>
				<TableNameList
					names={tableNames}
					loading={loading}
					logs={logs}
					error={error}
					onAbandon={handleDialogClose}
					onPreview={setPreviewTable}
					rowActions={rowActions}
				/>
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
	const { preview, loading, logs, error } = useDatasetTablePreview(
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
		if (error) {
			return <JobFailure message={error} lines={logs} />;
		}
		return <TablePreviewTable preview={preview} />;
	}

	return (
		<DataPreviewDialog
			tableName={tableName}
			handleClose={handleClose}
			body={renderContent()}
			footerActions={
				<BlueButton
					title="Export"
					handleClick={() => setShowExport(true)}
					disabled={loading || error !== ""}
				/>
			}
		>
			{showExport && (
				<TableConvertDialog
					tableName={tableName}
					convert={(result, fetchOptions) =>
						convertTable(datasetSrcInfo, tableName, result, fetchOptions)
					}
					onClose={() => setShowExport(false)}
				/>
			)}
		</DataPreviewDialog>
	);
}

// 「{tableName} — Data Preview」ダイアログの外枠。footerActionsはCloseの前に並べ、childrenは上に重ねるダイアログ
export function DataPreviewDialog({
	tableName,
	handleClose,
	body,
	footerActions,
	children,
}: {
	tableName: string;
	handleClose: () => void;
	body: ReactNode;
	footerActions?: ReactNode;
	children?: ReactNode;
}) {
	return (
		<FullDialog onClose={handleClose}>
			<DialogBody size="fluid">
				<DialogTitle>{tableName} — Data Preview</DialogTitle>
				{body}
			</DialogBody>
			<DialogFooter>
				{footerActions}
				<WhiteButton title="Close" handleClick={handleClose} />
			</DialogFooter>
			{children}
		</FullDialog>
	);
}

// 先頭の一部の行と全行数の表示（rowsはsidecarが件数を制限した先頭の一部）
export function TablePreviewTable({
	preview,
}: {
	preview: TablePreview | null;
}) {
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

// テーブル名の一覧（読込中・失敗・空の表示を含む）。各名前の横にプレビューボタンとrowActionsを出す
export function TableNameList({
	names,
	loading,
	logs,
	error,
	onAbandon,
	onPreview,
	rowActions,
}: {
	names: string[];
	loading: boolean;
	logs: string[];
	error: string;
	onAbandon: () => void;
	onPreview: (name: string) => void;
	rowActions?: (name: string) => ReactNode;
}) {
	if (loading) {
		return (
			<JobProgress title="Loading..." lines={logs} onAbandon={onAbandon} />
		);
	}
	if (error) {
		return <JobFailure message={error} lines={logs} />;
	}
	if (names.length === 0) {
		return <StatusMessage kind="empty">No tables found.</StatusMessage>;
	}
	return (
		<ul className="text-sm bg-surface-subtle border border-border rounded-lg p-3 overflow-auto max-h-96 space-y-1">
			{names.map((name) => (
				<li
					key={name}
					className="flex items-center gap-2 text-content-secondary"
				>
					<span className="flex-1">{name}</span>
					<PreviewButton handleClick={() => onPreview(name)} />
					{rowActions?.(name)}
				</li>
			))}
		</ul>
	);
}
