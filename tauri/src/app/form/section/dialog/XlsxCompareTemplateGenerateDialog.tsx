import { useState } from "react";
import {
	DataTable,
	DialogField,
	InlineActions,
	JobFailure,
	StatusMessage,
	Td,
	Th,
	Tr,
} from "../../../../components/dialog";
import { BlueButton } from "../../../../components/element/Button";
import {
	AddButton,
	RemoveButton,
} from "../../../../components/element/ButtonIcon";
import {
	CheckBox,
	ControllTextBox,
	InputLabel,
} from "../../../../components/element/Input";
import { JobProgress } from "../../../../components/element/JobProgress";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useSetResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import {
	useColumnNamesFetcher,
	useDatasetTableNames,
} from "../../../../hooks/useDatasetSettings";
import { type JobContext, useJob } from "../../../../hooks/useJob";
import { withAllTables } from "../../../../model/CommandOption";
import type { CompareOptions } from "../../../../model/SelectParameter";
import {
	buildCompareTemplateRequest,
	type ColumnPair,
	type DatasetColumns,
	EMPTY_DATASET_COLUMNS,
	selectTable,
	type XlsxCompareTemplateRequest,
} from "../../../../model/XlsxCompareTemplate";
import { fetchData, postJsonParams } from "../../../../utils/fetchUtils";
import { DatasetLoadForm, DatasetSrcInfoScope } from "../DatasetLoadForm";
import { RefreshableDialogShell } from "./RefreshableDialogShell";
import ResourceEditButton, {
	type ResourceEditButtonProp,
} from "./ResourceEditButton";
import { OptionSelect, type SelectOption } from "./SqlInsertParts";
import { useRefreshableDialog } from "./useRefreshableDialog";
import { toSavedFileName } from "./XlsxTemplateGenerateDialog";

const formId = "xlsxCompareTemplateGenerateForm";
const fileNameField = "-fileName";
const DEFAULT_COMPARE_SHEET_NAME = "compare";
const EMPTY_PAIR: ColumnPair = { first: "", second: "" };

async function postCompareTemplate(
	apiUrl: string,
	request: XlsxCompareTemplateRequest,
	context: JobContext,
): Promise<string[]> {
	const response = await fetchData({
		...postJsonParams(`${apiUrl}template/generate-compare`, request),
		...context,
	});
	return (await response.json()) as string[];
}

function columnOptions(columns: string[]): SelectOption<string>[] {
	return [
		{ value: "", label: "" },
		...columns.map((column) => ({ value: column, label: column })),
	];
}

