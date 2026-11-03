import { useState } from "react";
import { DialogActions, DialogField } from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import type { JobContext } from "../../../../hooks/useJob";
import { useTableSelection } from "../../../../hooks/useTableSelection";
import {
	type Conjunction,
	columnLines,
	conditionLines,
	DEFAULT_CONDITION_OPTIONS,
	defaultTableAlias,
	qualifyColumn,
	subqueryLines,
	uniqueAlias,
} from "../../../../utils/textInsert";
import SelectableTable from "./SelectableTable";
import {
	CONJUNCTION_OPTIONS,
	InsertPreview,
	NestedDialogFrame,
	OptionCheck,
	OptionSelect,
	OptionText,
} from "./SqlInsertParts";
import { useColumnLoad } from "./useColumnLoad";

// IN は SELECT する1列、FROM / JOIN に置くときは複数列を選び、EXISTS は SELECT 1 にする
export type SubqueryMode = "in" | "exists" | "from";

export default function SqlSubqueryDialog({
	mode,
	tables,
	usedAliases,
	onQueryColumns,
	onComplete,
	onClose,
}: {
	mode: Exclude<SubqueryMode, "from">;
	tables: string[];
	// 外側の別名。サブクエリの別名のデフォルトが重ならないようにする
	usedAliases: string[];
	onQueryColumns: (table: string, context: JobContext) => Promise<string[]>;
	onComplete: (lines: string[]) => void;
	onClose: () => void;
}) {
	const { selected, toggleOnly } = useTableSelection();
	const [alias, setAlias] = useState("");
	const columnLoad = useColumnLoad(onQueryColumns);
	const selectedTable = tables.find((t) => selected.has(t)) ?? null;

	const handleToggle = (table: string) => {
		toggleOnly(table);
		setAlias(
			selected.has(table)
				? ""
				: uniqueAlias(defaultTableAlias(table), usedAliases),
		);
	};

	const { target } = columnLoad;
	if (target !== null) {
		return (
			columnLoad.renderState("Subquery Columns") ?? (
				<SubqueryColumns
					mode={mode}
					table={target.table}
					alias={alias.trim()}
					columns={target.columns ?? []}
					onComplete={onComplete}
					onClose={onClose}
				/>
			)
		);
	}

	return (
		<NestedDialogFrame title="Subquery Table">
			<DialogField>
				<SelectableTable
					items={tables}
					selected={selected}
					onToggle={handleToggle}
					columnLabel="Table Name"
					filterPlaceholder="Filter tables..."
				/>
			</DialogField>
			<DialogField last>
				<OptionText
					id="sqlSubquery_alias"
					label="Table alias"
					value={alias}
					onChange={setAlias}
				/>
			</DialogField>
			<DialogActions>
				<BlueButton
					title="Next"
					handleClick={() => selectedTable && columnLoad.open(selectedTable)}
					disabled={selectedTable === null}
				/>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
		</NestedDialogFrame>
	);
}

export function SubqueryColumns({
	mode,
	table,
	alias,
	columns,
	completeTitle = "Insert",
	onComplete,
	onClose,
}: {
	mode: SubqueryMode;
	table: string;
	alias: string;
	columns: string[];
	completeTitle?: string;
	// selectedはSELECTした列名（修飾なし）
	onComplete: (lines: string[], selected: string[]) => void;
	onClose: () => void;
}) {
	const selectColumn = useTableSelection();
	const whereColumns = useTableSelection();
	const [qualify, setQualify] = useState(alias !== "");
	const [conjunction, setConjunction] = useState<Conjunction>("AND");
	const qualifier = qualify ? alias : "";
	const selectedColumns = columns.filter((c) => selectColumn.selected.has(c));
	const conditions = columns
		.filter((c) => whereColumns.selected.has(c))
		.map((c) => qualifyColumn(c, qualifier));

	// EXISTS（と列を選ぶ前）は 1 を、IN は先頭の1列を SELECT する
	const selectListOf = (targets: string[]) => {
		if (mode === "exists" || targets.length === 0) {
			return columnLines(["1"], "select", { alias: "", columnAs: false });
		}
		return columnLines(
			mode === "in" ? targets.slice(0, 1) : targets,
			"select",
			{
				alias: qualifier,
				columnAs: false,
			},
		);
	};
	const where =
		conditions.length === 0
			? []
			: conditionLines(conditions, "where", {
					...DEFAULT_CONDITION_OPTIONS,
					conjunction,
				});
	const linesOf = (targets: string[]) =>
		subqueryLines(table, alias, selectListOf(targets), where);
	const missingSelect = mode !== "exists" && selectedColumns.length === 0;

	return (
		<NestedDialogFrame title="Subquery Columns" table={table}>
			{mode !== "exists" && (
				<DialogField>
					<p className="text-sm font-medium mb-1">
						{mode === "in" ? "SELECT column" : "SELECT columns"}
					</p>
					<SelectableTable
						items={columns}
						selected={selectColumn.selected}
						onToggle={
							mode === "in" ? selectColumn.toggleOnly : selectColumn.toggle
						}
						onToggleAll={mode === "in" ? undefined : selectColumn.toggleAll}
						columnLabel="Column Name"
						filterPlaceholder="Filter columns..."
						emptyMessage="No columns found"
					/>
				</DialogField>
			)}
			<DialogField>
				<p className="text-sm font-medium mb-1">WHERE columns</p>
				<SelectableTable
					items={columns}
					selected={whereColumns.selected}
					onToggle={whereColumns.toggle}
					onToggleAll={whereColumns.toggleAll}
					columnLabel="Column Name"
					filterPlaceholder="Filter columns..."
					emptyMessage="No columns found"
				/>
			</DialogField>
			<DialogField>
				<OptionSelect
					id="sqlSubquery_conjunction"
					label="Join conditions with"
					options={CONJUNCTION_OPTIONS}
					onChange={setConjunction}
				/>
			</DialogField>
			{alias !== "" && (
				<DialogField>
					<OptionCheck
						id="sqlSubquery_qualify"
						label={`Prefix table alias (${alias})`}
						checked={qualify}
						onChange={setQualify}
					/>
				</DialogField>
			)}
			<InsertPreview lines={linesOf(selectedColumns)} />
			<DialogActions>
				<BlueButton
					title={completeTitle}
					handleClick={() =>
						onComplete(linesOf(selectedColumns), selectedColumns)
					}
					disabled={missingSelect}
				/>
				{mode === "from" && (
					<BlueButton
						title="All Columns"
						handleClick={() => onComplete(linesOf(columns), columns)}
						disabled={columns.length === 0}
					/>
				)}
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
		</NestedDialogFrame>
	);
}
