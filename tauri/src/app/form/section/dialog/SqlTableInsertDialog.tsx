import { useRef, useState } from "react";
import {
	DialogActions,
	DialogField,
	DialogTitle,
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
import { useAbortSignalRef } from "../../../../hooks/useAbortSignalRef";
import {
	type JdbcFetchOptions,
	useJdbcColumns,
	useJdbcTables,
} from "../../../../hooks/useJdbc";
import { useJobLog } from "../../../../hooks/useJobLog";
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
	onInsertColumn?: (columns: string[]) => void;
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
	const [loading, setLoading] = useState(false);
	const jobLog = useJobLog();
	const abortControllerRef = useAbortSignalRef();
	const getJdbcTables = useJdbcTables();
	const getJdbcColumns = useJdbcColumns();

	// 閉じると（アンマウントでabort）sidecar側の読込も中断する
	const handleLoad = () => {
		if (loading) {
			return;
		}
		setLoading(true);
		jobLog.reset();
		getJdbcTables(jdbcValues, {
			signal: abortControllerRef.current?.signal,
			onLog: jobLog.append,
		}).then((result) => {
			if (!abortControllerRef.current?.signal.aborted) {
				setTables(result);
				setLoading(false);
			}
		});
	};

	const queryColumns = (
		table: string,
		fetchOptions?: JdbcFetchOptions,
	): Promise<string[]> => getJdbcColumns(jdbcValues, table, fetchOptions);

	function renderBody() {
		if (loading) {
			return (
				<JobProgress
					title="Loading..."
					lines={jobLog.logs}
					onAbandon={onClose}
				/>
			);
		}
		if (tables !== null) {
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
			<DialogActions>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
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
	onInsertColumn?: (columns: string[]) => void;
	plain: boolean;
	onQueryColumns?: (
		table: string,
		fetchOptions?: JdbcFetchOptions,
	) => Promise<string[]>;
	onClose: () => void;
}) {
	const { selected, toggle, toggleAll } = useTableSelection();
	const [columnDialog, setColumnDialog] = useState<{
		table: string;
		data: string[] | "loading";
	} | null>(null);
	const abortControllerRef = useAbortSignalRef();
	// 列の読込1回分。閉じたら中断する（ダイアログ全体のアンマウントでも中断される）
	const columnAbortRef = useRef<AbortController | null>(null);
	const columnLog = useJobLog();
	const [prefix, setPrefix] = useState("");
	const [suffix, setSuffix] = useState("");

	const handleInsert = () => {
		const selectedTables = tables.filter((t) => selected.has(t));
		onInsert(decorateLines(selectedTables, prefix, suffix));
	};

	const handleSelectTable = (table: string) => {
		if (columnDialog?.table === table && columnDialog.data === "loading") {
			return;
		}
		if (!abortControllerRef.current) {
			return;
		}
		columnAbortRef.current?.abort();
		const controller = new AbortController();
		columnAbortRef.current = controller;
		const signal = AbortSignal.any([
			abortControllerRef.current.signal,
			controller.signal,
		]);
		setColumnDialog({ table, data: "loading" });
		columnLog.reset();
		onQueryColumns?.(table, { signal, onLog: columnLog.append }).then(
			(columns) => {
				if (!signal.aborted) {
					setColumnDialog((prev) =>
						prev?.table === table ? { table, data: columns } : prev,
					);
				}
			},
			() => {
				if (!signal.aborted) {
					setColumnDialog(null);
				}
			},
		);
	};

	const closeColumnDialog = () => {
		columnAbortRef.current?.abort();
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
						onQueryColumns={onInsertColumn ? undefined : onQueryColumns}
						onSelectTable={onInsertColumn ? handleSelectTable : undefined}
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
			{columnDialog !== null && onInsertColumn && (
				<ColumnSelectDialog
					table={columnDialog.table}
					columnData={columnDialog.data}
					logs={columnLog.logs}
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
	columnData,
	logs,
	plain,
	onInsert,
	onClose,
}: {
	table: string;
	columnData: string[] | "loading";
	logs: string[];
	plain: boolean;
	onInsert: (columns: string[]) => void;
	onClose: () => void;
}) {
	const { selected, toggle, toggleAll } = useTableSelection();
	const [prefix, setPrefix] = useState("");
	const [suffix, setSuffix] = useState("");
	const [alias, setAlias] = useState("");

	if (columnData === "loading") {
		return (
			<ModalOverlay width="w-80" zClass="z-modal-nested">
				<JobProgress title="Loading..." lines={logs} onAbandon={onClose} />
			</ModalOverlay>
		);
	}

	return (
		<ModalOverlay width="w-80" zClass="z-modal-nested">
			<DialogTitle>Select Columns</DialogTitle>
			<p className="text-caption mb-4 truncate" title={table}>
				{table}
			</p>
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
					items={columnData}
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
								columnData
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
