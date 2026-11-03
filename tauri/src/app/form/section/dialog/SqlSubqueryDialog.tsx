import { useState } from "react";
import { DialogActions, DialogField } from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import {
	ControllTextBox,
	InputLabel,
} from "../../../../components/element/Input";
import { type JobContext, useJob } from "../../../../hooks/useJob";
import { useTableSelection } from "../../../../hooks/useTableSelection";
import {
	type Conjunction,
	conditionLines,
	defaultTableAlias,
	qualifyColumn,
	subqueryColumnLine,
	subqueryLines,
	uniqueAlias,
} from "../../../../utils/textInsert";
import SelectableTable from "./SelectableTable";
import {
	InsertPreview,
	NestedDialogFrame,
	OptionCheck,
	OptionSelect,
	renderColumnLoadState,
} from "./SqlInsertParts";

export const CONJUNCTION_GROUPS = [
	{
		label: "",
		options: [
			{ value: "AND" as const, label: "AND" },
			{ value: "OR" as const, label: "OR" },
		],
	},
];

// IN は SELECT する1列を選び、EXISTS は SELECT 1 にする
export type SubqueryMode = "in" | "exists";

export default function SqlSubqueryDialog({
	mode,
	tables,
	usedAliases,
	onQueryColumns,
	onComplete,
	onClose,
}: {
	mode: SubqueryMode;
	tables: string[];
	// 外側の別名。サブクエリの別名のデフォルトが重ならないようにする
	usedAliases: string[];
	onQueryColumns: (table: string, context: JobContext) => Promise<string[]>;
	onComplete: (lines: string[]) => void;
	onClose: () => void;
}) {
	const { selected, toggleOnly } = useTableSelection();
	const [alias, setAlias] = useState("");
	const [target, setTarget] = useState<{
		table: string;
		columns: string[] | null;
	} | null>(null);
	const columnJob = useJob();
	const selectedTable = tables.find((t) => selected.has(t)) ?? null;

	const handleToggle = (table: string) => {
		toggleOnly(table);
		setAlias(
			selected.has(table)
				? ""
				: uniqueAlias(defaultTableAlias(table), usedAliases),
		);
	};

	const handleNext = async () => {
		if (selectedTable === null) {
			return;
		}
		setTarget({ table: selectedTable, columns: null });
		const result = await columnJob.run((context) =>
			onQueryColumns(selectedTable, context),
		);
		if (result) {
			setTarget({ table: selectedTable, columns: result.value });
		}
	};

	if (target !== null) {
		return (
			renderColumnLoadState({
				title: "Subquery Columns",
				table: target.table,
				columns: target.columns,
				loading: columnJob.running,
				error: columnJob.error,
				logs: columnJob.logs,
				onClose,
			}) ?? (
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
				<InputLabel
					text="Table alias"
					id="sqlSubquery_alias"
					required={false}
				/>
				<ControllTextBox
					name="sqlSubquery_alias"
					id="sqlSubquery_alias"
					required={false}
					value={alias}
					handleChange={(ev) => setAlias(ev.target.value)}
				/>
			</DialogField>
			<DialogActions>
				<BlueButton
					title="Next"
					handleClick={handleNext}
					disabled={selectedTable === null}
				/>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
		</NestedDialogFrame>
	);
}

function SubqueryColumns({
	mode,
	table,
	alias,
	columns,
	onComplete,
	onClose,
}: {
	mode: SubqueryMode;
	table: string;
	alias: string;
	columns: string[];
	onComplete: (lines: string[]) => void;
	onClose: () => void;
}) {
	const selectColumn = useTableSelection();
	const whereColumns = useTableSelection();
	const [qualify, setQualify] = useState(alias !== "");
	const [conjunction, setConjunction] = useState<Conjunction>("AND");
	const qualifier = qualify ? alias : "";
	const selectedColumn = columns.find((c) => selectColumn.selected.has(c));
	const conditions = columns
		.filter((c) => whereColumns.selected.has(c))
		.map((c) => qualifyColumn(c, qualifier));

	const selectList =
		mode === "exists" || selectedColumn === undefined
			? [subqueryColumnLine("1")]
			: [subqueryColumnLine(qualifyColumn(selectedColumn, qualifier))];
	const where =
		conditions.length === 0
			? []
			: conditionLines(conditions, "where", {
					operator: "equals",
					not: false,
					conjunction,
					group: false,
					connector: null,
					keyword: true,
					inValues: "",
					subquery: null,
				});
	const lines = subqueryLines(table, alias, selectList, where);
	const missingSelect = mode === "in" && selectedColumn === undefined;

	return (
		<NestedDialogFrame title="Subquery Columns" table={table}>
			{mode === "in" && (
				<DialogField>
					<p className="text-sm font-medium mb-1">SELECT column</p>
					<SelectableTable
						items={columns}
						selected={selectColumn.selected}
						onToggle={selectColumn.toggleOnly}
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
				<div className="flex gap-3">
					<OptionSelect
						id="sqlSubquery_conjunction"
						label="Join conditions with"
						groups={CONJUNCTION_GROUPS}
						onChange={setConjunction}
					/>
				</div>
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
			<InsertPreview lines={lines} />
			<DialogActions>
				<BlueButton
					title="Insert"
					handleClick={() => onComplete(lines)}
					disabled={missingSelect}
				/>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
		</NestedDialogFrame>
	);
}
