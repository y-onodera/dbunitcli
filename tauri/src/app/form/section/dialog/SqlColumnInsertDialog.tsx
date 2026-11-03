import { useState } from "react";
import { DialogActions, DialogField } from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import {
	ControllTextBox,
	InputLabel,
} from "../../../../components/element/Input";
import type { JobContext } from "../../../../hooks/useJob";
import { useTableSelection } from "../../../../hooks/useTableSelection";
import {
	type ColumnUsage,
	type ConditionOperator,
	type Conjunction,
	columnKeyword,
	columnLines,
	columnSeparator,
	conditionLines,
	isConditionUsage,
	isMultiColumnOperator,
	placeScalar,
	qualifyColumn,
	type ScalarPlace,
	type ScalarSource,
	scalarExpression,
	usesColumnQualifier,
	withCoalesce,
} from "../../../../utils/textInsert";
import SelectableTable from "./SelectableTable";
import {
	InsertPreview,
	NestedDialogFrame,
	OptionCheck,
	OptionSelect,
	renderColumnLoadState,
	type SelectGroup,
} from "./SqlInsertParts";
import SqlSubqueryDialog, { CONJUNCTION_GROUPS } from "./SqlSubqueryDialog";

const COLUMN_USAGE_GROUPS: SelectGroup<ColumnUsage>[] = [
	{
		label: "",
		options: [
			{ value: "select", label: "Select" },
			{ value: "orderBy", label: "Order by" },
			{ value: "groupBy", label: "Group by" },
			{ value: "where", label: "Where" },
			{ value: "on", label: "On" },
			{ value: "update", label: "Update set (=, ,)" },
			{ value: "insert", label: "Insert" },
			{ value: "scalar", label: "Scalar value" },
		],
	},
];

const SCALAR_SOURCE_GROUPS: SelectGroup<ScalarSource>[] = [
	{
		label: "",
		options: [
			{ value: "column", label: "Column" },
			{ value: "case", label: "CASE" },
			{ value: "subquery", label: "Subquery" },
		],
	},
];

const SCALAR_PLACE_GROUPS: SelectGroup<ScalarPlace>[] = [
	{
		label: "",
		options: [
			{ value: "select", label: "Select column" },
			{ value: "orderBy", label: "Order by" },
			{ value: "condition", label: "Condition right side" },
		],
	},
];

const OPERATOR_GROUPS: SelectGroup<ConditionOperator>[] = [
	{
		label: "",
		options: [
			{ value: "equals", label: "=" },
			{ value: "isNull", label: "IS NULL" },
			{ value: "in", label: "IN" },
			{ value: "exists", label: "EXISTS" },
		],
	},
];

type InSource = "values" | "subquery";
const IN_SOURCE_GROUPS: SelectGroup<InSource>[] = [
	{
		label: "",
		options: [
			{ value: "values", label: "Values" },
			{ value: "subquery", label: "Subquery" },
		],
	},
];

type Connector = "none" | Conjunction;
const CONNECTOR_GROUPS: SelectGroup<Connector>[] = [
	{
		label: "",
		options: [
			{ value: "none", label: "None (first condition)" },
			{ value: "AND", label: "AND" },
			{ value: "OR", label: "OR" },
		],
	},
];

// サブクエリの「...」は、選ぶ前のプレビューでサブクエリの位置を示す
const SUBQUERY_PLACEHOLDER = ["..."];

