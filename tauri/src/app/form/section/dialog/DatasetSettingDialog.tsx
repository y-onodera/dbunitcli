import { useState } from "react";
import { SettingDialog } from "../../../../components/dialog";
import type { DatasetSetting } from "../../../../model/DatasetSettings";
import ColumnDatalistDialog from "./ColumnDatalistDialog";
import {
	AdditionalColumnsFields,
	FilterOrderFields,
	ModeFields,
	TargetFields,
} from "./DatasetSettingFields";
import { InlinePreviewButton } from "./DatasetTableNamesPreviewButton";
import { useColumnCandidates } from "./useColumnCandidates";

export default function DatasetSettingDialog(props: {
	setting: DatasetSetting;
	handleDialogClose: () => void;
	handleCommit: (newSettings: DatasetSetting) => void;
}) {
	const [target, setTarget] = useState(props.setting);
	const candidates = useColumnCandidates();

	return (
		<SettingDialog
			title="Dataset Setting"
			helpCommand="dialogs/dataset-setting"
			helpLabel="Dataset Setting"
			setting={target}
			handleDialogClose={props.handleDialogClose}
			handleCommit={props.handleCommit}
			footerActions={() => (
				// 編集中のこの設定1件の対象テーブルだけを、設定を適用してプレビューする
				<InlinePreviewButton toInline={() => ({ inlineSetting: target })} />
			)}
		>
			<TargetFields
				target={target}
				setTarget={setTarget}
				tableNames={candidates.tableNames}
			/>
			<ModeFields
				target={target}
				setTarget={setTarget}
				columnList={candidates.columnList}
				SeparateDialog={DatasetSettingDialog}
			/>
			<AdditionalColumnsFields target={target} setTarget={setTarget} />
			<FilterOrderFields
				target={target}
				setTarget={setTarget}
				columnDatalist={candidates.columnDatalist}
				columnList={candidates.columnList}
				onLoadColumns={candidates.openColumnDialog}
			/>
			{candidates.showColumnDialog && (
				<ColumnDatalistDialog
					tableNames={candidates.tableNames}
					target={target}
					onLoad={candidates.setColumnDatalist}
					onClose={candidates.closeColumnDialog}
				/>
			)}
		</SettingDialog>
	);
}
