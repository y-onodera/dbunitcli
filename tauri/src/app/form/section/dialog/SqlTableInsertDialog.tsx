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
	defaultTableAlias,
	type TableUsage,
	usesTableAlias,
} from "../../../../utils/textInsert";
import SqlColumnInsertDialog from "./SqlColumnInsertDialog";
import type { SubquerySource } from "./SqlSubqueryDialog";
import TableInsertForm, {
	QUERY_TABLE_USAGE_GROUPS,
	TABLE_USAGE_GROUPS,
} from "./TableInsertForm";
import TableList from "./TableList";
import { useColumnLoad } from "./useColumnLoad";

interface SqlTableInsertDialogProps {
	jdbcValues: Record<string, string>;
	onInsert: (lines: string[], inline?: boolean) => void;
	// inlineなら改行を足さずにカーソル位置へ入れる
	onInsertColumn: (columns: string[], inline?: boolean) => void;
	plain?: boolean;
	// テーブル・列の用途からDMLを除く
	queryOnly?: boolean;
	onClose: () => void;
}

export default function SqlTableInsertDialog({
	jdbcValues,
	onInsert,
	onInsertColumn,
	plain = false,
	queryOnly = false,
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
				queryOnly={queryOnly}
				onQueryColumns={queryColumns}
				onClose={onClose}
			/>
		);
	}

	return (
		<ModalOverlay onClose={onClose}>
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
	queryOnly,
	onQueryColumns,
	onClose,
}: {
	tables: string[];
	onInsert: (lines: string[], inline?: boolean) => void;
	onInsertColumn: (columns: string[], inline?: boolean) => void;
	plain: boolean;
	queryOnly: boolean;
	onQueryColumns: (table: string, context: JobContext) => Promise<string[]>;
	onClose: () => void;
}) {
	// plainは名前だけを複数行で挿入する。それ以外は句を組み立てるため1テーブルだけ選ぶ
	const { selected, toggle, toggleOnly, toggleAll } = useTableSelection();
	// 行の列選択アイコンから開く列選択ダイアログ（用途は列選択ダイアログで選ぶ）
	const columnLoad = useColumnLoad(onQueryColumns);
	const [usage, setUsage] = useState<TableUsage>("select");
	const [alias, setAlias] = useState("");
	const subquerySource: SubquerySource = {
		kind: "tables",
		tables,
		onQueryColumns,
	};
	const selectedTable = tables.find((t) => selected.has(t)) ?? null;

	const handleToggle = (table: string) => {
		if (plain) {
			toggle(table);
			return;
		}
		toggleOnly(table);
		setAlias(selected.has(table) ? "" : defaultTableAlias(table));
	};

	// 選択中のテーブルなら入力済みの別名で修飾し（空なら修飾しない）、それ以外はデフォルトの別名を候補にする
	const columnAliasOf = (table: string) =>
		table === selectedTable && !plain && usesTableAlias(usage)
			? { alias: alias.trim(), qualify: alias.trim() !== "" }
			: { alias: defaultTableAlias(table), qualify: false };

	const list = (
		<DialogField last={plain}>
			{tables.length === 0 ? (
				<StatusMessage kind="empty">No tables found</StatusMessage>
			) : (
				<TableList
					tables={tables}
					selected={selected}
					onToggleAll={plain ? toggleAll : undefined}
					onToggle={handleToggle}
					onSelectTable={columnLoad.open}
				/>
			)}
		</DialogField>
	);

	return (
		<>
			{plain ? (
				<>
					{list}
					<DialogActions>
						<BlueButton
							title="Insert"
							handleClick={() =>
								onInsert(tables.filter((t) => selected.has(t)))
							}
							disabled={selected.size === 0}
						/>
						<WhiteButton title="Cancel" handleClick={onClose} />
					</DialogActions>
				</>
			) : (
				<TableInsertForm
					idPrefix="sqlTableInsert"
					table={selectedTable}
					usage={usage}
					onUsageChange={setUsage}
					alias={alias}
					onAliasChange={setAlias}
					usageGroups={
						queryOnly ? QUERY_TABLE_USAGE_GROUPS : TABLE_USAGE_GROUPS
					}
					subquerySource={subquerySource}
					queryOnly={queryOnly}
					onInsert={onInsert}
					onClose={onClose}
				>
					{list}
				</TableInsertForm>
			)}
			{columnLoad.target !== null && (
				<SqlColumnInsertDialog
					table={columnLoad.target.table}
					columns={columnLoad.target.columns}
					loading={columnLoad.job.running}
					error={columnLoad.job.error}
					logs={columnLoad.job.logs}
					plain={plain}
					{...columnAliasOf(columnLoad.target.table)}
					subquerySource={subquerySource}
					queryOnly={queryOnly}
					onInsert={(lines, inline) => {
						if (inline) {
							onInsertColumn(lines, true);
						} else {
							onInsertColumn(lines);
						}
						columnLoad.close();
					}}
					onClose={columnLoad.close}
				/>
			)}
		</>
	);
}
