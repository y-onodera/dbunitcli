import { useState } from "react";
import {
	DialogActions,
	DialogField,
	DialogTitle,
	ModalOverlay,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { useCsvColumnNamesFetcher } from "../../../../hooks/useDatasetSettings";
import type { JobContext } from "../../../../hooks/useJob";
import {
	csvFileAlias,
	csvReadExpression,
	type DerivedTableUsage,
	isJoin,
	statementColumnUsage,
	tableLines,
} from "../../../../utils/textInsert";
import CsvFileFields from "./CsvFileFields";
import SqlColumnInsertDialog from "./SqlColumnInsertDialog";
import {
	InsertPreview,
	OptionSelect,
	OptionText,
	type SelectGroup,
} from "./SqlInsertParts";
import { useColumnLoad } from "./useColumnLoad";

const CSV_USAGE_GROUPS: SelectGroup<DerivedTableUsage>[] = [
	{
		label: "Statement",
		options: [{ value: "select", label: "Select" }],
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

export default function CsvReadInsertDialog({
	encoding,
	onInsert,
	onClose,
}: {
	// 文字コードの初期値
	encoding: string;
	onInsert: (lines: string[]) => void;
	onClose: () => void;
}) {
	const [path, setPath] = useState("");
	const [charset, setCharset] = useState(encoding);
	const [usage, setUsage] = useState<DerivedTableUsage>("select");
	const [alias, setAlias] = useState("");
	const fetchCsvColumnNames = useCsvColumnNamesFetcher();
	const columnLoad = useColumnLoad((_table: string, context: JobContext) =>
		fetchCsvColumnNames(path.trim(), charset.trim(), context),
	);
	const expression = csvReadExpression(path, charset);
	const canProceed =
		path.trim() !== "" &&
		charset.trim() !== "" &&
		(!isJoin(usage) || alias.trim() !== "");

	// ファイルを選び直したら、別名をファイル名から付け直す
	const handleChoose = (next: string) => {
		setPath(next);
		setAlias(csvFileAlias(next));
	};

	const handleNext = () => {
		if (usage === "crossJoin") {
			onInsert(tableLines(expression, usage, alias, []));
			return;
		}
		columnLoad.open(expression);
	};

	return (
		<ModalOverlay>
			<DialogTitle>Insert CSVREAD</DialogTitle>
			<CsvFileFields
				idPrefix="csvReadInsert"
				path={path}
				charset={charset}
				onPathChange={setPath}
				onChoose={handleChoose}
				onCharsetChange={setCharset}
			/>
			<DialogField>
				<div className="flex gap-3">
					<OptionSelect
						id="csvReadInsert_usage"
						label="Usage"
						groups={CSV_USAGE_GROUPS}
						onChange={setUsage}
					/>
					<OptionText
						id="csvReadInsert_alias"
						label="Table alias"
						required={isJoin(usage)}
						value={alias}
						onChange={setAlias}
					/>
				</div>
			</DialogField>
			<InsertPreview
				lines={
					// 列まで選ぶ用途は、列選択ダイアログで文全体を見せる
					canProceed && usage === "crossJoin"
						? tableLines(expression, usage, alias, [])
						: []
				}
			/>
			<DialogActions>
				<BlueButton
					title={usage === "crossJoin" ? "Insert" : "Next"}
					handleClick={handleNext}
					disabled={!canProceed}
				/>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
			{columnLoad.target !== null && usage !== "crossJoin" && (
				<SqlColumnInsertDialog
					table={columnLoad.target.table}
					columns={columnLoad.target.columns}
					loading={columnLoad.job.running}
					error={columnLoad.job.error}
					logs={columnLoad.job.logs}
					plain={false}
					alias={alias.trim()}
					qualify={alias.trim() !== ""}
					statement={{
						usage: statementColumnUsage(usage),
						wrap: (lines) =>
							tableLines(columnLoad.target?.table ?? "", usage, alias, lines),
					}}
					subquerySource={{ kind: "csv", encoding: charset }}
					onInsert={(lines) => {
						columnLoad.close();
						onInsert(lines);
					}}
					onClose={columnLoad.close}
				/>
			)}
		</ModalOverlay>
	);
}
