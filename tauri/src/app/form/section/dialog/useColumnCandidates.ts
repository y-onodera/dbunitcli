import { useState } from "react";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useDatasetTableNames } from "../../../../hooks/useDatasetSettings";
import { withAllTables } from "../../../../model/CommandOption";

// 設定1件の編集ダイアログで使うテーブル名・カラム名の候補。
// テーブル名はDatasetSrcInfoProviderのデータセットからsettingTargetOnlyで絞らずに取得し、
// カラム名はLoad Columns（ColumnDatalistDialog）で読み込んだものを保持する
export function useColumnCandidates() {
	const datasetSrcInfo = useDatasetSrcInfo();
	const { tableNames } = useDatasetTableNames(withAllTables(datasetSrcInfo));
	const [columnDatalist, setColumnDatalist] = useState<string[]>([]);
	const [showColumnDialog, setShowColumnDialog] = useState(false);
	return {
		tableNames,
		columnDatalist,
		setColumnDatalist,
		columnList: columnDatalist.length > 0 ? "column_datalist_list" : undefined,
		showColumnDialog,
		openColumnDialog: () => setShowColumnDialog(true),
		closeColumnDialog: () => setShowColumnDialog(false),
	};
}
