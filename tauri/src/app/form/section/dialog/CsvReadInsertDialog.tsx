import { useState } from "react";
import {
	DialogActions,
	DialogField,
	DialogTitle,
	ModalOverlay,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import {
	csvFileAlias,
	type DerivedTableUsage,
	isJoin,
	statementColumnUsage,
	tableLines,
	usesDerivedTable,
} from "../../../../utils/textInsert";
import CsvFileFields from "./CsvFileFields";
import SqlColumnInsertDialog from "./SqlColumnInsertDialog";
import {
	InsertPreview,
	OptionSelect,
	OptionText,
	type SelectGroup,
} from "./SqlInsertParts";
import { TABLE_USAGE_GROUPS } from "./SqlTableInsertDialog";
import { useCsvFile } from "./useCsvFile";

// CSVは書き込めないため、Select Tablesの用途のうちFROM / JOINに置くものだけ
const CSV_USAGE_GROUPS: SelectGroup<DerivedTableUsage>[] =
	TABLE_USAGE_GROUPS.map((group) => ({
		label: group.label,
		options: group.options.filter(
			(o): o is { value: DerivedTableUsage; label: string } =>
				usesDerivedTable(o.value),
		),
	}));

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
	const csv = useCsvFile(encoding, csvFileAlias);
	const { alias, expression, columnLoad } = csv;
	const [usage, setUsage] = useState<DerivedTableUsage>("select");
	const crossJoin = usage === "crossJoin";
	const crossJoinLines = tableLines(expression, usage, alias, []);
	const canProceed = csv.ready && (!isJoin(usage) || alias.trim() !== "");

	const handleNext = () => {
		if (crossJoin) {
			onInsert(crossJoinLines);
			return;
		}
		columnLoad.open(expression);
	};

	return (
		<ModalOverlay>
			<DialogTitle>Insert CSVREAD</DialogTitle>
			<CsvFileFields idPrefix="csvReadInsert" {...csv.fields} />
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
						onChange={csv.setAlias}
					/>
				</div>
			</DialogField>
			<InsertPreview
				// 列まで選ぶ用途は、列選択ダイアログで文全体を見せる
				lines={canProceed && crossJoin ? crossJoinLines : []}
			/>
			<DialogActions>
				<BlueButton
					title={crossJoin ? "Insert" : "Next"}
					handleClick={handleNext}
					disabled={!canProceed}
				/>
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
			{columnLoad.target !== null && !crossJoin && (
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
						wrap: (lines) => tableLines(expression, usage, alias, lines),
					}}
					subquerySource={{ kind: "csv", encoding: csv.charset }}
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
