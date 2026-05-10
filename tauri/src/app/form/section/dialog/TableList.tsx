import { Fragment, useEffect, useRef, useState } from "react";
import { ButtonIcon } from "../../../../components/element/ButtonIcon";
import { AddIcon, ExpandIcon } from "../../../../components/element/Icon";

export interface TableListProps {
	tables: string[];
	selected: Set<string>;
	onToggleAll: (tables: string[], checked: boolean) => void;
	onToggle: (table: string) => void;
	maxHeightClass?: string;
	onQueryColumns?: (table: string) => Promise<string[]>;
	onInsertColumn?: (column: string) => void;
	onSelectTable?: (table: string) => void;
	activeTable?: string;
}

export default function TableList({
	tables,
	selected,
	onToggleAll,
	onToggle,
	maxHeightClass = "max-h-96",
	onQueryColumns,
	onInsertColumn,
	onSelectTable,
	activeTable,
}: TableListProps) {
	const [filter, setFilter] = useState("");
	const [columnMap, setColumnMap] = useState<Map<string, string[] | "loading">>(
		new Map(),
	);
	const isMountedRef = useRef(true);
	useEffect(() => {
		return () => {
			isMountedRef.current = false;
		};
	}, []);

	const filterLower = filter.toLowerCase();
	const filteredTables = filterLower
		? tables.filter((t) => t.toLowerCase().includes(filterLower))
		: tables;
	const allSelected =
		filteredTables.length > 0 && filteredTables.every((t) => selected.has(t));

	const handleQueryColumns = (table: string) => {
		if (!onQueryColumns) {
			return;
		}
		if (columnMap.get(table) === "loading") {
			return;
		}
		if (columnMap.has(table)) {
			setColumnMap((prev) => {
				const next = new Map(prev);
				next.delete(table);
				return next;
			});
			return;
		}
		setColumnMap((prev) => new Map(prev).set(table, "loading"));
		onQueryColumns(table).then(
			(columns) => {
				if (isMountedRef.current) {
					setColumnMap((prev) => new Map(prev).set(table, columns));
				}
			},
			() => {
				if (isMountedRef.current) {
					setColumnMap((prev) => {
						const next = new Map(prev);
						next.delete(table);
						return next;
					});
				}
			},
		);
	};

	return (
		<div>
			<input
				type="text"
				value={filter}
				onChange={(e) => setFilter(e.target.value)}
				placeholder="Filter tables..."
				className="w-full mb-1 px-2 py-1 text-sm border border-border rounded bg-input focus-visible:ring-3 ring-primary-ring"
			/>
			<div
				className={`relative overflow-x-auto ${maxHeightClass} overflow-y-auto border border-border-subtle rounded`}
			>
				<table className="w-full text-sm text-left">
					<thead className="bg-surface-subtle sticky top-0">
						<tr>
							<th className="px-3 py-2 w-8">
								<input
									type="checkbox"
									checked={allSelected}
									onChange={(e) =>
										onToggleAll(filteredTables, e.target.checked)
									}
									className="w-4 h-4 accent-primary-hover"
								/>
							</th>
							<th className="px-3 py-2 font-medium text-content-secondary">
								Table Name
							</th>
						</tr>
					</thead>
					<tbody>
						{filteredTables.map((table) => (
							<Fragment key={table}>
								<tr
									className="hover:bg-surface-subtle cursor-pointer border-t border-border-faint"
									onClick={() => onToggle(table)}
								>
									<td className="px-3 py-1.5">
										<input
											type="checkbox"
											checked={selected.has(table)}
											onChange={() => onToggle(table)}
											onClick={(e) => e.stopPropagation()}
											className="w-4 h-4 accent-primary-hover"
										/>
									</td>
									<td className="px-3 py-1.5">
										<div className="flex items-center gap-1">
											{table}
											{onQueryColumns && (
												<ButtonIcon
													handleClick={(e) => {
														e.stopPropagation();
														handleQueryColumns(table);
													}}
												>
													{columnMap.get(table) === "loading" ? (
														<span className="text-xs text-content-disabled w-3 h-3">
															…
														</span>
													) : (
														<ExpandIcon close={!columnMap.has(table)} />
													)}
												</ButtonIcon>
											)}
											{onSelectTable && (
												<ButtonIcon
													handleClick={(e) => {
														e.stopPropagation();
														onSelectTable(table);
													}}
												>
													<ExpandIcon close={activeTable !== table} />
												</ButtonIcon>
											)}
										</div>
									</td>
								</tr>
								{Array.isArray(columnMap.get(table)) &&
									(columnMap.get(table) as string[]).map((col) => (
										<tr
											key={`${table}::${col}`}
											className="bg-surface-subtle border-t border-border-faint"
										>
											<td />
											<td className="px-6 py-1">
												<div className="flex items-center gap-2">
													<span className="text-xs text-content-muted">
														{col}
													</span>
													{onInsertColumn && (
														<ButtonIcon handleClick={() => onInsertColumn(col)}>
															<AddIcon title="Insert column" />
														</ButtonIcon>
													)}
												</div>
											</td>
										</tr>
									))}
							</Fragment>
						))}
					</tbody>
				</table>
			</div>
		</div>
	);
}
