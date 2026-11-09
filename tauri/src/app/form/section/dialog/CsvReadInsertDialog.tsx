import { useState } from "react";
import { DialogTitle, ModalOverlay } from "../../../../components/dialog";
import { useCsvColumnNamesFetcher } from "../../../../hooks/useDatasetSettings";
import {
	csvFileAlias,
	type TableUsage,
	usesDerivedTable,
} from "../../../../utils/textInsert";
import CsvFileFields from "./CsvFileFields";
import type { SelectGroup } from "./SqlInsertParts";
import TableInsertForm, { TABLE_USAGE_GROUPS } from "./TableInsertForm";
import { useCsvFile } from "./useCsvFile";

// CSVは書き込めないため、Select Tablesの用途のうちFROM / JOINに置くものと列だけを選ぶもの
const CSV_USAGE_GROUPS: SelectGroup<TableUsage>[] = TABLE_USAGE_GROUPS.map(
	(group) => ({
		label: group.label,
		options: group.options.filter(
			(o) => usesDerivedTable(o.value) || o.value === "columns",
		),
	}),
);

// Select Tablesのテーブル一覧の代わりに、CSVファイルを1つ選んでCSVREADで読む
export default function CsvReadInsertDialog({
	encoding,
	onInsert,
	onClose,
}: {
	// 文字コードの初期値
	encoding: string;
	onInsert: (lines: string[], inline?: boolean) => void;
	onClose: () => void;
}) {
	const csv = useCsvFile(encoding, csvFileAlias);
	const fetchColumns = useCsvColumnNamesFetcher();
	const [usage, setUsage] = useState<TableUsage>("select");
	return (
		<ModalOverlay onClose={onClose}>
			<DialogTitle>Insert CSVREAD</DialogTitle>
			<CsvFileFields idPrefix="csvReadInsert" {...csv.fields} />
			<TableInsertForm
				idPrefix="csvReadInsert"
				table={csv.ready ? csv.expression : null}
				usage={usage}
				onUsageChange={setUsage}
				alias={csv.alias}
				onAliasChange={csv.setAlias}
				usageGroups={CSV_USAGE_GROUPS}
				subquerySource={{
					kind: "csv",
					encoding: csv.fields.charset,
					onQueryColumns: fetchColumns,
				}}
				onInsert={onInsert}
				onClose={onClose}
			/>
		</ModalOverlay>
	);
}
