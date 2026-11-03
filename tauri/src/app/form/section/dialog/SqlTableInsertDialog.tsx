import { useRef, useState } from "react";
import {
	DialogActions,
	DialogField,
	DialogTitle,
	JobFailure,
	ModalOverlay,
	StatusMessage,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { JobProgress } from "../../../../components/element/JobProgress";
import { useJdbcColumns, useJdbcTables } from "../../../../hooks/useJdbc";
import { type JobContext, useJob } from "../../../../hooks/useJob";
import { useTableSelection } from "../../../../hooks/useTableSelection";
import {
	type DerivedTableUsage,
	defaultTableAlias,
	derivedTableLines,
	isJoin,
	isStatementUsage,
	type StatementUsage,
	statementColumnUsage,
	type TableUsage,
	tableLines,
	usesDerivedTable,
	usesTableAlias,
} from "../../../../utils/textInsert";
import SqlColumnInsertDialog from "./SqlColumnInsertDialog";
import {
	InsertPreview,
	OptionSelect,
	OptionText,
	type SelectGroup,
	type SelectOption,
} from "./SqlInsertParts";
import { SubqueryColumns, type SubquerySource } from "./SqlSubqueryDialog";
import TableList from "./TableList";
import { useColumnLoad } from "./useColumnLoad";

export const TABLE_USAGE_GROUPS: SelectGroup<TableUsage>[] = [
	{
		label: "Statement",
		options: [
			{ value: "select", label: "Select" },
			{ value: "insert", label: "Insert" },
			{ value: "update", label: "Update" },
			{ value: "delete", label: "Delete" },
		],
	},
	{
		label: "Join",
		options: [
			{ value: "leftJoin", label: "Left join" },
			{ value: "innerJoin", label: "Inner join" },
			{ value: "crossJoin", label: "Cross join" },
		],
	},
];

type TableSource = "table" | "subquery";
const TABLE_SOURCE_OPTIONS: SelectOption<TableSource>[] = [
	{ value: "table", label: "Table" },
	{ value: "subquery", label: "Subquery" },
];

interface SqlTableInsertDialogProps {
	jdbcValues: Record<string, string>;
	onInsert: (tables: string[]) => void;
	// inlineなら改行を足さずにカーソル位置へ入れる
	onInsertColumn: (columns: string[], inline?: boolean) => void;
	plain?: boolean;
	onClose: () => void;
}

export default function SqlTableInsertDialog({
	jdbcValues,
	onInsert,
	onInsertColumn,
	plain = false,
	onClose,
}: SqlTableInsertDialogProps) {
	const [tables, setTables] = useState<string[] | null>(null);
	// 失敗したら原因を追えるよう、エラーと実行中のログを残す。閉じると（アンマウントでabort）sidecar側の読込も中断する
	const job = useJob();
	const getJdbcTables = useJdbcTables();
	const getJdbcColumns = useJdbcColumns();

	const handleLoad = async () => {
		if (job.running) {
			return;
		}
		columnCache.current.clear();
		const result = await job.run((context) =>
			getJdbcTables(jdbcValues, context),
		);
		if (result) {
			setTables(result.value);
		}
	};

	// 同じテーブルの列は、テーブル一覧を読み直すまで問い合わせ直さない
	const columnCache = useRef(new Map<string, string[]>());

	const queryColumns = async (table: string, context: JobContext) => {
		const cached = columnCache.current.get(table);
		if (cached !== undefined) {
			return cached;
		}
		const columns = await getJdbcColumns(jdbcValues, table, context);
		columnCache.current.set(table, columns);
		return columns;
	};

	function renderBody() {
		if (job.running) {
			return (
				<JobProgress title="Loading..." lines={job.logs} onAbandon={onClose} />
			);
		}
		if (job.error || tables === null) {
			return (
				<>
					{job.error && <JobFailure message={job.error} lines={job.logs} />}
					<DialogActions>
						<WhiteButton title="Cancel" handleClick={onClose} />
					</DialogActions>
				</>
			);
		}
		return (
			<TablesContent
				tables={tables}
				onInsert={onInsert}
				onInsertColumn={onInsertColumn}
				plain={plain}
				onQueryColumns={queryColumns}
				onClose={onClose}
			/>
		);
	}

	return (
		<ModalOverlay>
			<DialogTitle>Select Tables</DialogTitle>
			<DialogField last>
				<BlueButton title="Load Tables" handleClick={handleLoad} />
			</DialogField>
			{renderBody()}
		</ModalOverlay>
	);
}

function TablesContent({
	tables,
	onInsert,
	onInsertColumn,
	plain,
	onQueryColumns,
	onClose,
}: {
	tables: string[];
	onInsert: (tables: string[]) => void;
	onInsertColumn: (columns: string[], inline?: boolean) => void;
	plain: boolean;
	onQueryColumns: (table: string, context: JobContext) => Promise<string[]>;
	onClose: () => void;
}) {
	// plainは名前だけを複数行で挿入する。それ以外は句を組み立てるため1テーブルだけ選ぶ
	const { selected, toggle, toggleOnly, toggleAll } = useTableSelection();
	// 列選択ダイアログの列の読込と、テーブル側の用途から文全体を挿入するときの用途（行の列選択アイコンから開いたときはnull）
	const columnLoad = useColumnLoad(onQueryColumns);
	const [columnStatement, setColumnStatement] = useState<StatementUsage | null>(
		null,
	);
	const [usage, setUsage] = useState<TableUsage>("select");
	const subquerySource: SubquerySource = {
		kind: "tables",
		tables,
		onQueryColumns,
	};
	const [alias, setAlias] = useState("");
	const [source, setSource] = useState<TableSource>("table");
	// サブクエリにするときの、サブクエリ全体の別名（選んだテーブルの別名はalias）
	const [subqueryAlias, setSubqueryAlias] = useState("");
	// サブクエリにするとき: サブクエリで使う列の読込と、その後の外側の列の選択
	const derivedLoad = useColumnLoad(onQueryColumns);
	const [derivedOuter, setDerivedOuter] = useState<{
		usage: Exclude<DerivedTableUsage, "crossJoin">;
		subquery: string[];
		columns: string[];
	} | null>(null);
	const selectedTable = plain
		? null
		: (tables.find((t) => selected.has(t)) ?? null);
	const derived = !plain && usesDerivedTable(usage) && source === "subquery";
	const aliasMissing = derived
		? subqueryAlias.trim() === ""
		: isJoin(usage) && alias.trim() === "";

	const handleUsageChange = (next: TableUsage) => {
		setUsage(next);
		setSource("table");
	};

	const handleToggle = (table: string) => {
		if (plain) {
			toggle(table);
			return;
		}
		toggleOnly(table);
		setAlias(selected.has(table) ? "" : defaultTableAlias(table));
	};

	const handleInsert = () => {
		if (selectedTable === null) {
			onInsert(tables.filter((t) => selected.has(t)));
			return;
		}
		if (derived) {
			derivedLoad.open(selectedTable);
			return;
		}
		if (isStatementUsage(usage)) {
			openColumnDialog(selectedTable, usage);
			return;
		}
		onInsert(tableLines(selectedTable, usage, alias, []));
	};

	const openColumnDialog = (
		table: string,
		statement: StatementUsage | null,
	) => {
		setColumnStatement(statement);
		columnLoad.open(table);
	};

	const handleDerivedSubquery = (subquery: string[], columns: string[]) => {
		derivedLoad.close();
		if (!usesDerivedTable(usage)) {
			return;
		}
		if (usage === "crossJoin") {
			onInsert(derivedTableLines(usage, subquery, subqueryAlias, []));
			return;
		}
		setDerivedOuter({ usage, subquery, columns });
	};

	const statementOf = (table: string, statement: StatementUsage | null) =>
		statement === null
			? undefined
			: {
					usage: statementColumnUsage(statement),
					wrap: (lines: string[]) => tableLines(table, statement, alias, lines),
				};

	// 列の修飾には、選択中のテーブルなら入力済みの別名を使い（空なら修飾しない）、別名があれば最初から修飾する
	const columnAliasOf = (table: string) =>
		table === selectedTable && usesTableAlias(usage)
			? { alias: alias.trim(), qualify: alias.trim() !== "" }
			: { alias: defaultTableAlias(table), qualify: false };

	return (
		<>
			<DialogField last={plain}>
				{tables.length === 0 ? (
					<StatusMessage kind="empty">No tables found</StatusMessage>
				) : (
					<TableList
						tables={tables}
						selected={selected}
						onToggleAll={plain ? toggleAll : undefined}
						onToggle={handleToggle}
						onSelectTable={(table) => openColumnDialog(table, null)}
					/>
				)}
			</DialogField>
			{!plain && (
				<>
					<DialogField>
						<div className="flex gap-3">
							<OptionSelect
								id="sqlTableInsert_usage"
								label="Usage"
								groups={TABLE_USAGE_GROUPS}
								onChange={handleUsageChange}
							/>
							<OptionText
								id="sqlTableInsert_alias"
								label="Table alias"
								required={isJoin(usage) && !derived}
								disabled={!usesTableAlias(usage)}
								value={usesTableAlias(usage) ? alias : ""}
								onChange={setAlias}
							/>
						</div>
					</DialogField>
					{usesDerivedTable(usage) && (
						<DialogField>
							<div className="flex gap-3">
								<OptionSelect
									key={usage}
									id="sqlTableInsert_source"
									label="Source"
									options={TABLE_SOURCE_OPTIONS}
									onChange={setSource}
								/>
								{derived && (
									<OptionText
										id="sqlTableInsert_subqueryAlias"
										label="Subquery alias"
										required
										value={subqueryAlias}
										onChange={setSubqueryAlias}
									/>
								)}
							</div>
						</DialogField>
					)}
					<InsertPreview
						lines={
							// 列まで選ぶ用途は、列選択ダイアログで文全体を見せる
							selectedTable === null || isStatementUsage(usage) || derived
								? []
								: tableLines(selectedTable, usage, alias, [])
						}
					/>
				</>
			)}
			<DialogActions>
				<BlueButton
					title={
						!plain && (isStatementUsage(usage) || derived) ? "Next" : "Insert"
					}
					handleClick={handleInsert}
					disabled={selected.size === 0 || (!plain && aliasMissing)}
				/>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
			{columnLoad.target !== null && (
				<SqlColumnInsertDialog
					table={columnLoad.target.table}
					columns={columnLoad.target.columns}
					loading={columnLoad.job.running}
					error={columnLoad.job.error}
					logs={columnLoad.job.logs}
					plain={plain}
					{...columnAliasOf(columnLoad.target.table)}
					statement={statementOf(columnLoad.target.table, columnStatement)}
					subquerySource={subquerySource}
					onInsert={(lines, inline) => {
						if (columnStatement === null) {
							if (inline) {
								onInsertColumn(lines, true);
							} else {
								onInsertColumn(lines);
							}
						} else {
							onInsert(lines);
						}
						columnLoad.close();
					}}
					onClose={columnLoad.close}
				/>
			)}
			{derivedLoad.target !== null &&
				(derivedLoad.renderState("Subquery Columns") ?? (
					<SubqueryColumns
						mode="from"
						table={derivedLoad.target.table}
						alias={alias.trim()}
						columns={derivedLoad.target.columns ?? []}
						completeTitle={usage === "crossJoin" ? "Insert" : "Next"}
						onComplete={handleDerivedSubquery}
						onClose={derivedLoad.close}
					/>
				))}
			{derivedOuter !== null && (
				<SqlColumnInsertDialog
					table={`(subquery) ${subqueryAlias.trim()}`}
					columns={derivedOuter.columns}
					loading={false}
					error=""
					logs={[]}
					plain={false}
					alias={subqueryAlias.trim()}
					qualify
					statement={{
						usage: statementColumnUsage(derivedOuter.usage),
						wrap: (lines) =>
							derivedTableLines(
								derivedOuter.usage,
								derivedOuter.subquery,
								subqueryAlias,
								lines,
							),
					}}
					subquerySource={subquerySource}
					onInsert={(lines) => {
						onInsert(lines);
						setDerivedOuter(null);
					}}
					onClose={() => setDerivedOuter(null)}
				/>
			)}
		</>
	);
}