function TextField({
	name,
	value,
	required,
	onChange,
}: {
	name: string;
	value: string;
	required: boolean;
	onChange: (value: string) => void;
}) {
	return (
		<DialogField>
			<InputLabel text={name} id={name} required={required} />
			<ControllTextBox
				name={name}
				id={name}
				required={required}
				value={value}
				handleChange={(ev) => onChange(ev.target.value)}
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

// データセット欄（DatasetSrcInfoScopeの内側）: テーブルを選んで列を読み込み、シート名を決める
function DatasetColumnsField({
	name,
	value,
	onChange,
}: {
	name: string;
	value: DatasetColumns;
	onChange: (value: DatasetColumns) => void;
}) {
	const srcInfo = useDatasetSrcInfo();
	const { tableNames } = useDatasetTableNames(withAllTables(srcInfo));
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
				required={true}
				onChange={(sheetName) => onChange({ ...value, sheetName })}
			/>
		</>
	);
}

// 2データセットのときの列ペア（キー・比較ペア）の編集
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
		onChange(pairs.map((it, i) => (i === index ? pair : it)));
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
						handleClick={() => onChange(pairs.filter((_, i) => i !== index))}
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

// 1データセットのときのキー列の選択
function KeyColumnsField({
	columns,
	selected,
	onChange,
}: {
	columns: string[];
	selected: string[];
	onChange: (selected: string[]) => void;
}) {
	const toggle = (column: string) =>
		onChange(
			selected.includes(column)
				? selected.filter((it) => it !== column)
				: [...selected, column],
		);
	return (
		<DialogField>
			<InputLabel text="keys" id="keys" required={false} />
			<DataTable
				head={
					<>
						<Th narrow />
						<Th>column</Th>
					</>
				}
			>
				{columns.map((column) => (
					<Tr key={column}>
						<Td>
							<input
								type="checkbox"
								aria-label={`key ${column}`}
								className="w-4 h-4 text-primary border border-border ring-primary-ring focus-visible:ring-3"
								checked={selected.includes(column)}
								onChange={() => toggle(column)}
							/>
						</Td>
						<Td>{column}</Td>
					</Tr>
				))}
			</DataTable>
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
	const dialog = useRefreshableDialog<CompareOptions>({
		refreshEndpoint: "compare/refresh",
		formId,
	});
	const setResourcesSettings = useSetResourcesSettings();
	const [fileName, setFileName] = useState("");
	const [compareSheetName, setCompareSheetName] = useState(
		DEFAULT_COMPARE_SHEET_NAME,
	);
	const [useSecond, setUseSecond] = useState(true);
	const [first, setFirst] = useState(EMPTY_DATASET_COLUMNS);
	const [second, setSecond] = useState(EMPTY_DATASET_COLUMNS);
	const [secondSheetName, setSecondSheetName] = useState("");
	const [keyPairs, setKeyPairs] = useState<ColumnPair[]>([EMPTY_PAIR]);
	const [keyColumns, setKeyColumns] = useState<string[]>([]);
	const [comparePairs, setComparePairs] = useState<ColumnPair[]>([EMPTY_PAIR]);

	const execute = async () => {
		const result = await dialog.execute(async (_values, context) => {
			const request = buildCompareTemplateRequest({
				fileName,
				compareSheetName,
				first,
				second: useSecond ? second : null,
				secondSheetName,
				keys: useSecond
					? keyPairs
					: keyColumns.map((column) => ({ first: column, second: column })),
				compares: comparePairs,
			});
			return postCompareTemplate(dialog.apiUrl, request, context);
		});
		if (result) {
			setResourcesSettings((current) =>
				current.with({ templateFiles: result.value }),
			);
			onSaved(toSavedFileName(fileName));
		}
	};

	const renderDataset = (
		srcData: CompareOptions["oldData"],
		value: DatasetColumns,
		onChange: (value: DatasetColumns) => void,
	) => (
		<DatasetSrcInfoScope name={formId} srcData={srcData}>
			<DatasetLoadForm
				handleTypeSelect={dialog.refresh}
				name={formId}
				srcData={srcData}
				parentSrcInfo
			/>
			<DatasetColumnsField
				name={srcData.prefix}
				value={value}
				onChange={onChange}
			/>
		</DatasetSrcInfoScope>
	);

	const renderSecond = (options: CompareOptions) => {
		if (useSecond) {
			return renderDataset(options.newData, second, setSecond);
		}
		return (
			<TextField
				name="new sheetName"
				value={secondSheetName}
				required={true}
				onChange={setSecondSheetName}
			/>
		);
	};

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
							required={true}
							onChange={setFileName}
						/>
						<TextField
							name="compare sheetName"
							value={compareSheetName}
							required={true}
							onChange={setCompareSheetName}
						/>
						<div className="flex items-center gap-2">
							<CheckBox
								name="useSecondDataset"
								id="useSecondDataset"
								defaultValue={String(useSecond)}
								handleOnChange={setUseSecond}
							/>
							<InputLabel
								text="Use second dataset"
								id="useSecondDataset"
								required={false}
							/>
						</div>
					</div>
					{renderDataset(options.oldData, first, setFirst)}
					{renderSecond(options)}
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
