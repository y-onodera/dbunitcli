import { useState } from "react";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { useJdbcTables } from "../../../../hooks/useJdbc";
import { useTableSelection } from "../../../../hooks/useTableSelection";
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
	const [loading, setLoading] = useState(false);
	const getJdbcTables = useJdbcTables();

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

	function renderBody() {
		if (loading) {
			return <p className="text-sm text-gray-500">Loading...</p>;
		}
		if (tables !== null) {
			return <TablesContent tables={tables} onInsert={onInsert} onClose={onClose} />;
		}
		return (
			<div className="flex gap-2 justify-end">
				<WhiteButton title="Cancel" handleClick={onClose} />
			</div>
		);
	}

	return (
		<div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
			<div className="bg-white rounded-lg shadow-lg p-6 w-96 max-w-full">
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
	onClose,
}: {
	tables: string[];
	onInsert: (tables: string[]) => void;
	onClose: () => void;
}) {
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
