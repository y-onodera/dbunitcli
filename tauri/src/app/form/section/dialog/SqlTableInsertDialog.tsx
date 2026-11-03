import { useState } from "react";
import {
	DialogActions,
	DialogField,
	DialogTitle,
	JobFailure,
	ModalOverlay,
	StatusMessage,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import {
	ControllTextBox,
	InputLabel,
} from "../../../../components/element/Input";
import { JobProgress } from "../../../../components/element/JobProgress";
import { useJdbcColumns, useJdbcTables } from "../../../../hooks/useJdbc";
import { type JobContext, useJob } from "../../../../hooks/useJob";
import { useTableSelection } from "../../../../hooks/useTableSelection";
import {
	defaultTableAlias,
	isJoin,
	isStatementUsage,
	type StatementUsage,
	statementColumnUsage,
	type TableUsage,
	tableLines,
	usesTableAlias,
} from "../../../../utils/textInsert";
import SqlColumnInsertDialog from "./SqlColumnInsertDialog";
import {
	InsertPreview,
	OptionSelect,
	type SelectGroup,
} from "./SqlInsertParts";
import TableList from "./TableList";

const TABLE_USAGE_GROUPS: SelectGroup<TableUsage>[] = [
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

interface SqlTableInsertDialogProps {
	jdbcValues: Record<string, string>;
	onInsert: (tables: string[]) => void;
	onInsertColumn: (columns: string[]) => void;
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
		const result = await job.run((context) =>
			getJdbcTables(jdbcValues, context),
		);
		if (result) {
			setTables(result.value);
		}
	};

	const queryColumns = (table: string, context: JobContext) =>
		getJdbcColumns(jdbcValues, table, context);

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
	onInsertColumn: (columns: string[]) => void;
	plain: boolean;
	onQueryColumns: (table: string, context: JobContext) => Promise<string[]>;
	onClose: () => void;
}) {
	// plainは名前だけを複数行で挿入する。それ以外は句を組み立てるため1テーブルだけ選ぶ
	const { selected, toggle, toggleOnly, toggleAll } = useTableSelection();
	// 列選択ダイアログの対象テーブルと、読み込めた列（読込中・失敗中はnull）
	// statementはテーブル側の用途から文全体を挿入するときの用途（行の列選択アイコンから開いたときはnull）
	const [columnDialog, setColumnDialog] = useState<{
		table: string;
		columns: string[] | null;
		statement: StatementUsage | null;
	} | null>(null);
	// 列の読込。別のテーブルを選ぶと前回を中断し、閉じても中断する（ダイアログ全体のアンマウントでも中断される）
	const columnJob = useJob();
	const [usage, setUsage] = useState<TableUsage>("select");
	const [alias, setAlias] = useState("");
	const selectedTable = plain
		? null
		: (tables.find((t) => selected.has(t)) ?? null);
	const aliasMissing = isJoin(usage) && alias.trim() === "";

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
		if (isStatementUsage(usage)) {
			openColumnDialog(selectedTable, usage);
			return;
		}
		onInsert(tableLines(selectedTable, usage, alias, []));
	};

	const openColumnDialog = async (
		table: string,
		statement: StatementUsage | null,
	) => {
		if (columnDialog?.table === table && columnJob.running) {
			return;
		}
		setColumnDialog({ table, columns: null, statement });
		const result = await columnJob.run((context) =>
			onQueryColumns(table, context),
		);
		if (result) {
			setColumnDialog({ table, columns: result.value, statement });
		}
	};

	const closeColumnDialog = () => {
		columnJob.cancel();
		setColumnDialog(null);
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
								onChange={setUsage}
							/>
							<div className="flex-1">
								<InputLabel
									text="Table alias"
									id="sqlTableInsert_alias"
									required={isJoin(usage)}
								/>
								<ControllTextBox
									name="sqlTableInsert_alias"
									id="sqlTableInsert_alias"
									required={isJoin(usage)}
									disabled={!usesTableAlias(usage)}
									value={usesTableAlias(usage) ? alias : ""}
									handleChange={(ev) => setAlias(ev.target.value)}
								/>
							</div>
						</div>
					</DialogField>
					<InsertPreview
						lines={
							// 列まで選ぶ用途は、列選択ダイアログで文全体を見せる
							selectedTable === null || isStatementUsage(usage)
								? []
								: tableLines(selectedTable, usage, alias, [])
						}
					/>
				</>
			)}
			<DialogActions>
				<BlueButton
					title={!plain && isStatementUsage(usage) ? "Next" : "Insert"}
					handleClick={handleInsert}
					disabled={selected.size === 0 || (!plain && aliasMissing)}
				/>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
			{columnDialog !== null && (
				<SqlColumnInsertDialog
					table={columnDialog.table}
					columns={columnDialog.columns}
					loading={columnJob.running}
					error={columnJob.error}
					logs={columnJob.logs}
					plain={plain}
					{...columnAliasOf(columnDialog.table)}
					statement={statementOf(columnDialog.table, columnDialog.statement)}
					tables={tables}
					onQueryColumns={onQueryColumns}
					onInsert={(lines) => {
						if (columnDialog.statement === null) {
							onInsertColumn(lines);
						} else {
							onInsert(lines);
						}
						setColumnDialog(null);
					}}
					onClose={closeColumnDialog}
				/>
			)}
		</>
	);
}
