import { type ReactNode, useState } from "react";
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
	SelectBox,
} from "../../../../components/element/Input";
import { JobProgress } from "../../../../components/element/JobProgress";
import { useJdbcColumns, useJdbcTables } from "../../../../hooks/useJdbc";
import { type JobContext, useJob } from "../../../../hooks/useJob";
import { useTableSelection } from "../../../../hooks/useTableSelection";
import {
	type ColumnUsage,
	columnKeyword,
	columnLines,
	columnSeparator,
	defaultTableAlias,
	isJoin,
	isStatementUsage,
	type StatementUsage,
	statementColumnUsage,
	type TableUsage,
	tableLines,
	usesColumnQualifier,
	usesTableAlias,
} from "../../../../utils/textInsert";
import SelectableTable from "./SelectableTable";
import TableList from "./TableList";

const TABLE_USAGE_GROUPS: UsageGroup<TableUsage>[] = [
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

const COLUMN_USAGE_GROUPS: UsageGroup<ColumnUsage>[] = [
	{
		label: "Usage",
		options: [
			{ value: "select", label: "Select" },
			{ value: "orderBy", label: "Order by" },
			{ value: "groupBy", label: "Group by" },
			{ value: "where", label: "Where (=, AND)" },
			{ value: "on", label: "On (=, AND)" },
			{ value: "update", label: "Update set (=, ,)" },
			{ value: "insert", label: "Insert" },
		],
	},
];

type UsageGroup<T extends string> = {
	label: string;
	options: { value: T; label: string }[];
};

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
							<div className="flex-1">
								<UsageSelect
									id="sqlTableInsert_usage"
									groups={TABLE_USAGE_GROUPS}
									onChange={setUsage}
								/>
							</div>
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
				<ColumnSelectDialog
					table={columnDialog.table}
					columns={columnDialog.columns}
					loading={columnJob.running}
					error={columnJob.error}
					logs={columnJob.logs}
					plain={plain}
					{...columnAliasOf(columnDialog.table)}
					statement={statementOf(columnDialog.table, columnDialog.statement)}
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

function ColumnSelectDialog({
	table,
	columns,
	loading,
	error,
	logs,
	plain,
	alias,
	qualify: initialQualify,
	statement,
	onInsert,
	onClose,
}: {
	table: string;
	columns: string[] | null;
	loading: boolean;
	error: string;
	logs: string[];
	plain: boolean;
	// 修飾に使う別名（入力はSelect Tables側）と、修飾の初期状態
	alias: string;
	qualify: boolean;
	// 指定時は用途を固定し、選んだ列をwrapで文全体にして挿入する
	statement?: { usage: ColumnUsage; wrap: (lines: string[]) => string[] };
	onInsert: (lines: string[]) => void;
	onClose: () => void;
}) {
	const { selected, toggle, toggleAll } = useTableSelection();
	const [selectedUsage, setUsage] = useState<ColumnUsage>("select");
	const usage = statement?.usage ?? selectedUsage;
	const [qualify, setQualify] = useState(initialQualify);
	const [columnAs, setColumnAs] = useState(false);
	const [continued, setContinued] = useState(false);
	const [keyword, setKeyword] = useState(true);

	if (loading) {
		return (
			<ModalOverlay width="w-80" zClass="z-modal-nested">
				<JobProgress title="Loading..." lines={logs} onAbandon={onClose} />
			</ModalOverlay>
		);
	}

	if (error || columns === null) {
		return (
			<ColumnDialogFrame table={table}>
				{error && <JobFailure message={error} lines={logs} />}
				<DialogActions>
					<WhiteButton title="Close" handleClick={onClose} />
				</DialogActions>
			</ColumnDialogFrame>
		);
	}

	const qualifying = qualify && usesColumnQualifier(usage) && alias !== "";
	const linesOf = (targets: string[]) => {
		if (plain) {
			return targets;
		}
		const formatted = columnLines(targets, usage, {
			alias: qualifying ? alias : "",
			columnAs,
			continued: statement === undefined && continued,
			keyword: statement === undefined && keyword,
		});
		return statement === undefined ? formatted : statement.wrap(formatted);
	};
	const selectedColumns = columns.filter((c) => selected.has(c));
	const keywordOfUsage = columnKeyword(usage);

	return (
		<ColumnDialogFrame table={table}>
			<DialogField last={plain}>
				<SelectableTable
					items={columns}
					selected={selected}
					onToggle={toggle}
					onToggleAll={toggleAll}
					columnLabel="Column Name"
					filterPlaceholder="Filter columns..."
					emptyMessage="No columns found"
				/>
			</DialogField>
			{!plain && (
				<>
					{statement === undefined && (
						<DialogField>
							<UsageSelect
								id="sqlColumnInsert_usage"
								groups={COLUMN_USAGE_GROUPS}
								onChange={setUsage}
							/>
						</DialogField>
					)}
					{usesColumnQualifier(usage) && alias !== "" && (
						<DialogField>
							<OptionCheck
								id="sqlColumnInsert_qualify"
								label={`Prefix table alias (${alias})`}
								checked={qualify}
								onChange={setQualify}
							/>
						</DialogField>
					)}
					{usage === "select" && (
						<DialogField>
							<OptionCheck
								id="sqlColumnInsert_as"
								label="AS column name"
								checked={columnAs}
								onChange={setColumnAs}
							/>
						</DialogField>
					)}
					{statement === undefined && keywordOfUsage !== null && (
						<DialogField>
							<OptionCheck
								id="sqlColumnInsert_keyword"
								label={`Keyword line (${keywordOfUsage})`}
								checked={keyword && !continued}
								disabled={continued}
								onChange={setKeyword}
							/>
						</DialogField>
					)}
					{statement === undefined && (
						<DialogField>
							<OptionCheck
								id="sqlColumnInsert_continued"
								label={`Separator on first line (${columnSeparator(usage)})`}
								checked={continued}
								onChange={setContinued}
							/>
						</DialogField>
					)}
					<InsertPreview
						lines={selectedColumns.length === 0 ? [] : linesOf(selectedColumns)}
					/>
				</>
			)}
			<DialogActions>
				<BlueButton
					title="Insert Columns"
					handleClick={() => onInsert(linesOf(selectedColumns))}
					disabled={selectedColumns.length === 0}
				/>
				<BlueButton
					title="All Columns"
					handleClick={() => onInsert(linesOf(columns))}
					disabled={columns.length === 0}
				/>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
		</ColumnDialogFrame>
	);
}

// 列選択ダイアログの枠: 見出しと対象のテーブル名
function ColumnDialogFrame({
	table,
	children,
}: {
	table: string;
	children: ReactNode;
}) {
	return (
		<ModalOverlay width="w-80" zClass="z-modal-nested">
			<DialogTitle>Select Columns</DialogTitle>
			<p className="text-caption mb-4 truncate" title={table}>
				{table}
			</p>
			{children}
		</ModalOverlay>
	);
}

function UsageSelect<T extends string>({
	id,
	groups,
	onChange,
}: {
	id: string;
	groups: UsageGroup<T>[];
	onChange: (value: T) => void;
}) {
	return (
		<>
			<InputLabel text="Usage" id={id} required={false} />
			<SelectBox
				name={id}
				id={id}
				required={false}
				wStyle="w-full"
				defaultValue={groups[0].options[0].value}
				handleOnChange={(value) => onChange(value as T)}
			>
				{groups.map((group) => (
					<optgroup key={group.label} label={group.label}>
						{group.options.map((o) => (
							<option key={o.value} value={o.value}>
								{o.label}
							</option>
						))}
					</optgroup>
				))}
			</SelectBox>
		</>
	);
}

function OptionCheck({
	id,
	label,
	checked,
	disabled = false,
	onChange,
}: {
	id: string;
	label: string;
	checked: boolean;
	disabled?: boolean;
	onChange: (checked: boolean) => void;
}) {
	return (
		<label htmlFor={id} className="flex items-center gap-1 mb-1 text-sm">
			<input
				id={id}
				type="checkbox"
				checked={checked}
				disabled={disabled}
				onChange={(e) => onChange(e.target.checked)}
				className="w-4 h-4 text-primary"
			/>
			{label}
		</label>
	);
}

// 挿入される内容を先に見せ、用途の選び間違いに気付けるようにする
function InsertPreview({ lines }: { lines: string[] }) {
	if (lines.length === 0) {
		return null;
	}
	return (
		<DialogField last>
			<pre
				data-testid="insert-preview"
				className="text-caption whitespace-pre-wrap break-all"
			>
				{lines.join("\n")}
			</pre>
		</DialogField>
	);
}
