import { Suspense, use, useMemo } from "react";
import { SettingDialog } from "../../../../components/dialog";
import { useJdbcTables } from "../../../../hooks/useJdbc";
import { useSaveDataSource } from "../../../../hooks/useQueryDatasource";
import { useTableSelection } from "../../../../hooks/useTableSelection";
import { saveOnSuccess } from "../../../../utils/fetchUtils";
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
	const initial = useMemo(
		() =>
			currentContent
				.split("\n")
				.map((t) => t.trim())
				.filter((t) => t.length > 0),
		[currentContent],
	);
	const { selected, toggle, toggleAll } = useTableSelection(tables, initial);
	const saveDataSource = useSaveDataSource();

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
						onToggle={toggle}
					/>
				)}
			</div>
		</SettingDialog>
	);
}
