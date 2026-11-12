import { useState } from "react";
import {
	DialogField,
	InlineActions,
	JobFailure,
	StatusMessage,
} from "../../../../components/dialog";
import { BlueButton } from "../../../../components/element/Button";
import {
	AddButton,
	RemoveButton,
} from "../../../../components/element/ButtonIcon";
import { InputLabel } from "../../../../components/element/Input";
import { JobProgress } from "../../../../components/element/JobProgress";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useSetResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import {
	useColumnNamesFetcher,
	useDatasetTableNames,
} from "../../../../hooks/useDatasetSettings";
import { useJob } from "../../../../hooks/useJob";
import { withAllTables } from "../../../../model/CommandOption";
import type { GenerateOptions } from "../../../../model/SelectParameter";
import {
	buildCompareTemplateRequest,
	type ColumnPair,
	type DatasetColumns,
	EMPTY_DATASET_COLUMNS,
	selectTable,
} from "../../../../model/XlsxCompareTemplate";
import { removeArray, replaceArray } from "../../../../utils/arrayUtils";
import { DatasetLoadForm, DatasetSrcInfoScope } from "../DatasetLoadForm";
import { RefreshableDialogShell } from "./RefreshableDialogShell";
import ResourceEditButton, {
	type ResourceEditButtonProp,
} from "./ResourceEditButton";
import { postDialogForm } from "./refreshableDialogForm";
import SelectableTable from "./SelectableTable";
import {
	OptionCheck,
	OptionSelect,
	OptionText,
	type SelectOption,
} from "./SqlInsertParts";
import { useRefreshableDialog } from "./useRefreshableDialog";
import { toSavedFileName } from "./XlsxTemplateGenerateDialog";

const formId = "xlsxCompareTemplateGenerateForm";
const fileNameField = "-fileName";
const DEFAULT_COMPARE_SHEET_NAME = "compare";
const EMPTY_PAIR: ColumnPair = { first: "", second: "" };

function columnOptions(columns: string[]): SelectOption<string>[] {
	return [
		{ value: "", label: "" },
		...columns.map((column) => ({ value: column, label: column })),
	];
}

// 1行1項目の入力欄
function TextField({
	name,
	value,
	onChange,
}: {
	name: string;
	value: string;
	onChange: (value: string) => void;
}) {
	return (
		<DialogField>
			<OptionText
				id={name}
				label={name}
				value={value}
				required={true}
				onChange={onChange}
			/>
		</DialogField>
	);
}

function renderColumnsState(
	value: DatasetColumns,
	job: { running: boolean; error: string; logs: string[] },
) {
	if (job.running) {
		return <JobProgress title="Loading..." lines={job.logs} />;
	}
	if (job.error) {
		return <JobFailure message={job.error} lines={job.logs} />;
	}
	if (value.columns.length === 0) {
		return <StatusMessage kind="empty">No columns loaded.</StatusMessage>;
	}
	return (
		<StatusMessage kind="loading">
			{`${value.columns.length} columns: ${value.columns.join(", ")}`}
		</StatusMessage>
	);
}

// テーブル欄（DatasetSrcInfoScopeの内側）: テーブルを選んで列を読み込み、シート名を決める
function DatasetColumnsField({
	name,
	tableNames,
	value,
	onChange,
}: {
	name: string;
	tableNames: string[];
	value: DatasetColumns;
	onChange: (value: DatasetColumns) => void;
}) {
	const srcInfo = useDatasetSrcInfo();
	const fetchColumns = useColumnNamesFetcher();
	const job = useJob();
	const loadColumns = async () => {
		const result = await job.run((context) =>
			fetchColumns(srcInfo, value.tableName, context),
		);
		if (result) {
			onChange({ ...value, columns: result.value });
		}
	};
	return (
		<>
			<InlineActions>
				<OptionSelect
					id={`${name}TableName`}
					label={`${name} table`}
					options={columnOptions(tableNames)}
					value={value.tableName}
					onChange={(tableName) => {
						job.cancel();
						onChange(selectTable(value, tableName));
					}}
				/>
				<BlueButton
					title="Load Columns"
					disabled={value.tableName === "" || job.running}
					handleClick={loadColumns}
				/>
			</InlineActions>
			{renderColumnsState(value, job)}
			<TextField
				name={`${name} sheetName`}
				value={value.sheetName}
				onChange={(sheetName) => onChange({ ...value, sheetName })}
			/>
		</>
	);
}

