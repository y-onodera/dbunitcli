import { useEffect, useRef, useState } from "react";
import { SettingDialog } from "../../components/dialog";
import { useJdbcTables } from "../../hooks/useJdbc";
import { useSaveDataSource } from "../../hooks/useQueryDatasource";
import { saveOnSuccess } from "../../utils/fetchUtils";

interface JdbcTableSelectorDialogProps {
	jdbcValues: Record<string, string>;
	currentContent: string;
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
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
		<div className="relative overflow-x-auto max-h-96 overflow-y-auto border border-gray-200 rounded">
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
						<th className="px-3 py-2 font-medium text-gray-700">Table Name</th>
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

function TableContent({
	loading,
	tables,
	selected,
	onToggleAll,
	onToggle,
}: {
	loading: boolean;
	tables: string[];
	selected: Set<string>;
	onToggleAll: (checked: boolean) => void;
	onToggle: (table: string) => void;
}) {
	if (loading) {
		return <p className="text-sm text-gray-500">Loading...</p>;
	}
	if (tables.length === 0) {
		return <p className="text-sm text-gray-500">No tables found</p>;
	}
	return (
		<TableList
			tables={tables}
			selected={selected}
			onToggleAll={onToggleAll}
			onToggle={onToggle}
		/>
	);
}

export default function JdbcTableSelectorDialog({
	jdbcValues,
	currentContent,
	fileName,
	handleDialogClose,
	handleSave,
}: JdbcTableSelectorDialogProps) {
	const [tables, setTables] = useState<string[]>([]);
	const [selected, setSelected] = useState<Set<string>>(new Set());
	const [loading, setLoading] = useState(true);
	const getJdbcTables = useJdbcTables();
	const saveDataSource = useSaveDataSource();
	const jdbcValuesRef = useRef(jdbcValues);
	const currentContentRef = useRef(currentContent);

	useEffect(() => {
		const load = async () => {
			setLoading(true);
			try {
				const result = await getJdbcTables(jdbcValuesRef.current);
				setTables(result);
				const existing = new Set(
					currentContentRef.current
						.split("\n")
						.map((t) => t.trim())
						.filter((t) => t.length > 0),
				);
				setSelected(existing);
			} finally {
				setLoading(false);
			}
		};
		void load();
	}, [getJdbcTables]);

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

	const handleSaveWithPath = (path: string) => {
		const contents = tables.filter((t) => selected.has(t)).join("\n");
		return saveOnSuccess(
			() => saveDataSource({ type: "table", name: path, contents }),
			() => handleSave(path),
		);
	};

	return (
		<SettingDialog
			fileName={fileName}
			handleDialogClose={handleDialogClose}
			handleSave={handleSaveWithPath}
		>
			<div className="p-4">
				<h2 className="text-lg font-semibold mb-4">Select Tables</h2>
				<TableContent
					loading={loading}
					tables={tables}
					selected={selected}
					onToggleAll={toggleAll}
					onToggle={toggleTable}
				/>
			</div>
		</SettingDialog>
	);
}
