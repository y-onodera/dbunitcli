import { useCallback, useState } from "react";
import {
	BlueButton,
	WhiteButton,
} from "../../components/element/Button";
import { useJdbcTables } from "../../hooks/useJdbc";
import TableList from "./TableList";

interface SqlTableInsertDialogProps {
	jdbcValues: Record<string, string>;
	onInsert: (tables: string[]) => void;
	onClose: () => void;
}

export default function SqlTableInsertDialog({
	jdbcValues,
	onInsert,
	onClose,
}: SqlTableInsertDialogProps) {
	const [tables, setTables] = useState<string[] | null>(null);
	const [selected, setSelected] = useState<Set<string>>(new Set());
	const [loading, setLoading] = useState(false);
	const getJdbcTables = useJdbcTables();

	const handleLoad = useCallback(async () => {
		setLoading(true);
		try {
			const result = await getJdbcTables(jdbcValues);
			setTables(result);
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
			setSelected(new Set(tables ?? []));
		} else {
			setSelected(new Set());
		}
	};

	const handleInsert = () => {
		const selectedTables = (tables ?? []).filter((t) => selected.has(t));
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
					{!loading && tables !== null && tables.length === 0 && (
						<p className="text-sm text-gray-500">No tables found</p>
					)}
					{!loading && tables !== null && tables.length > 0 && (
						<TableList
							tables={tables}
							selected={selected}
							onToggleAll={toggleAll}
							onToggle={toggleTable}
							maxHeightClass="max-h-72"
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
