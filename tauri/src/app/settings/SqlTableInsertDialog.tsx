import { useCallback, useState } from "react";
import {
	BlueButton,
	WhiteButton,
} from "../../components/element/Button";
import { useJdbcTables } from "../../hooks/useJdbc";

interface SqlTableInsertDialogProps {
	jdbcValues: Record<string, string>;
	onInsert: (tables: string[]) => void;
	onClose: () => void;
}

function TableList({
	tables,
	selected,
	onToggleAll,
	onToggle,
}: {
	tables: string[];
	selected: Set<string>;
	onToggleAll: (checked: boolean) => void;
	onToggle: (table: string) => void;
}) {
	const allSelected = tables.length > 0 && tables.every((t) => selected.has(t));
	return (
		<div className="relative overflow-x-auto max-h-72 overflow-y-auto border border-gray-200 rounded">
			<table className="w-full text-sm text-left">
				<thead className="bg-gray-50 sticky top-0">
					<tr>
						<th className="px-3 py-2 w-8">
							<input
								type="checkbox"
								checked={allSelected}
								onChange={(e) => onToggleAll(e.target.checked)}
								className="w-4 h-4 accent-indigo-600"
							/>
						</th>
						<th className="px-3 py-2 font-medium text-gray-700">
							Table Name
						</th>
					</tr>
				</thead>
				<tbody>
					{tables.map((table) => (
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
	);
}

export default function SqlTableInsertDialog({
	jdbcValues,
	onInsert,
	onClose,
}: SqlTableInsertDialogProps) {
	const [tables, setTables] = useState<string[]>([]);
	const [selected, setSelected] = useState<Set<string>>(new Set());
	const [loading, setLoading] = useState(false);
	const [loaded, setLoaded] = useState(false);
	const getJdbcTables = useJdbcTables();

	const handleLoad = useCallback(async () => {
		setLoading(true);
		try {
			const result = await getJdbcTables(jdbcValues);
			setTables(result);
			setLoaded(true);
		} finally {
			setLoading(false);
		}
	}, [getJdbcTables, jdbcValues]);

	const toggleTable = (table: string) => {
		setSelected((prev) => {
			const next = new Set(prev);
			if (next.has(table)) {
				next.delete(table);
			} else {
				next.add(table);
			}
			return next;
		});
	};

	const toggleAll = (checked: boolean) => {
		if (checked) {
			setSelected(new Set(tables));
		} else {
			setSelected(new Set());
		}
	};

	const handleInsert = () => {
		const selectedTables = tables.filter((t) => selected.has(t));
		onInsert(selectedTables);
	};

	return (
		<div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
			<div className="bg-white rounded-lg shadow-lg p-6 w-96 max-w-full">
				<h2 className="text-lg font-semibold mb-4">Select Tables</h2>
				<div className="mb-4">
					<BlueButton title="Load Tables" handleClick={handleLoad} disabled={loading} />
				</div>
				<div className="mb-4 min-h-8">
					{loading && (
						<p className="text-sm text-gray-500">Loading...</p>
					)}
					{!loading && loaded && tables.length === 0 && (
						<p className="text-sm text-gray-500">No tables found</p>
					)}
					{!loading && tables.length > 0 && (
						<TableList
							tables={tables}
							selected={selected}
							onToggleAll={toggleAll}
							onToggle={toggleTable}
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
			</div>
		</div>
	);
}
