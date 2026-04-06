import { Suspense, use, useState } from "react";
import { SettingDialog } from "../../components/dialog";
import { useJdbcTables } from "../../hooks/useJdbc";
import { useSaveDataSource } from "../../hooks/useQueryDatasource";
import { saveOnSuccess } from "../../utils/fetchUtils";
import TableList from "./TableList";

interface JdbcTableSelectorDialogProps {
	jdbcValues: Record<string, string>;
	currentContent: string;
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}

export default function JdbcTableSelectorDialog({
	jdbcValues,
	currentContent,
	fileName,
	handleDialogClose,
	handleSave,
}: JdbcTableSelectorDialogProps) {
	const getJdbcTables = useJdbcTables();
	return (
		<Suspense fallback={<div>Loading...</div>}>
			<Dialog
				promise={getJdbcTables(jdbcValues)}
				currentContent={currentContent}
				fileName={fileName}
				handleDialogClose={handleDialogClose}
				handleSave={handleSave}
			/>
		</Suspense>
	);
}

function Dialog({
	promise,
	currentContent,
	fileName,
	handleDialogClose,
	handleSave,
}: {
	promise: Promise<string[]>;
	currentContent: string;
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const tables = use(promise);
	const [selected, setSelected] = useState<Set<string>>(
		() =>
			new Set(
				currentContent
					.split("\n")
					.map((t) => t.trim())
					.filter((t) => t.length > 0),
			),
	);
	const saveDataSource = useSaveDataSource();

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
				{tables.length === 0 ? (
					<p className="text-sm text-gray-500">No tables found</p>
				) : (
					<TableList
						tables={tables}
						selected={selected}
						onToggleAll={toggleAll}
						onToggle={toggleTable}
					/>
				)}
			</div>
		</SettingDialog>
	);
}