export default function SqlColumnInsertDialog({
	table,
	columns,
	loading,
	error,
	logs,
	plain,
	alias,
	qualify: initialQualify,
	statement,
	tables,
	onQueryColumns,
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
	// サブクエリのテーブルの候補と、その列の読込
	tables: string[];
	onQueryColumns: (table: string, context: JobContext) => Promise<string[]>;
	// inlineなら改行を足さずにカーソル位置へ入れる（条件の右辺）
	onInsert: (lines: string[], inline?: boolean) => void;
	onClose: () => void;
}) {
	const { selected, toggle, toggleOnly, toggleAll } = useTableSelection();
	const [selectedUsage, setUsage] = useState<ColumnUsage>("select");
	const usage = statement?.usage ?? selectedUsage;
	const [qualify, setQualify] = useState(initialQualify);
	const [columnAs, setColumnAs] = useState(false);
	const [continued, setContinued] = useState(false);
	const [keyword, setKeyword] = useState(true);
	const [operator, setOperator] = useState<ConditionOperator>("equals");
	const [not, setNot] = useState(false);
	const [conjunction, setConjunction] = useState<Conjunction>("AND");
	const [group, setGroup] = useState(false);
	const [connector, setConnector] = useState<Connector>("none");
	const [inSource, setInSource] = useState<InSource>("values");
	const [inValues, setInValues] = useState("");
	const [subqueryMode, setSubqueryMode] = useState<"in" | "exists" | null>(
		null,
	);
	const [scalarSource, setScalarSource] = useState<ScalarSource>("column");
	const [scalarPlace, setScalarPlace] = useState<ScalarPlace>("select");
	const [whenCount, setWhenCount] = useState("1");
	const [coalesce, setCoalesce] = useState(false);
	const [fallback, setFallback] = useState("");
	const [asName, setAsName] = useState("");

	const loadState = renderColumnLoadState({
		title: "Select Columns",
		table,
		columns,
		loading,
		error,
		logs,
		onClose,
	});
	if (loadState !== null || columns === null) {
		return loadState;
	}

	const condition = !plain && isConditionUsage(usage);
	const conditionOperator = condition ? operator : "equals";
	const scalar = !plain && usage === "scalar";
	const scalarFromSubquery = scalar && scalarSource === "subquery";
	const inline = scalar && scalarPlace === "condition";
	const singleColumn =
		conditionOperator === "in" || (scalar && !scalarFromSubquery);
	const noColumns = conditionOperator === "exists" || scalarFromSubquery;
	const needsSubquery =
		conditionOperator === "exists" ||
		(conditionOperator === "in" && inSource === "subquery") ||
		scalarFromSubquery;
	const qualifying = qualify && usesColumnQualifier(usage) && alias !== "";
	const qualifier = qualifying ? alias : "";
	// 先頭の条件として挿入するときだけキーワード行を付けられる
	const firstCondition = statement !== undefined || connector === "none";

	const linesOf = (targets: string[], subquery: string[] | null) => {
		if (plain) {
			return targets;
		}
		if (scalar) {
			return scalarLines(targets[0] ?? "", subquery);
		}
		const formatted = isConditionUsage(usage)
			? conditionLines(
					targets.map((c) => qualifyColumn(c, qualifier)),
					usage,
					{
						operator: conditionOperator,
						not,
						conjunction,
						group,
						connector: firstCondition ? null : (connector as Conjunction),
						keyword: statement === undefined && keyword,
						inValues,
						subquery,
					},
				)
			: columnLines(targets, usage, {
					alias: qualifier,
					columnAs,
					continued: statement === undefined && continued,
					keyword: statement === undefined && keyword,
				});
		return statement === undefined ? formatted : statement.wrap(formatted);
	};
	const scalarLines = (column: string, subquery: string[] | null) => {
		const expression = scalarExpression(
			scalarSource,
			qualifyColumn(column, qualifier),
			{ whenCount: Number.parseInt(whenCount, 10) || 1, subquery },
		);
		const value = coalesce ? withCoalesce(expression, fallback) : expression;
		if (scalarPlace === "condition") {
			return value;
		}
		return placeScalar(value, scalarPlace, { continued, asName });
	};
	const selectedColumns = columns.filter((c) => selected.has(c));
	const keywordOfUsage = columnKeyword(usage);
	const previewSubquery = needsSubquery ? SUBQUERY_PLACEHOLDER : null;
	const canInsert =
		noColumns ||
		(singleColumn ? selectedColumns.length === 1 : selectedColumns.length > 0);

	// 1列だけ選ぶ指定に変えたら、選択済みの列を外す
	const handleOperatorChange = (next: ConditionOperator) => {
		setOperator(next);
		if (next === "in") {
			toggleAll(columns, false);
		}
	};

	const handleUsageChange = (next: ColumnUsage) => {
		setUsage(next);
		if (next === "scalar") {
			toggleAll(columns, false);
		}
	};

	return (
		<NestedDialogFrame title="Select Columns" table={table}>
			{!plain && (
				<>
					{statement === undefined && (
						<DialogField>
							<OptionSelect
								id="sqlColumnInsert_usage"
								label="Usage"
								groups={COLUMN_USAGE_GROUPS}
								onChange={handleUsageChange}
							/>
						</DialogField>
					)}
					{scalar && (
						<DialogField>
							<div className="flex gap-3">
								<OptionSelect
									id="sqlColumnInsert_scalarSource"
									label="Source"
									groups={SCALAR_SOURCE_GROUPS}
									onChange={setScalarSource}
								/>
								<OptionSelect
									id="sqlColumnInsert_scalarPlace"
									label="Place in"
									groups={SCALAR_PLACE_GROUPS}
									onChange={setScalarPlace}
								/>
							</div>
						</DialogField>
					)}
					{condition && (
						<DialogField>
							<div className="flex gap-3 items-end">
								<OptionSelect
									id="sqlColumnInsert_operator"
									label="Operator"
									groups={OPERATOR_GROUPS}
									onChange={handleOperatorChange}
								/>
								<OptionCheck
									id="sqlColumnInsert_not"
									label="NOT"
									checked={not}
									onChange={setNot}
								/>
							</div>
						</DialogField>
					)}
				</>
			)}
			{!noColumns && (
				<DialogField last={plain}>
					<SelectableTable
						items={columns}
						selected={selected}
						onToggle={singleColumn ? toggleOnly : toggle}
						onToggleAll={singleColumn ? undefined : toggleAll}
						columnLabel="Column Name"
						filterPlaceholder="Filter columns..."
						emptyMessage="No columns found"
					/>
				</DialogField>
			)}
			{!plain && (
				<>
					{scalar && (
						<ScalarOptions
							source={scalarSource}
							place={scalarPlace}
							whenCount={whenCount}
							coalesce={coalesce}
							fallback={fallback}
							asName={asName}
							onWhenCountChange={setWhenCount}
							onCoalesceChange={setCoalesce}
							onFallbackChange={setFallback}
							onAsNameChange={setAsName}
						/>
					)}
					{condition && operator === "in" && (
						<DialogField>
							<div className="flex gap-3">
								<OptionSelect
									id="sqlColumnInsert_inSource"
									label="IN source"
									groups={IN_SOURCE_GROUPS}
									onChange={setInSource}
								/>
								{inSource === "values" && (
									<div className="flex-1">
										<InputLabel
											text="Values"
											id="sqlColumnInsert_inValues"
											required={false}
										/>
										<ControllTextBox
											name="sqlColumnInsert_inValues"
											id="sqlColumnInsert_inValues"
											required={false}
											value={inValues}
											handleChange={(ev) => setInValues(ev.target.value)}
										/>
									</div>
								)}
							</div>
						</DialogField>
					)}
					{condition && isMultiColumnOperator(operator) && (
						<DialogField>
							<div className="flex gap-3 items-end">
								<OptionSelect
									id="sqlColumnInsert_conjunction"
									label="Join columns with"
									groups={CONJUNCTION_GROUPS}
									onChange={setConjunction}
								/>
								<OptionCheck
									id="sqlColumnInsert_group"
									label="Group with ( )"
									checked={group}
									onChange={setGroup}
								/>
							</div>
						</DialogField>
					)}
					{condition && statement === undefined && (
						<DialogField>
							<OptionSelect
								id="sqlColumnInsert_connector"
								label="Connect to main query"
								groups={CONNECTOR_GROUPS}
								onChange={setConnector}
							/>
						</DialogField>
					)}
					{usesColumnQualifier(usage) && alias !== "" && !noColumns && (
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
								checked={keyword && !continued && firstCondition}
								disabled={continued || !firstCondition}
								onChange={setKeyword}
							/>
						</DialogField>
					)}
					{statement === undefined && !condition && !inline && (
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
						lines={canInsert ? linesOf(selectedColumns, previewSubquery) : []}
					/>
				</>
			)}
			<DialogActions>
				{needsSubquery ? (
					<BlueButton
						title="Next"
						handleClick={() =>
							setSubqueryMode(
								condition && operator === "exists" ? "exists" : "in",
							)
						}
						disabled={!canInsert}
					/>
				) : (
					<BlueButton
						title={scalar ? "Insert Scalar" : "Insert Columns"}
						handleClick={() => onInsert(linesOf(selectedColumns, null), inline)}
						disabled={!canInsert}
					/>
				)}
				{!singleColumn && !noColumns && (
					<BlueButton
						title="All Columns"
						handleClick={() => onInsert(linesOf(columns, null))}
						disabled={columns.length === 0}
					/>
				)}
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
			{subqueryMode !== null && (
				<SqlSubqueryDialog
					mode={subqueryMode}
					tables={tables}
					usedAliases={alias === "" ? [] : [alias]}
					onQueryColumns={onQueryColumns}
					onComplete={(subquery) =>
						onInsert(linesOf(selectedColumns, subquery), inline)
					}
					onClose={() => setSubqueryMode(null)}
				/>
			)}
		</NestedDialogFrame>
	);
}

// CASE の WHEN の数、COALESCE の代替値、SELECT に置くときの AS の名前
function ScalarOptions({
	source,
	place,
	whenCount,
	coalesce,
	fallback,
	asName,
	onWhenCountChange,
	onCoalesceChange,
	onFallbackChange,
	onAsNameChange,
}: {
	source: ScalarSource;
	place: ScalarPlace;
	whenCount: string;
	coalesce: boolean;
	fallback: string;
	asName: string;
	onWhenCountChange: (value: string) => void;
	onCoalesceChange: (checked: boolean) => void;
	onFallbackChange: (value: string) => void;
	onAsNameChange: (value: string) => void;
}) {
	return (
		<>
			{source === "case" && (
				<DialogField>
					<InputLabel
						text="WHEN count"
						id="sqlColumnInsert_whenCount"
						required={false}
					/>
					<ControllTextBox
						name="sqlColumnInsert_whenCount"
						id="sqlColumnInsert_whenCount"
						required={false}
						clearable={false}
						value={whenCount}
						handleChange={(ev) =>
							onWhenCountChange(ev.target.value.replace(/[^0-9]/g, ""))
						}
					/>
				</DialogField>
			)}
			<DialogField>
				<OptionCheck
					id="sqlColumnInsert_coalesce"
					label="COALESCE"
					checked={coalesce}
					onChange={onCoalesceChange}
				/>
				{coalesce && (
					<>
						<InputLabel
							text="Fallback value"
							id="sqlColumnInsert_fallback"
							required={false}
						/>
						<ControllTextBox
							name="sqlColumnInsert_fallback"
							id="sqlColumnInsert_fallback"
							required={false}
							value={fallback}
							handleChange={(ev) => onFallbackChange(ev.target.value)}
						/>
					</>
				)}
			</DialogField>
			{place === "select" && (
				<DialogField>
					<InputLabel
						text="AS name"
						id="sqlColumnInsert_asName"
						required={false}
					/>
					<ControllTextBox
						name="sqlColumnInsert_asName"
						id="sqlColumnInsert_asName"
						required={false}
						value={asName}
						handleChange={(ev) => onAsNameChange(ev.target.value)}
					/>
				</DialogField>
			)}
		</>
	);
}