// 比較する2テーブルの選択（DatasetSrcInfoScopeの内側）。
// generateはsrcのデータセットを1つしか読めないため、2テーブルとも同じデータセット（traversal・テーブル名の絞込・settingの結果）から選ぶ
function CompareTablesField({
	useSecond,
	first,
	second,
	onChangeFirst,
	onChangeSecond,
}: {
	useSecond: boolean;
	first: DatasetColumns;
	second: DatasetColumns;
	onChangeFirst: (value: DatasetColumns) => void;
	onChangeSecond: (value: DatasetColumns) => void;
}) {
	const srcInfo = useDatasetSrcInfo();
	const { tableNames } = useDatasetTableNames(withAllTables(srcInfo));
	return (
		<>
			{useSecond && tableNames.length !== 2 && (
				<StatusMessage kind="empty">
					{`The dataset has ${tableNames.length} tables. Narrow it down to the two tables to compare with traversal, table name filter or setting.`}
				</StatusMessage>
			)}
			<DatasetColumnsField
				name="old"
				tableNames={tableNames}
				value={first}
				onChange={onChangeFirst}
			/>
			{useSecond ? (
				<DatasetColumnsField
					name="new"
					tableNames={tableNames}
					value={second}
					onChange={onChangeSecond}
				/>
			) : (
				<TextField
					name="new sheetName"
					value={second.sheetName}
					onChange={(sheetName) => onChangeSecond({ ...second, sheetName })}
				/>
			)}
		</>
	);
}

// 2テーブルのときの列ペア（キー・比較ペア）の編集
function ColumnPairsField({
	caption,
	firstColumns,
	secondColumns,
	pairs,
	onChange,
}: {
	caption: string;
	firstColumns: string[];
	secondColumns: string[];
	pairs: ColumnPair[];
	onChange: (pairs: ColumnPair[]) => void;
}) {
	const update = (index: number, pair: ColumnPair) =>
		onChange(replaceArray(pairs, pair, index));
	return (
		<DialogField>
			<InputLabel text={caption} id={caption} required={false} />
			{pairs.map((pair, index) => (
				<InlineActions key={`${caption}-${index.toString()}`}>
					<OptionSelect
						id={`${caption}First${index}`}
						label="old"
						options={columnOptions(firstColumns)}
						value={pair.first}
						onChange={(first) => update(index, { ...pair, first })}
					/>
					<OptionSelect
						id={`${caption}Second${index}`}
						label="new"
						options={columnOptions(secondColumns)}
						value={pair.second}
						onChange={(second) => update(index, { ...pair, second })}
					/>
					<RemoveButton
						title={`remove ${caption}`}
						handleClick={() => onChange(removeArray(pairs, index))}
					/>
				</InlineActions>
			))}
			<AddButton
				title={`add ${caption}`}
				handleClick={() => onChange([...pairs, EMPTY_PAIR])}
			/>
		</DialogField>
	);
}

// 1テーブルのときのキー列の選択。選んだ順に複合キーにする
function KeyColumnsField({
	columns,
	selected,
	onChange,
}: {
	columns: string[];
	selected: string[];
	onChange: (selected: string[]) => void;
}) {
	return (
		<DialogField>
			<InputLabel text="keys" id="keys" required={false} />
			<SelectableTable
				items={columns}
				selected={new Set(selected)}
				onToggle={(column) =>
					onChange(
						selected.includes(column)
							? selected.filter((it) => it !== column)
							: [...selected, column],
					)
				}
				onToggleAll={(items, checked) =>
					onChange(
						checked
							? [...selected, ...items.filter((it) => !selected.includes(it))]
							: selected.filter((it) => !items.includes(it)),
					)
				}
				columnLabel="column"
				filterPlaceholder="Filter columns"
				emptyMessage="No columns loaded."
			/>
			<StatusMessage kind="empty">
				The other columns are compared with the same column.
			</StatusMessage>
		</DialogField>
	);
}

