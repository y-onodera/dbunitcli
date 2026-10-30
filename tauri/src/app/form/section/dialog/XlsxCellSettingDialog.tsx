import { useState } from "react";
import { Arrays, Fieldset, SettingDialog } from "../../../../components/dialog";
import { type CellSetting, XlsxSchema } from "../../../../model/XlsxSchema";
import { InlinePreviewButton } from "./DatasetTableNamesPreviewButton";
import { AddFileInfoCheck, XlsxTableFields } from "./XlsxSettingFields";

export default function XlsxCellSettingDialog(props: {
	setting: CellSetting;
	handleDialogClose: () => void;
	handleCommit: (newSettings: CellSetting) => void;
}) {
	const [target, setTarget] = useState(props.setting);

	return (
		<SettingDialog
			title="Xlsx Cell Setting"
			helpCommand="dialogs/xlsx-cell-setting"
			helpLabel="Xlsx Cell Setting"
			setting={target}
			handleDialogClose={props.handleDialogClose}
			handleCommit={props.handleCommit}
			footerActions={() => (
				// 編集中のこの設定1件だけのスキーマで読み込む
				<InlinePreviewButton
					toInline={() => ({ inlineXlsxSchema: new XlsxSchema([], [target]) })}
				/>
			)}
		>
			<Fieldset>
				<XlsxTableFields target={target} setTarget={setTarget} />
				<Arrays
					name="rows"
					values={target.rows.map((row) => row.cellAddress.join(","))}
					handleChange={(text, index) =>
						setTarget((cur) => cur.replaceRows(text.split(","), index))
					}
					handleRemove={(index) =>
						setTarget((cur) => cur.replaceRows([], index))
					}
				/>
				<AddFileInfoCheck target={target} setTarget={setTarget} />
			</Fieldset>
		</SettingDialog>
	);
}
