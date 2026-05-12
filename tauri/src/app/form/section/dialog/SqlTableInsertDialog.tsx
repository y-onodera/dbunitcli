import { useEffect, useRef, useState } from "react";
import {
	DialogActions,
	DialogTitle,
	ModalOverlay,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { FilterInput } from "../../../../components/element/Input";
import { useJdbcColumns, useJdbcTables } from "../../../../hooks/useJdbc";
import { useTableSelection } from "../../../../hooks/useTableSelection";
import TableList from "./TableList";

interface SqlTableInsertDialogProps {
	jdbcValues: Record<string, string>;
	onInsert: (tables: string[]) => void;
	onInsertColumn?: (column: string) => void;
	onClose: () => void;
}

export default function SqlTableInsertDialog({
	jdbcValues,
	onInsert,
	onInsertColumn,
	onClose,
}: SqlTableInsertDialogProps) {
	const [tables, setTables] = useState<string[] | null>(null);
	const [loading, setLoading] = useState(false);
	const getJdbcTables = useJdbcTables();
	const getJdbcColumns = useJdbcColumns();

	const handleLoad = () => {
		if (loading) {
			return;
		}
		setLoading(true);
		getJdbcTables(jdbcValues).then((result) => {
			setTables(result);
			setLoading(false);
		});
	};

	const queryColumns = (table: string): Promise<string[]> =>
		getJdbcColumns(jdbcValues, table);

	function renderBody() {
		if (loading) {
			return (
				<>
					<p className="text-sm text-content-muted mb-4">Loading...</p>
					<DialogActions>
						<WhiteButton title="Cancel" handleClick={onClose} />
					</DialogActions>
				</>
			);
		}
		if (tables !== null) {
			return (
				<TablesContent
					tables={tables}
					onInsert={onInsert}
					onInsertColumn={onInsertColumn}
					onQueryColumns={queryColumns}
					onClose={onClose}
				/>
			);
		}
		return (
			<DialogActions>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
		);
	}

	return (
		<ModalOverlay>
			<DialogTitle>Select Tables</DialogTitle>
			<div className="mb-4">
				<BlueButton title="Load Tables" handleClick={handleLoad} />
			</div>
			{renderBody()}
		</ModalOverlay>
	);
}

function TablesContent({
	tables,
	onInsert,
	onInsertColumn,
	onQueryColumns,
	onClose,
}: {
	tables: string[];
	onInsert: (tables: string[]) => void;
	onInsertColumn?: (column: string) => void;
	onQueryColumns?: (table: string) => Promise<string[]>;
	onClose: () => void;
}) {
	const { selected, toggle, toggleAll } = useTableSelection();
	const [columnDialog, setColumnDialog] = useState<{
		table: string;
		data: string[] | "loading";
	} | null>(null);
	const isMountedRef = useRef(true);
	useEffect(() => {
		return () => {
			isMountedRef.current = false;
		};
	}, []);

	const handleInsert = () => {
		const selectedTables = tables.filter((t) => selected.has(t));
		onInsert(selectedTables);
	};

	const handleSelectTable = (table: string) => {
		if (columnDialog?.table === table && columnDialog.data === "loading") {
			return;
		}
		setColumnDialog({ table, data: "loading" });
		onQueryColumns?.(table).then(
			(columns) => {
				if (isMountedRef.current) {
					setColumnDialog((prev) =>
						prev?.table === table ? { table, data: columns } : prev,
					);
				}
			},
			() => {
				if (isMountedRef.current) {
					setColumnDialog(null);
				}
			},
		);
	};

	return (
		<>
			<div className="mb-4 min-h-8">
				{tables.length === 0 ? (
					<p className="text-sm text-content-muted">No tables found</p>
				) : (
					<TableList
						tables={tables}
						selected={selected}
						onToggleAll={toggleAll}
						onToggle={toggle}
						maxHeightClass="max-h-72"
						onQueryColumns={onInsertColumn ? undefined : onQueryColumns}
						onSelectTable={onInsertColumn ? handleSelectTable : undefined}
					/>
				)}
			</div>
			<DialogActions>
				<BlueButton
					title="Insert"
					handleClick={handleInsert}
					disabled={selected.size === 0}
				/>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
			{columnDialog !== null && onInsertColumn && (
				<ColumnSelectDialog
					table={columnDialog.table}
					columnData={columnDialog.data}
					onInsert={(columns) => {
						for (const col of columns) {
							onInsertColumn(col);
						}
						setColumnDialog(null);
					}}
					onClose={() => setColumnDialog(null)}
				/>
			)}
		</>
	);
}

function ColumnSelectDialog({
	table,
	columnData,
	onInsert,
	onClose,
}: {
	table: string;
	columnData: string[] | "loading";
	onInsert: (columns: string[]) => void;
	onClose: () => void;
}) {
	const [filter, setFilter] = useState("");
	const { selected, toggle, toggleAll } = useTableSelection();

	if (columnData === "loading") {
		return (
			<ModalOverlay width="w-80" zClass="z-modal-nested">
				<p className="text-sm text-content-muted mb-4">Loading...</p>
				<DialogActions>
					<WhiteButton title="Cancel" handleClick={onClose} />
				</DialogActions>
			</ModalOverlay>
		);
	}

	const filterLower = filter.toLowerCase();
	const filteredColumns = filterLower
		? columnData.filter((c) => c.toLowerCase().includes(filterLower))
		: columnData;
	const allSelected =
		filteredColumns.length > 0 && filteredColumns.every((c) => selected.has(c));

	return (
		<ModalOverlay width="w-80" zClass="z-modal-nested">
			<DialogTitle>Select Columns</DialogTitle>
			<p className="text-caption mb-4 truncate" title={table}>
				{table}
			</p>
			<div className="mb-4">
				<FilterInput
					value={filter}
					onChange={setFilter}
					placeholder="Filter columns..."
				/>
				<div className="relative overflow-x-auto max-h-72 overflow-y-auto border border-border-subtle rounded">
					{columnData.length === 0 ? (
						<p className="text-sm text-content-muted px-3 py-2">
							No columns found
						</p>
					) : (
						<table className="w-full text-sm text-left">
							<thead className="bg-surface-subtle sticky top-0">
								<tr>
									<th className="px-3 py-2 w-8">
										<input
											type="checkbox"
											checked={allSelected}
											onChange={(e) =>
												toggleAll(filteredColumns, e.target.checked)
											}
											className="w-4 h-4 accent-primary-hover"
										/>
									</th>
									<th className="px-3 py-2 font-medium text-content-secondary">
										Column Name
									</th>
								</tr>
							</thead>
							<tbody>
								{filteredColumns.map((col) => (
									<tr
										key={col}
										className="hover:bg-surface-subtle cursor-pointer border-t border-border-faint"
										onClick={() => toggle(col)}
									>
										<td className="px-3 py-1.5">
											<input
												type="checkbox"
												checked={selected.has(col)}
												onChange={() => toggle(col)}
												onClick={(e) => e.stopPropagation()}
												className="w-4 h-4 accent-primary-hover"
											/>
										</td>
										<td className="px-3 py-1.5">{col}</td>
									</tr>
								))}
							</tbody>
						</table>
					)}
				</div>
			</div>
			<DialogActions>
				<BlueButton
					title="Insert Columns"
					handleClick={() =>
						onInsert(columnData.filter((c) => selected.has(c)))
					}
					disabled={selected.size === 0}
				/>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
		</ModalOverlay>
	);
}
