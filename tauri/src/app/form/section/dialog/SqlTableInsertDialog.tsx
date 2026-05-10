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
			<div className={`bg-surface rounded-lg shadow-lg p-6 max-w-full ${onInsertColumn ? "w-[660px]" : "w-96"}`}>
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
	const {
		selected: selectedColumns,
		toggle: toggleColumn,
		toggleAll: toggleAllColumns,
	} = useTableSelection();
	const [columnTarget, setColumnTarget] = useState<string | null>(null);
	const [columnData, setColumnData] = useState<string[] | "loading" | null>(
		null,
	);
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
		if (table === columnTarget) {
			setColumnTarget(null);
			setColumnData(null);
			return;
		}
		setColumnTarget(table);
		setColumnData("loading");
		toggleAllColumns(Array.from(selectedColumns), false);
		onQueryColumns?.(table).then(
			(columns) => {
				if (isMountedRef.current) {
					setColumnData(columns);
				}
			},
			() => {
				if (isMountedRef.current) {
					setColumnData(null);
					setColumnTarget(null);
				}
			},
		);
	};

	const handleInsertColumns = () => {
		if (!Array.isArray(columnData)) return;
		for (const col of columnData.filter((c) => selectedColumns.has(c))) {
			onInsertColumn?.(col);
		}
	};

	const tableList =
		tables.length === 0 ? (
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
				activeTable={columnTarget ?? undefined}
			/>
		);

	return (
		<>
			<div className="mb-4 min-h-8 flex gap-4">
				<div className={columnTarget !== null ? "flex-1 min-w-0" : "w-full"}>
					{tableList}
				</div>
				{columnTarget !== null && onInsertColumn && (
					<ColumnPanel
						table={columnTarget}
						columnData={columnData}
						selectedColumns={selectedColumns}
						onToggleColumn={toggleColumn}
						onToggleAllColumns={toggleAllColumns}
						onInsertColumns={handleInsertColumns}
						insertDisabled={selectedColumns.size === 0}
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
		</>
	);
}

function ColumnPanel({
	table,
	columnData,
	selectedColumns,
	onToggleColumn,
	onToggleAllColumns,
	onInsertColumns,
	insertDisabled,
}: {
	table: string;
	columnData: string[] | "loading" | null;
	selectedColumns: Set<string>;
	onToggleColumn: (col: string) => void;
	onToggleAllColumns: (cols: string[], checked: boolean) => void;
	onInsertColumns: () => void;
	insertDisabled: boolean;
}) {
	if (columnData === "loading") {
		return (
			<div className="w-48 flex items-center justify-center">
				<p className="text-sm text-content-muted">Loading...</p>
			</div>
		);
	}

	const columns = Array.isArray(columnData) ? columnData : [];
	const allSelected =
		columns.length > 0 && columns.every((c) => selectedColumns.has(c));

	return (
		<div className="w-48 flex flex-col gap-2">
			<p
				className="text-xs font-medium text-content-secondary truncate"
				title={table}
			>
				{table}
			</p>
			<div className="overflow-y-auto max-h-64 border border-border-subtle rounded">
				{columns.length === 0 ? (
					<p className="text-sm text-content-muted px-3 py-2">No columns</p>
				) : (
					<table className="w-full text-sm text-left">
						<thead className="bg-surface-subtle sticky top-0">
							<tr>
								<th className="px-2 py-1 w-6">
									<input
										type="checkbox"
										checked={allSelected}
										onChange={(e) => onToggleAllColumns(columns, e.target.checked)}
										className="w-4 h-4 accent-primary-hover"
									/>
								</th>
								<th className="px-2 py-1 font-medium text-content-secondary text-xs">
									Column
								</th>
							</tr>
						</thead>
						<tbody>
							{columns.map((col) => (
								<tr
									key={col}
									className="hover:bg-surface-subtle cursor-pointer border-t border-border-faint"
									onClick={() => onToggleColumn(col)}
								>
									<td className="px-2 py-1">
										<input
											type="checkbox"
											checked={selectedColumns.has(col)}
											onChange={() => onToggleColumn(col)}
											onClick={(e) => e.stopPropagation()}
											className="w-4 h-4 accent-primary-hover"
										/>
									</td>
									<td className="px-2 py-1 text-xs">{col}</td>
								</tr>
							))}
						</tbody>
					</table>
				)}
			</div>
			<BlueButton
				title="Insert Columns"
				handleClick={onInsertColumns}
				disabled={insertDisabled}
			/>
		</div>
	);
}
