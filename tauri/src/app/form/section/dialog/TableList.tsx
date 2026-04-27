import { useState } from "react";

export interface TableListProps {
	tables: string[];
	selected: Set<string>;
	onToggleAll: (tables: string[], checked: boolean) => void;
	onToggle: (table: string) => void;
	maxHeightClass?: string;
}

export default function TableList({
	tables,
	selected,
	onToggleAll,
	onToggle,
	maxHeightClass = "max-h-96",
}: TableListProps) {
	const [filter, setFilter] = useState("");
	const filteredTables = filter
		? tables.filter((t) => t.toLowerCase().includes(filter.toLowerCase()))
		: tables;
	const allSelected =
		filteredTables.length > 0 && filteredTables.every((t) => selected.has(t));
	return (
		<div>
			<input
				type="text"
				value={filter}
				onChange={(e) => setFilter(e.target.value)}
				placeholder="Filter tables..."
				className="w-full mb-1 px-2 py-1 text-sm border border-gray-300 rounded bg-gray-50 focus:outline-none focus-visible:ring-3 ring-indigo-300"
			/>
			<div
				className={`relative overflow-x-auto ${maxHeightClass} overflow-y-auto border border-gray-200 rounded`}
			>
				<table className="w-full text-sm text-left">
					<thead className="bg-gray-50 sticky top-0">
						<tr>
							<th className="px-3 py-2 w-8">
								<input
									type="checkbox"
									checked={allSelected}
									onChange={(e) =>
										onToggleAll(filteredTables, e.target.checked)
									}
									className="w-4 h-4 accent-indigo-600"
								/>
							</th>
							<th className="px-3 py-2 font-medium text-gray-700">
								Table Name
							</th>
						</tr>
					</thead>
					<tbody>
						{filteredTables.map((table) => (
							<tr
								key={table}
								className="hover:bg-gray-50 cursor-pointer border-t border-gray-100"
								onClick={() => onToggle(table)}
							>
								<td className="px-3 py-1.5">
									<input
										type="checkbox"
										checked={selected.has(table)}
										onChange={() => onToggle(table)}
										onClick={(e) => e.stopPropagation()}
										className="w-4 h-4 accent-indigo-600"
									/>
								</td>
								<td className="px-3 py-1.5">{table}</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>
		</div>
	);
}
