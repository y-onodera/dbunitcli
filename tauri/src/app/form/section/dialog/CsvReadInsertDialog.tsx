import { type SetStateAction, useState } from "react";
import {
	DialogActions,
	DialogField,
	DialogTitle,
	ModalOverlay,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { useCsvColumnNamesFetcher } from "../../../../hooks/useDatasetSettings";
import type { JobContext } from "../../../../hooks/useJob";
import type { CommandOption } from "../../../../model/CommandOption";
import {
	csvFileAlias,
	csvReadExpression,
	type DerivedTableUsage,
	isJoin,
	statementColumnUsage,
	tableLines,
} from "../../../../utils/textInsert";
import { FileChooser } from "../element/Chooser";
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

// csvq実行時にCSVREADの相対パスを探す基準（datasetBase→workspace）と同じ基準で相対パスにする
const CSV_FILE_ELEMENT: CommandOption = {
	name: "csvFile",
	value: "",
	optional: true,
	attribute: {
		type: "FILE",
		required: false,
		selectOption: [],
		defaultPath: "DATASET",
	},
};

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
	const handleChoose = (value: SetStateAction<string>) => {
		const next = typeof value === "function" ? value(path) : value;
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
			<DialogField>
				<div className="flex gap-2 items-end">
					<OptionText
						id="csvReadInsert_file"
						label="CSV file"
						required
						value={path}
						onChange={setPath}
					/>
					<div className="mb-1">
						<FileChooser
							prefix="csvReadInsert"
							element={CSV_FILE_ELEMENT}
							path={path}
							setPath={handleChoose}
						/>
					</div>
				</div>
			</DialogField>
			<DialogField>
				<OptionText
					id="csvReadInsert_charset"
					label="Charset"
					required
					value={charset}
					onChange={setCharset}
				/>
			</DialogField>
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
					// サブクエリの候補になるテーブルは無い（csvqにDBのテーブル一覧は無い）
					tables={[]}
					onQueryColumns={async () => []}
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
