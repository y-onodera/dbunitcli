import { useState } from "react";
import {
	DialogBody,
	DialogField,
	JobFailure,
	SettingDialog,
	StatusMessage,
} from "../../../../components/dialog";
import { BlueEditButton } from "../../../../components/element/ButtonIcon";
import { JobProgress } from "../../../../components/element/JobProgress";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useDatasetTableColumns } from "../../../../hooks/useDatasetSettings";
import {
	parseCompareColumns,
	selectedOldColumns,
	toggleCompareColumn,
	toggleCompareColumns,
} from "../../../../model/CompareColumns";
import SelectableTable from "./SelectableTable";

// oldTableの列からkeys / comparesを選ぶ。"old:new"と書いた対応は、oldの列を選んでいる間は残す
function CompareColumnsDialog({
	caption,
	oldTable,
	currentValue,
	handleDialogClose,
	handleSave,
}: {
	caption: string;
	oldTable: string;
	currentValue: string;
	handleDialogClose: () => void;
	handleSave: (value: string) => void;
}) {
	const srcInfo = useDatasetSrcInfo();
	const { columns, loading, logs, error } = useDatasetTableColumns(
		srcInfo,
		oldTable,
	);
	const [entries, setEntries] = useState(() =>
		parseCompareColumns(currentValue),
	);

	const renderColumns = () => {
		if (oldTable === "") {
			return <StatusMessage kind="empty">Enter oldTable.</StatusMessage>;
		}
		if (loading) {
			return <JobProgress title="Loading..." lines={logs} />;
		}
		if (error) {
			return <JobFailure message={error} lines={logs} />;
		}
		return (
			<SelectableTable
				items={columns}
				selected={selectedOldColumns(entries)}
				onToggle={(column) => setEntries(toggleCompareColumn(entries, column))}
				onToggleAll={(items, checked) =>
					setEntries(toggleCompareColumns(entries, items, checked))
				}
				columnLabel={`${oldTable} column`}
				filterPlaceholder="Filter columns"
				emptyMessage="No columns loaded."
			/>
		);
	};

	return (
		<SettingDialog
			title={`Select ${caption}`}
			helpCommand="dialogs/compare-columns"
			helpLabel="Select Compare Columns"
			setting={entries}
			handleDialogClose={handleDialogClose}
			handleCommit={(selected) => handleSave(selected.join(","))}
			commitLabel="Apply"
		>
			<DialogBody size="sm">
				<DialogField>{renderColumns()}</DialogField>
				<DialogField last>
					<StatusMessage kind="loading">
						{entries.length > 0
							? `${caption}: ${entries.join(",")}`
							: `No ${caption} selected.`}
					</StatusMessage>
				</DialogField>
			</DialogBody>
		</SettingDialog>
	);
}

export function CompareColumnsButton({
	caption,
	oldTable,
	value,
	setValue,
}: {
	caption: string;
	oldTable: string;
	value: string;
	setValue: (value: string) => void;
}) {
	const [showDialog, setShowDialog] = useState(false);
	return (
		<>
			<BlueEditButton
				title={`select ${caption}`}
				handleClick={() => setShowDialog(true)}
			/>
			{showDialog && (
				<CompareColumnsDialog
					caption={caption}
					oldTable={oldTable}
					currentValue={value}
					handleDialogClose={() => setShowDialog(false)}
					handleSave={(newValue) => {
						setValue(newValue);
						setShowDialog(false);
					}}
				/>
			)}
		</>
	);
}
