import { useState } from "react";
import {
	DataTable,
	DialogActions,
	DialogField,
	DialogTitle,
	JobFailure,
	ModalOverlay,
	StatusMessage,
	Td,
	Th,
	Tr,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { BlueEditButton } from "../../../../components/element/ButtonIcon";
import { FilterInput } from "../../../../components/element/Input";
import { JobProgress } from "../../../../components/element/JobProgress";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useDatasetTableSources } from "../../../../hooks/useDatasetSettings";
import {
	type DatasetSrcInfo,
	withValues,
} from "../../../../model/CommandOption";
import {
	buildFilterRegex,
	matchedKeys,
	type RegexFilterKind,
	type RegexFilterRow,
	toFilterRows,
} from "../../../../model/RegexFilter";

// 正規表現を当てる値の列名
const KEY_COLUMN_LABEL: Record<RegexFilterKind, string> = {
	path: "file",
	table: "source table",
};

type RegexFilterProps = {
	kind: RegexFilterKind;
	// 編集する項目のsrc要素名（regInclude等）。一覧はこの項目を空にして読む
	fieldName: string;
	value: string;
	onApply: (regex: string) => void;
};

// 正規表現の項目の横に置き、読み込まれるテーブルの一覧から選んで正規表現を組み立てるダイアログを開く
export function RegexFilterButton(props: RegexFilterProps) {
	const [open, setOpen] = useState(false);
	return (
		<>
			<BlueEditButton
				title={`select ${props.fieldName} from table list`}
				handleClick={() => setOpen(true)}
			/>
			{open && (
				<RegexFilterDialog
					{...props}
					onApply={(regex) => {
						props.onApply(regex);
						setOpen(false);
					}}
					onClose={() => setOpen(false)}
				/>
			)}
		</>
	);
}

function withoutField(info: DatasetSrcInfo, fieldName: string): DatasetSrcInfo {
	return withValues(info, { [fieldName]: "" });
}

function toggled(selected: Set<string>, key: string): Set<string> {
	const next = new Set(selected);
	if (next.has(key)) {
		next.delete(key);
	} else {
		next.add(key);
	}
	return next;
}

function toggledAll(
	selected: Set<string>,
	keys: string[],
	checked: boolean,
): Set<string> {
	const next = new Set(selected);
	for (const key of keys) {
		if (checked) {
			next.add(key);
		} else {
			next.delete(key);
		}
	}
	return next;
}

function matchesFilter(row: RegexFilterRow, filter: string): boolean {
	return [row.key, ...row.tableNames].some((it) =>
		it.toLowerCase().includes(filter),
	);
}

function RegexFilterDialog({
	kind,
	fieldName,
	value,
	onApply,
	onClose,
}: RegexFilterProps & { onClose: () => void }) {
	const srcInfo = useDatasetSrcInfo();
	// 開いた時点のフォームで1回だけ読む（毎回作り直すと読込が繰り返される）
	const [loadInfo] = useState(() => withoutField(srcInfo, fieldName));
	const { sources, loading, logs, error } = useDatasetTableSources(
		loadInfo,
		true,
	);
	const [edited, setEdited] = useState<Set<string> | null>(null);
	const [filter, setFilter] = useState("");
	const rows = toFilterRows(kind, sources);
	const keys = rows.map((row) => row.key);
	// 選び直すまでは、入力中の正規表現に一致する行を選んだ状態にする
	const selected =
		edited ??
		new Set(matchedKeys(kind, value, keys, loadInfo.values.src ?? ""));
	const regex = buildFilterRegex(
		kind,
		keys.filter((key) => selected.has(key)),
	);
	const filterLower = filter.toLowerCase();
	const visibleRows = rows.filter((row) => matchesFilter(row, filterLower));
	const visibleKeys = visibleRows.map((row) => row.key);
	const allSelected =
		visibleKeys.length > 0 && visibleKeys.every((key) => selected.has(key));

	const renderRows = () => {
		if (loading) {
			return <JobProgress title="Loading..." lines={logs} />;
		}
		if (error) {
			return <JobFailure message={error} lines={logs} />;
		}
		if (rows.length === 0) {
			return <StatusMessage kind="empty">No tables loaded.</StatusMessage>;
		}
		return (
			<>
				<FilterInput value={filter} onChange={setFilter} placeholder="Filter" />
				<DataTable
					head={
						<>
							<Th narrow>
								<input
									type="checkbox"
									aria-label="Select all"
									checked={allSelected}
									onChange={(e) =>
										setEdited(
											toggledAll(selected, visibleKeys, e.target.checked),
										)
									}
									className="w-4 h-4 text-primary"
								/>
							</Th>
							<Th>{KEY_COLUMN_LABEL[kind]}</Th>
							<Th>table</Th>
						</>
					}
				>
					{visibleRows.map((row) => (
						<Tr
							key={row.key}
							onClick={() => setEdited(toggled(selected, row.key))}
						>
							<Td>
								<input
									type="checkbox"
									aria-label={row.key}
									checked={selected.has(row.key)}
									onChange={() => setEdited(toggled(selected, row.key))}
									onClick={(e) => e.stopPropagation()}
									className="w-4 h-4 text-primary"
								/>
							</Td>
							<Td>{row.key}</Td>
							<Td>{row.tableNames.join(", ")}</Td>
						</Tr>
					))}
				</DataTable>
			</>
		);
	};

	return (
		<ModalOverlay onClose={onClose} size="lg">
			<DialogTitle
				command="dialogs/regex-filter"
				label="Select from Table List"
			>
				Select from Table List
			</DialogTitle>
			<DialogField>{renderRows()}</DialogField>
			<DialogField last>
				<StatusMessage kind="loading">{`${fieldName}: ${regex}`}</StatusMessage>
			</DialogField>
			<DialogActions>
				<BlueButton
					title="Apply"
					disabled={loading}
					handleClick={() => onApply(regex)}
				/>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
		</ModalOverlay>
	);
}
