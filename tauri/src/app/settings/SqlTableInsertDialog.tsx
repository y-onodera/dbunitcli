import { Suspense, use, useState } from "react";
import {
	BlueButton,
	WhiteButton,
} from "../../components/element/Button";
import { useJdbcTables } from "../../hooks/useJdbc";
import { useTableSelection } from "../../hooks/useTableSelection";
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
	const [tablesPromise, setTablesPromise] = useState<Promise<string[]> | null>(
		null,
	);
	const getJdbcTables = useJdbcTables();

	const handleLoad = () => {
		setTablesPromise(getJdbcTables(jdbcValues));
	};

	return (
		<div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
			<div className="bg-white rounded-lg shadow-lg p-6 w-96 max-w-full">
				<h2 className="text-lg font-semibold mb-4">Select Tables</h2>
				<div className="mb-4">
					<BlueButton title="Load Tables" handleClick={handleLoad} />
				</div>
				{tablesPromise === null ? (
					<div className="flex gap-2 justify-end">
						<WhiteButton title="Cancel" handleClick={onClose} />
					</div>
				) : (
					<Suspense
						fallback={<p className="text-sm text-gray-500">Loading...</p>}
					>
						<TablesContent
							promise={tablesPromise}
							onInsert={onInsert}
							onClose={onClose}
						/>
					</Suspense>
				)}
			</div>
		</div>
	);
}

function TablesContent({
	promise,
	onInsert,
	onClose,
}: {
	promise: Promise<string[]>;
	onInsert: (tables: string[]) => void;
	onClose: () => void;
}) {
	const tables = use(promise);
	const { selected, toggle, toggleAll } = useTableSelection(tables);

	const handleInsert = () => {
		const selectedTables = tables.filter((t) => selected.has(t));
		onInsert(selectedTables);
	};

	return (
		<>
			<div className="mb-4 min-h-8">
				{tables.length === 0 ? (
					<p className="text-sm text-gray-500">No tables found</p>
				) : (
					<TableList
						tables={tables}
						selected={selected}
						onToggleAll={toggleAll}
						onToggle={toggle}
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
		</>
	);
}
