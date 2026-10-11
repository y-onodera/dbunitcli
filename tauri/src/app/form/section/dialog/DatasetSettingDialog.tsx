import { useState } from "react";
import { SettingDialog } from "../../../../components/dialog";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useDatasetTableNames } from "../../../../hooks/useDatasetSettings";
import type { DatasetSetting } from "../../../../model/DatasetSettings";
import ColumnDatalistDialog from "./ColumnDatalistDialog";
import {
	AdditionalColumnsFields,
	FilterOrderFields,
	ModeFields,
	TargetFields,
} from "./DatasetSettingFields";

export default function DatasetSettingDialog(props: {
	setting: DatasetSetting;
	handleDialogClose: () => void;
	handleCommit: (newSettings: DatasetSetting) => void;
}) {
	const [target, setTarget] = useState(props.setting);
	const [columnDatalist, setColumnDatalist] = useState<string[]>([]);
	const [showColumnDialog, setShowColumnDialog] = useState(false);
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
		</SettingDialog>
	);
}
