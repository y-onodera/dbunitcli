import { useState } from "react";
import {
	DialogBody,
	DialogField,
	JobFailure,
	SettingDialog,
	StatusMessage,
} from "../../../../components/dialog";
import { BlueEditButton } from "../../../../components/element/ButtonIcon";
import { SelectBox } from "../../../../components/element/Input";
import { JobProgress } from "../../../../components/element/JobProgress";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useDatasetTableColumns } from "../../../../hooks/useDatasetSettings";
import {
	changeNewColumn,
	newColumnOf,
	oldColumnOf,
	selectedOldColumns,
	toggleCompareColumns,
} from "../../../../model/CompareColumns";
import { splitComma } from "../../../../utils/arrayUtils";
import SelectableTable, { type ExtraColumn } from "./SelectableTable";

// oldTableの列からkeys / comparesを選ぶ。選んだ列ごとにnewTableの列を対応させる
function CompareColumnsDialog({
	caption,
	oldTable,
	newTable,
	currentValue,
	handleDialogClose,
	handleSave,
}: {
	caption: string;
	oldTable: string;
	newTable: string;
	currentValue: string;
	handleDialogClose: () => void;
	handleSave: (value: string) => void;
}) {
	const srcInfo = useDatasetSrcInfo();
	const { columns, loading, logs, error } = useDatasetTableColumns(
		srcInfo,
		oldTable,
	);
	// oldと同じテーブルは読み直さない。oldが未入力なら一覧を出さないので読まない
	const readsNew = oldTable !== "" && newTable !== oldTable;
	const {
		columns: newTableColumns,
		loading: newLoading,
		logs: newLogs,
	} = useDatasetTableColumns(srcInfo, readsNew ? newTable : "");
	const newColumns = readsNew ? newTableColumns : columns;
	const [entries, setEntries] = useState(() => splitComma(currentValue));
	const selected = selectedOldColumns(entries);
	const newColumnByOld = new Map(
		entries.map((entry) => [oldColumnOf(entry), newColumnOf(entry)]),
	);

	const renderNewColumn = (column: string) => {
		const newColumn = newColumnByOld.get(column);
		if (newColumn === undefined) {
			return null;
		}
		const found = newColumns.includes(newColumn);
		return (
			<SelectBox
				name={`new column of ${column}`}
				id={`newColumn_${column}`}
				required={false}
				wStyle="w-full"
				defaultValue={found ? newColumn : ""}
				handleOnChange={(value) =>
					setEntries(changeNewColumn(entries, column, value))
				}
			>
				{!found && <option value="">(not in {newTable})</option>}
				{newColumns.map((option) => (
					<option key={option} value={option}>
						{option}
					</option>
				))}
			</SelectBox>
		);
	};
	// newTableの列が読めないときは、old側の列だけを選ぶ
	const extraColumns: ExtraColumn[] =
		newColumns.length > 0
			? [
					{
						label: `${newTable} column`,
						value: (column) => newColumnByOld.get(column) ?? "",
						render: renderNewColumn,
					},
				]
			: [];

	const renderColumns = () => {
		if (oldTable === "") {
			return <StatusMessage kind="empty">Enter oldTable.</StatusMessage>;
		}
		if (loading || newLoading) {
			return (
				<JobProgress title="Loading..." lines={loading ? logs : newLogs} />
			);
		}
		if (error) {
			return <JobFailure message={error} lines={logs} />;
		}
		return (
			<SelectableTable
				items={columns}
				selected={selected}
				onToggle={(column) =>
					setEntries(
						toggleCompareColumns(
							entries,
							[column],
							!selected.has(column),
							newColumns,
						),
					)
				}
				onToggleAll={(items, checked) =>
					setEntries(toggleCompareColumns(entries, items, checked, newColumns))
				}
				columnLabel={`${oldTable} column`}
				filterPlaceholder="Filter columns"
				emptyMessage="No columns loaded."
				extraColumns={extraColumns}
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
	newTable,
	value,
	setValue,
}: {
	caption: string;
	oldTable: string;
	newTable: string;
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
					newTable={newTable}
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
