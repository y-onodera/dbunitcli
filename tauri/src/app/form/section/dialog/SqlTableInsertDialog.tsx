import { useEffect, useRef, useState } from "react";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
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
			return <p className="text-sm text-content-muted">Loading...</p>;
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
			<div className="flex gap-2 justify-end">
				<WhiteButton title="Cancel" handleClick={onClose} />
			</div>
		);
	}

	return (
		<div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
			<div className="bg-surface rounded-lg shadow-lg p-6 w-96 max-w-full">
				<h2 className="text-lg font-semibold mb-4">Select Tables</h2>
				<div className="mb-4">
					<BlueButton title="Load Tables" handleClick={handleLoad} />
				</div>
				{renderBody()}
			</div>
		</div>
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
			<div className="flex gap-2 justify-end">
				<BlueButton
					title="Insert"
					handleClick={handleInsert}
					disabled={selected.size === 0}
				/>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</div>
			{columnDialog !== null && onInsertColumn && (
				<ColumnSelectDialog
					table={columnDialog.table}
					columnData={columnDialog.data}
					onInsert={(columns) => {
						for (const col of columns) onInsertColumn(col);
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
			<div className="fixed inset-0 bg-black/50 flex items-center justify-center z-[60]">
				<div className="bg-surface rounded-lg shadow-lg p-6 w-80 max-w-full">
					<p className="text-sm text-content-muted">Loading...</p>
				</div>
			</div>
		);
	}

	const filterLower = filter.toLowerCase();
	const filteredColumns = filterLower
		? columnData.filter((c) => c.toLowerCase().includes(filterLower))
		: columnData;
	const allSelected =
		filteredColumns.length > 0 && filteredColumns.every((c) => selected.has(c));

	return (
		<div className="fixed inset-0 bg-black/50 flex items-center justify-center z-[60]">
			<div className="bg-surface rounded-lg shadow-lg p-6 w-80 max-w-full">
				<h2 className="text-lg font-semibold mb-1">Select Columns</h2>
				<p
					className="text-xs text-content-secondary mb-4 truncate"
					title={table}
				>
					{table}
				</p>
				<div className="mb-4">
					<input
						type="text"
						value={filter}
						onChange={(e) => setFilter(e.target.value)}
						placeholder="Filter columns..."
						className="w-full mb-1 px-2 py-1 text-sm border border-border rounded bg-input focus-visible:ring-3 ring-primary-ring"
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
				<div className="flex gap-2 justify-end">
					<BlueButton
						title="Insert Columns"
						handleClick={() =>
							onInsert(columnData.filter((c) => selected.has(c)))
						}
						disabled={selected.size === 0}
					/>
					<WhiteButton title="Cancel" handleClick={onClose} />
				</div>
			</div>
		</div>
	);
}
