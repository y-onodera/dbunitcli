import { useState } from "react";
import { DialogActions, DialogField } from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import type { JobContext } from "../../../../hooks/useJob";
import { useTableSelection } from "../../../../hooks/useTableSelection";
import {
	type Conjunction,
	columnLines,
	conditionLines,
	csvFileAlias,
	DEFAULT_CONDITION_OPTIONS,
	defaultTableAlias,
	qualifyColumn,
	subqueryLines,
	uniqueAlias,
} from "../../../../utils/textInsert";
import CsvFileFields from "./CsvFileFields";
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
import { useCsvFile } from "./useCsvFile";

// IN は SELECT する1列、FROM / JOIN に置くときは複数列を選び、EXISTS は SELECT 1 にする
export type SubqueryMode = "in" | "exists" | "from";

// サブクエリのテーブルの選び方。tablesはDBのテーブル一覧から、csvはCSVファイルをCSVREADで読む（encodingは文字コードの初期値）
export type SubquerySource =
	| {
			kind: "tables";
			tables: string[];
			onQueryColumns: (table: string, context: JobContext) => Promise<string[]>;
	  }
	| { kind: "csv"; encoding: string };

type SubqueryDialogProps = {
	mode: Exclude<SubqueryMode, "from">;
	// 外側の別名。サブクエリの別名のデフォルトが重ならないようにする
	usedAliases: string[];
	onComplete: (lines: string[]) => void;
	onClose: () => void;
};

export default function SqlSubqueryDialog({
	source,
	...props
}: SubqueryDialogProps & { source: SubquerySource }) {
	if (source.kind === "csv") {
		return <CsvSubqueryTable encoding={source.encoding} {...props} />;
	}
	return (
		<ListSubqueryTable
			tables={source.tables}
			onQueryColumns={source.onQueryColumns}
			{...props}
		/>
	);
}

function ListSubqueryTable({
	mode,
	tables,
	usedAliases,
	onQueryColumns,
	onComplete,
	onClose,
}: SubqueryDialogProps & {
	tables: string[];
	onQueryColumns: (table: string, context: JobContext) => Promise<string[]>;
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

	return (
		renderSubqueryColumns(columnLoad, mode, alias, onComplete, onClose) ?? (
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
				<SubqueryTableActions
					alias={alias}
					onAliasChange={setAlias}
					canProceed={selectedTable !== null}
					onNext={() => selectedTable && columnLoad.open(selectedTable)}
					onClose={onClose}
				/>
			</NestedDialogFrame>
		)
	);
}

// csvqにはDBのテーブル一覧が無いため、ファイルを1つ選んでCSVREADで読む
function CsvSubqueryTable({
	mode,
	encoding,
	usedAliases,
	onComplete,
	onClose,
}: SubqueryDialogProps & { encoding: string }) {
	const csv = useCsvFile(encoding, (path) =>
		uniqueAlias(csvFileAlias(path), usedAliases),
	);

	return (
		renderSubqueryColumns(
			csv.columnLoad,
			mode,
			csv.alias,
			onComplete,
			onClose,
		) ?? (
			<NestedDialogFrame title="Subquery Table">
				<CsvFileFields idPrefix="sqlSubquery" {...csv.fields} />
				<SubqueryTableActions
					alias={csv.alias}
					onAliasChange={csv.setAlias}
					canProceed={csv.ready}
					onNext={() => csv.columnLoad.open(csv.expression)}
					onClose={onClose}
				/>
			</NestedDialogFrame>
		)
	);
}

// テーブルを選んだ後の列の選択。列を読む前ならnull
function renderSubqueryColumns(
	columnLoad: ReturnType<typeof useColumnLoad>,
	mode: Exclude<SubqueryMode, "from">,
	alias: string,
	onComplete: (lines: string[]) => void,
	onClose: () => void,
) {
	const { target } = columnLoad;
	if (target === null) {
		return null;
	}
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

function SubqueryTableActions({
	alias,
	onAliasChange,
	canProceed,
	onNext,
	onClose,
}: {
	alias: string;
	onAliasChange: (value: string) => void;
	canProceed: boolean;
	onNext: () => void;
	onClose: () => void;
}) {
	return (
		<>
			<DialogField last>
				<OptionText
					id="sqlSubquery_alias"
					label="Table alias"
					value={alias}
					onChange={onAliasChange}
				/>
			</DialogField>
			<DialogActions>
				<BlueButton title="Next" handleClick={onNext} disabled={!canProceed} />
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
		</>
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