function XlsxCompareTemplateGenerateDialog({
	onClose,
	onSaved,
}: {
	onClose: () => void;
	onSaved: (fileName: string) => void;
}) {
	const dialog = useRefreshableDialog<GenerateOptions>({
		refreshEndpoint: "generate/refresh",
		formId,
		generateType: "xlsxTemplate",
	});
	const setResourcesSettings = useSetResourcesSettings();
	const [fileName, setFileName] = useState("");
	const [compareSheetName, setCompareSheetName] = useState(
		DEFAULT_COMPARE_SHEET_NAME,
	);
	const [useSecond, setUseSecond] = useState(true);
	const [first, setFirst] = useState(EMPTY_DATASET_COLUMNS);
	const [second, setSecond] = useState(EMPTY_DATASET_COLUMNS);
	const [keyPairs, setKeyPairs] = useState<ColumnPair[]>([EMPTY_PAIR]);
	const [keyColumns, setKeyColumns] = useState<string[]>([]);
	const [comparePairs, setComparePairs] = useState<ColumnPair[]>([EMPTY_PAIR]);

	const execute = async () => {
		const result = await dialog.execute(async (_values, context) => {
			const request = buildCompareTemplateRequest({
				fileName,
				compareSheetName,
				first,
				second: useSecond ? second : { ...first, sheetName: second.sheetName },
				keys: useSecond
					? keyPairs
					: keyColumns.map((column) => ({ first: column, second: column })),
				compares: useSecond ? comparePairs : null,
			});
			return postDialogForm<string[]>(
				dialog.apiUrl,
				"template/generate-compare",
				request,
				context,
			);
		});
		if (result) {
			setResourcesSettings((current) =>
				current.with({ templateFiles: result.value }),
			);
			onSaved(toSavedFileName(fileName));
		}
	};

	const renderDataset = (srcData: GenerateOptions["srcData"]) => (
		<DatasetSrcInfoScope name={formId} srcData={srcData}>
			<DatasetLoadForm
				handleTypeSelect={dialog.refresh}
				name={formId}
				srcData={srcData}
				parentSrcInfo
			/>
			<CompareTablesField
				useSecond={useSecond}
				first={first}
				second={second}
				onChangeFirst={setFirst}
				onChangeSecond={setSecond}
			/>
		</DatasetSrcInfoScope>
	);

	const renderKeysAndCompares = () => {
		if (!useSecond) {
			return (
				<KeyColumnsField
					columns={first.columns}
					selected={keyColumns}
					onChange={setKeyColumns}
				/>
			);
		}
		return (
			<>
				<ColumnPairsField
					caption="keys"
					firstColumns={first.columns}
					secondColumns={second.columns}
					pairs={keyPairs}
					onChange={setKeyPairs}
				/>
				<ColumnPairsField
					caption="compares"
					firstColumns={first.columns}
					secondColumns={second.columns}
					pairs={comparePairs}
					onChange={setComparePairs}
				/>
			</>
		);
	};

	return (
		<RefreshableDialogShell
			command="dialogs/xlsx-compare-template-generate"
			label="Generate Compare Template"
			title="Generate Compare Template"
			formId={formId}
			loadState={dialog.loadState}
			executing={dialog.executing}
			executeError={dialog.executeError}
			logs={dialog.logs}
			onExecute={execute}
			onClose={onClose}
		>
			{(options) => (
				<>
					<div>
						<TextField
							name={fileNameField}
							value={fileName}
							onChange={setFileName}
						/>
						<TextField
							name="compare sheetName"
							value={compareSheetName}
							onChange={setCompareSheetName}
						/>
						<OptionCheck
							id="useSecondTable"
							label="Use second table"
							checked={useSecond}
							onChange={setUseSecond}
						/>
					</div>
					{renderDataset(options.srcData)}
					{renderKeysAndCompares()}
				</>
			)}
		</RefreshableDialogShell>
	);
}

// template欄のdropdownに出す「比較用テンプレート生成」
export function XlsxCompareTemplateGenerateButton({
	setPath,
}: Pick<ResourceEditButtonProp, "setPath">) {
	return (
		<ResourceEditButton
			renderDialog={(open, closeDialog) =>
				open ? (
					<XlsxCompareTemplateGenerateDialog
						onClose={closeDialog}
						onSaved={(fileName) => {
							setPath(fileName);
							closeDialog();
						}}
					/>
				) : null
			}
			renderTrigger={(openDialog) => (
				<AddButton title="generate compare template" handleClick={openDialog} />
			)}
		/>
	);
}
