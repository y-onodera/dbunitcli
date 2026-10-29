import { useState } from "react";
import { SettingDialog } from "../../../../components/dialog";
import { BlueButton } from "../../../../components/element/Button";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useDatasetTableNames } from "../../../../hooks/useDatasetSettings";
import type { DatasetSrcInfo } from "../../../../model/CommandOption";
import type { DatasetSetting } from "../../../../model/DatasetSettings";
import ColumnDatalistDialog from "./ColumnDatalistDialog";
import {
	AdditionalColumnsFields,
	FilterOrderFields,
	ModeFields,
	TargetFields,
} from "./DatasetSettingFields";
import { DatasetTableNamesPreviewDialog } from "./DatasetTableNamesPreviewButton";

export default function DatasetSettingDialog(props: {
	setting: DatasetSetting;
	handleDialogClose: () => void;
	handleCommit: (newSettings: DatasetSetting) => void;
}) {
	const [target, setTarget] = useState(props.setting);
	const [columnDatalist, setColumnDatalist] = useState<string[]>([]);
	const [showColumnDialog, setShowColumnDialog] = useState(false);
	const [previewSrcInfo, setPreviewSrcInfo] = useState<DatasetSrcInfo | null>(
		null,
	);
	const columnList =
		columnDatalist.length > 0 ? "column_datalist_list" : undefined;
	const datasetSrcInfo = useDatasetSrcInfo();
	const { tableNames } = useDatasetTableNames(datasetSrcInfo);

	return (
		<SettingDialog
			title="Dataset Setting"
			helpCommand="dialogs/dataset-setting"
			helpLabel="Dataset Setting"
			setting={target}
			handleDialogClose={props.handleDialogClose}
			handleCommit={props.handleCommit}
			footerActions={() =>
				// 編集中のこの設定1件の対象テーブルだけを、設定を適用してプレビューする（保存はしない）
				datasetSrcInfo.srcType && (
					<BlueButton
						title="Preview"
						handleClick={() =>
							setPreviewSrcInfo({
								...datasetSrcInfo,
								setting: "",
								inlineSetting: target,
							})
						}
					/>
				)
			}
		>
			<TargetFields
				target={target}
				setTarget={setTarget}
				tableNames={tableNames}
			/>
			<ModeFields
				target={target}
				setTarget={setTarget}
				columnList={columnList}
				SeparateDialog={DatasetSettingDialog}
			/>
			<AdditionalColumnsFields target={target} setTarget={setTarget} />
			<FilterOrderFields
				target={target}
				setTarget={setTarget}
				columnDatalist={columnDatalist}
				columnList={columnList}
				onLoadColumns={() => setShowColumnDialog(true)}
			/>
			{showColumnDialog && (
				<ColumnDatalistDialog
					tableNames={tableNames}
					target={target}
					onLoad={(columns) => setColumnDatalist(columns)}
					onClose={() => setShowColumnDialog(false)}
				/>
			)}
			{previewSrcInfo !== null && (
				<DatasetTableNamesPreviewDialog
					datasetSrcInfo={previewSrcInfo}
					handleDialogClose={() => setPreviewSrcInfo(null)}
				/>
			)}
		</SettingDialog>
	);
}
