import { useState } from "react";
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
import { useAbortSignalRef } from "../../../../hooks/useAbortSignalRef";
import { useJdbcColumns, useJdbcTables } from "../../../../hooks/useJdbc";
import { useTableSelection } from "../../../../hooks/useTableSelection";
import { decorateLines, qualifyColumn } from "../../../../utils/textInsert";
import SelectableTable from "./SelectableTable";
import TableList from "./TableList";

type AffixOption = { label: string; value: string };

const NONE: AffixOption = { label: "None", value: "" };
const COMMA: AffixOption = { label: ",", value: "," };
const TABLE_PREFIXES = [NONE, COMMA];
const TABLE_SUFFIXES = [NONE, COMMA];
const COLUMN_PREFIXES = [NONE, COMMA, { label: "and", value: "and " }];
const COLUMN_SUFFIXES = [NONE, COMMA, { label: "=", value: " = " }];

interface SqlTableInsertDialogProps {
	jdbcValues: Record<string, string>;
	onInsert: (tables: string[]) => void;
	onInsertColumn?: (columns: string[]) => void;
	onClose: () => void;
}

export default function SqlTableInsertDialog({
	jdbcValues,
	onInsert,
	onInsertColumn,
	onClose,
}: SqlTableInsertDialogProps) {
	const [tables, setTables] = useState<string[] | null>(null);
	const [loading, setLoading] = useState(false);
	const getJdbcTables = useJdbcTables();
	const getJdbcColumns = useJdbcColumns();

	const handleLoad = () => {
		if (loading) {
			return;
		}
		setLoading(true);
		getJdbcTables(jdbcValues).then((result) => {
			setTables(result);
			setLoading(false);
		});
	};

	const queryColumns = (table: string): Promise<string[]> =>
		getJdbcColumns(jdbcValues, table);

	function renderBody() {
		if (loading) {
			return (
				<>
					<StatusMessage kind="loading">Loading...</StatusMessage>
					<DialogActions>
						<WhiteButton title="Cancel" handleClick={onClose} />
					</DialogActions>
				</>
			);
		}
		if (tables !== null) {
			return (
				<TablesContent
					tables={tables}
					onInsert={onInsert}
					onInsertColumn={onInsertColumn}
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
	onQueryColumns,
	onClose,
}: {
	tables: string[];
	onInsert: (tables: string[]) => void;
	onInsertColumn?: (columns: string[]) => void;
	onQueryColumns?: (table: string) => Promise<string[]>;
	onClose: () => void;
}) {
	const { selected, toggle, toggleAll } = useTableSelection();
	const [columnDialog, setColumnDialog] = useState<{
		table: string;
		data: string[] | "loading";
	} | null>(null);
	const abortControllerRef = useAbortSignalRef();
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
		setColumnDialog({ table, data: "loading" });
		onQueryColumns?.(table).then(
			(columns) => {
				if (!abortControllerRef.current?.signal.aborted) {
					setColumnDialog((prev) =>
						prev?.table === table ? { table, data: columns } : prev,
					);
				}
			},
			() => {
				if (!abortControllerRef.current?.signal.aborted) {
					setColumnDialog(null);
				}
			},
		);
	};

	return (
		<>
			<DialogField>
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
			<AffixSelects
				idPrefix="sqlTableInsert"
				prefixOptions={TABLE_PREFIXES}
				suffixOptions={TABLE_SUFFIXES}
				onPrefixChange={setPrefix}
				onSuffixChange={setSuffix}
			/>
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
					onInsert={(columns) => {
						onInsertColumn(columns);
						setColumnDialog(null);
					}}
					onClose={() => setColumnDialog(null)}
				/>
			)}
		</>
	);
}

function ColumnSelectDialog({
	table,
	columnData,
	onInsert,
	onClose,
}: {
	table: string;
	columnData: string[] | "loading";
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
				<StatusMessage kind="loading">Loading...</StatusMessage>
				<DialogActions>
					<WhiteButton title="Cancel" handleClick={onClose} />
				</DialogActions>
			</ModalOverlay>
		);
	}

	return (
		<ModalOverlay width="w-80" zClass="z-modal-nested">
			<DialogTitle>Select Columns</DialogTitle>
			<p className="text-caption mb-4 truncate" title={table}>
				{table}
			</p>
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
			<DialogField>
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
			<AffixSelects
				idPrefix="sqlColumnInsert"
				prefixOptions={COLUMN_PREFIXES}
				suffixOptions={COLUMN_SUFFIXES}
				onPrefixChange={setPrefix}
				onSuffixChange={setSuffix}
			/>
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
