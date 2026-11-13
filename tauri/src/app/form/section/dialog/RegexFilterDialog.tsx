import { useState } from "react";
import {
	DialogActions,
	DialogField,
	DialogTitle,
	JobFailure,
	ModalOverlay,
	StatusMessage,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { BlueEditButton } from "../../../../components/element/ButtonIcon";
import { JobProgress } from "../../../../components/element/JobProgress";
import {
	useDatasetSrcInfo,
	useInDatasetSrcInfoScope,
} from "../../../../context/DatasetSrcInfoProvider";
import { useDatasetTableSources } from "../../../../hooks/useDatasetSettings";
import { useTableSelection } from "../../../../hooks/useTableSelection";
import { withValues } from "../../../../model/CommandOption";
import {
	buildFilterRegex,
	matchedKeys,
	type RegexFilterKind,
	type TableSource,
	toFilterRows,
} from "../../../../model/RegexFilter";
import type { TextProp } from "../element/FormElementProp";
import SelectableTable from "./SelectableTable";

// 正規表現を当てる値の列名
const KEY_COLUMN_LABEL: Record<RegexFilterKind, string> = {
	path: "file",
	table: "source table",
};

type RegexFilterProps = {
	kind: RegexFilterKind;
	// 編集する項目のsrc要素名（regInclude等）。一覧はこの項目を空にして読む
	fieldName: string;
	value: string;
	onApply: (regex: string) => void;
};

// 正規表現の項目（Text）のchildrenに渡し、項目の横にRegexFilterButtonを置く
export function regexFilterSlot(
	kind: RegexFilterKind,
	fieldName: string,
): NonNullable<TextProp["children"]> {
	return ({ path, setPath }) => (
		<RegexFilterButton
			kind={kind}
			fieldName={fieldName}
			value={path}
			onApply={setPath}
		/>
	);
}

// 読み込まれるテーブルの一覧から選んで正規表現を組み立てるダイアログを開く。データセットを読めない範囲（runのtraversal等）では出さない
export function RegexFilterButton(props: RegexFilterProps) {
	const inScope = useInDatasetSrcInfoScope();
	const [open, setOpen] = useState(false);
	if (!inScope) {
		return null;
	}
	return (
		<>
			<BlueEditButton
				title={`select ${props.fieldName} from table list`}
				handleClick={() => setOpen(true)}
			/>
			{open && (
				<RegexFilterDialog
					{...props}
					onApply={(regex) => {
						props.onApply(regex);
						setOpen(false);
					}}
					onClose={() => setOpen(false)}
				/>
			)}
		</>
	);
}

type DialogProps = RegexFilterProps & { onClose: () => void };

function RegexFilterDialog(props: DialogProps) {
	const srcInfo = useDatasetSrcInfo();
	// 開いた時点のフォームで1回だけ読む（毎回作り直すと読込が繰り返される）
	const [loadInfo] = useState(() =>
		withValues(srcInfo, { [props.fieldName]: "" }),
	);
	const { sources, loading, logs, error } = useDatasetTableSources(loadInfo);

	const renderContent = () => {
		if (loading) {
			return <JobProgress title="Loading..." lines={logs} />;
		}
		if (error) {
			return <JobFailure message={error} lines={logs} />;
		}
		return null;
	};
	const content = renderContent();
	return (
		<ModalOverlay onClose={props.onClose} size="lg">
			<DialogTitle
				command="dialogs/regex-filter"
				label="Select from Table List"
			>
				Select from Table List
			</DialogTitle>
			{content === null ? (
				<RegexFilterSelection {...props} sources={sources} />
			) : (
				<>
					<DialogField last>{content}</DialogField>
					<DialogActions>
						<BlueButton title="Apply" disabled={true} handleClick={() => {}} />
						<WhiteButton title="Cancel" handleClick={props.onClose} />
					</DialogActions>
				</>
			)}
		</ModalOverlay>
	);
}

// 読み込んだ後に描画し、入力中の正規表現に一致する行を選んだ状態から始める
function RegexFilterSelection({
	kind,
	fieldName,
	value,
	onApply,
	onClose,
	sources,
}: DialogProps & { sources: TableSource[] }) {
	const rows = toFilterRows(kind, sources);
	const tableNames = new Map(rows.map((row) => [row.key, row.tableNames]));
	const keys = rows.map((row) => row.key);
	const { selected, toggle, toggleAll } = useTableSelection(
		matchedKeys(kind, value, sources),
	);
	const regex = buildFilterRegex(
		kind,
		keys.filter((key) => selected.has(key)),
	);
	return (
		<>
			<DialogField>
				<SelectableTable
					items={keys}
					selected={selected}
					onToggle={toggle}
					onToggleAll={toggleAll}
					columnLabel={KEY_COLUMN_LABEL[kind]}
					extraColumns={[
						{
							label: "table",
							value: (key) => (tableNames.get(key) ?? []).join(", "),
						},
					]}
					filterPlaceholder="Filter"
					emptyMessage="No tables loaded."
				/>
			</DialogField>
			<DialogField last>
				<StatusMessage kind="loading">{`${fieldName}: ${regex}`}</StatusMessage>
			</DialogField>
			<DialogActions>
				<BlueButton title="Apply" handleClick={() => onApply(regex)} />
				<WhiteButton title="Cancel" handleClick={onClose} />
			</DialogActions>
		</>
	);
}
