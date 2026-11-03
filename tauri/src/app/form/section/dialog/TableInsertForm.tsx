import { useState } from "react";
import { DialogActions, DialogField } from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import {
	type DerivedTableUsage,
	derivedTableLines,
	isJoin,
	isStatementUsage,
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

// 選んだテーブル1つの用途・別名を決め、句（列まで選ぶ用途は列も選んで文全体）を挿入する。
// テーブルを選ぶ画面は呼び出し側で、別名のデフォルトもそこで付ける。
// 用途と別名は、呼び出し側の列選択（テーブル一覧の行の列選択アイコン）でも使うため呼び出し側が持つ
export default function TableInsertForm({
	idPrefix,
	table,
	usage,
	onUsageChange,
	alias,
	onAliasChange,
	usageGroups = TABLE_USAGE_GROUPS,
	subquerySource,
	onInsert,
	onClose,
}: {
	idPrefix: string;
	// 選んでいなければnull
	table: string | null;
	usage: TableUsage;
	onUsageChange: (value: TableUsage) => void;
	alias: string;
	onAliasChange: (value: string) => void;
	usageGroups?: SelectGroup<TableUsage>[];
	// tableの選び方と列の取り方。サブクエリのテーブルも同じ選び方で選ぶ
	subquerySource: SubquerySource;
	onInsert: (lines: string[]) => void;
	onClose: () => void;
}) {
	// 列まで選ぶ用途のときの、選んだテーブルの列の読込
	const columnLoad = useColumnLoad(subquerySource.onQueryColumns);
	const [source, setSource] = useState<TableSource>("table");
	// サブクエリにするときの、サブクエリ全体の別名（選んだテーブルの別名はalias）
	const [subqueryAlias, setSubqueryAlias] = useState("");
	// サブクエリにするとき: サブクエリで使う列の読込と、その後の外側の列の選択
	const derivedLoad = useColumnLoad(subquerySource.onQueryColumns);
	const [derivedOuter, setDerivedOuter] = useState<{
		usage: Exclude<DerivedTableUsage, "crossJoin">;
		subquery: string[];
		columns: string[];
	} | null>(null);
	const derived = usesDerivedTable(usage) && source === "subquery";
	const aliasMissing = derived
		? subqueryAlias.trim() === ""
		: isJoin(usage) && alias.trim() === "";
	const statement = isStatementUsage(usage) ? usage : null;
	const columnTarget = columnLoad.target;

	const handleUsageChange = (next: TableUsage) => {
		onUsageChange(next);
		setSource("table");
	};

	const handleInsert = () => {
		if (table === null) {
			return;
		}
		if (derived) {
			derivedLoad.open(table);
			return;
		}
		if (statement !== null) {
			columnLoad.open(table);
			return;
		}
		onInsert(tableLines(table, usage, alias, []));
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

	return (
		<>
			<DialogField>
				<div className="flex gap-3">
					<OptionSelect
						id={`${idPrefix}_usage`}
						label="Usage"
						groups={usageGroups}
						onChange={handleUsageChange}
					/>
					<OptionText
						id={`${idPrefix}_alias`}
						label="Table alias"
						required={isJoin(usage) && !derived}
						disabled={!usesTableAlias(usage)}
						value={usesTableAlias(usage) ? alias : ""}
						onChange={onAliasChange}
					/>
				</div>
			</DialogField>
			{usesDerivedTable(usage) && (
				<DialogField>
					<div className="flex gap-3">
						<OptionSelect
							key={usage}
							id={`${idPrefix}_source`}
							label="Source"
							options={TABLE_SOURCE_OPTIONS}
							onChange={setSource}
						/>
						{derived && (
							<OptionText
								id={`${idPrefix}_subqueryAlias`}
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
					table === null || statement !== null || derived
						? []
						: tableLines(table, usage, alias, [])
				}
			/>
			<DialogActions>
				<BlueButton
					title={statement !== null || derived ? "Next" : "Insert"}
					handleClick={handleInsert}
					disabled={table === null || aliasMissing}
				/>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
			{columnTarget !== null && statement !== null && (
				<SqlColumnInsertDialog
					table={columnTarget.table}
					columns={columnTarget.columns}
					loading={columnLoad.job.running}
					error={columnLoad.job.error}
					logs={columnLoad.job.logs}
					plain={false}
					alias={alias.trim()}
					qualify={alias.trim() !== ""}
					statement={{
						usage: statementColumnUsage(statement),
						wrap: (lines) =>
							tableLines(columnTarget.table, statement, alias, lines),
					}}
					subquerySource={subquerySource}
					onInsert={(lines) => {
						onInsert(lines);
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
