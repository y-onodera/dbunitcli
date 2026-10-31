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
import { decorateLines, qualifyColumn } from "../../../../utils/textInsert";
import SelectableTable from "./SelectableTable";
import TableList from "./TableList";

type AffixOption = { label: string; value: string };

const NONE: AffixOption = { label: "None", value: "" };
const AS: AffixOption = { label: "as", value: " as " };
const COMMA: AffixOption = { label: ",", value: "," };
const TABLE_PREFIXES = [NONE, COMMA];
const TABLE_SUFFIXES = [NONE, COMMA, AS];
const COLUMN_PREFIXES = [NONE, COMMA, { label: "and", value: "and " }];
const COLUMN_SUFFIXES = [NONE, COMMA, AS, { label: "=", value: " = " }];

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
	const { selected, toggle, toggleAll } = useTableSelection();
	// 列選択ダイアログの対象テーブルと、読み込めた列（読込中・失敗中はnull）
	const [columnDialog, setColumnDialog] = useState<{
		table: string;
		columns: string[] | null;
	} | null>(null);
	// 列の読込。別のテーブルを選ぶと前回を中断し、閉じても中断する（ダイアログ全体のアンマウントでも中断される）
	const columnJob = useJob();
	const [prefix, setPrefix] = useState("");
	const [suffix, setSuffix] = useState("");

	const handleInsert = () => {
		const selectedTables = tables.filter((t) => selected.has(t));
		onInsert(decorateLines(selectedTables, prefix, suffix));
	};

	const handleSelectTable = async (table: string) => {
		if (columnDialog?.table === table && columnJob.running) {
			return;
		}
		setColumnDialog({ table, columns: null });
		const result = await columnJob.run((context) =>
			onQueryColumns(table, context),
		);
		if (result) {
			setColumnDialog({ table, columns: result.value });
		}
	};

	const closeColumnDialog = () => {
		columnJob.cancel();
		setColumnDialog(null);
	};

	return (
		<>
			<DialogField last={plain}>
				{tables.length === 0 ? (
					<StatusMessage kind="empty">No tables found</StatusMessage>
				) : (
					<TableList
						tables={tables}
						selected={selected}
						onToggleAll={toggleAll}
						onToggle={toggle}
						onSelectTable={handleSelectTable}
					/>
				)}
			</DialogField>
			{!plain && (
				<AffixSelects
					idPrefix="sqlTableInsert"
					prefixOptions={TABLE_PREFIXES}
					suffixOptions={TABLE_SUFFIXES}
					onPrefixChange={setPrefix}
					onSuffixChange={setSuffix}
				/>
			)}
			<DialogActions>
				<BlueButton
					title="Insert"
					handleClick={handleInsert}
					disabled={selected.size === 0}
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
					onInsert={(columns) => {
						onInsertColumn(columns);
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
	onInsert,
	onClose,
}: {
	table: string;
	columns: string[] | null;
	loading: boolean;
	error: string;
	logs: string[];
	plain: boolean;
	onInsert: (columns: string[]) => void;
	onClose: () => void;
}) {
	const { selected, toggle, toggleAll } = useTableSelection();
	const [prefix, setPrefix] = useState("");
	const [suffix, setSuffix] = useState("");
	const [alias, setAlias] = useState("");

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

	return (
		<ColumnDialogFrame table={table}>
			{!plain && (
				<DialogField>
					<InputLabel
						text="Table alias"
						id="sqlColumnInsert_alias"
						required={false}
					/>
					<ControllTextBox
						name="sqlColumnInsert_alias"
						id="sqlColumnInsert_alias"
						required={false}
						value={alias}
						handleChange={(ev) => setAlias(ev.target.value)}
					/>
				</DialogField>
			)}
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
				<AffixSelects
					idPrefix="sqlColumnInsert"
					prefixOptions={COLUMN_PREFIXES}
					suffixOptions={COLUMN_SUFFIXES}
					onPrefixChange={setPrefix}
					onSuffixChange={setSuffix}
				/>
			)}
			<DialogActions>
				<BlueButton
					title="Insert Columns"
					handleClick={() =>
						onInsert(
							decorateLines(
								columns
									.filter((c) => selected.has(c))
									.map((c) => qualifyColumn(c, alias)),
								prefix,
								suffix,
							),
						)
					}
					disabled={selected.size === 0}
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

function AffixSelects({
	idPrefix,
	prefixOptions,
	suffixOptions,
	onPrefixChange,
	onSuffixChange,
}: {
	idPrefix: string;
	prefixOptions: AffixOption[];
	suffixOptions: AffixOption[];
	onPrefixChange: (value: string) => void;
	onSuffixChange: (value: string) => void;
}) {
	return (
		<DialogField last>
			<div className="flex gap-3">
				<AffixSelect
					id={`${idPrefix}_prefix`}
					label="Prefix"
					options={prefixOptions}
					onChange={onPrefixChange}
				/>
				<AffixSelect
					id={`${idPrefix}_suffix`}
					label="Suffix"
					options={suffixOptions}
					onChange={onSuffixChange}
				/>
			</div>
		</DialogField>
	);
}

function AffixSelect({
	id,
	label,
	options,
	onChange,
}: {
	id: string;
	label: string;
	options: AffixOption[];
	onChange: (value: string) => void;
}) {
	return (
		<div className="flex-1">
			<InputLabel text={label} id={id} required={false} />
			<SelectBox
				name={id}
				id={id}
				required={false}
				wStyle="w-full"
				handleOnChange={onChange}
			>
				{options.map((o) => (
					<option key={o.label} value={o.value}>
						{o.label}
					</option>
				))}
			</SelectBox>
		</div>
	);
}
