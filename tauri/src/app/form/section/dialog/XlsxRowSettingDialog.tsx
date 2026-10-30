import { useState } from "react";
import { Fieldset, SettingDialog, Text } from "../../../../components/dialog";
import { type RowSetting, XlsxSchema } from "../../../../model/XlsxSchema";
import { InlinePreviewButton } from "./DatasetTableNamesPreviewButton";
import { CommaSeparatedText } from "./DialogFields";
import { AddFileInfoCheck, XlsxTableFields } from "./XlsxSettingFields";

export default function XlsxRowSettingDialog(props: {
	setting: RowSetting;
	handleDialogClose: () => void;
	handleCommit: (newSettings: RowSetting) => void;
}) {
	const [target, setTarget] = useState(props.setting);

	return (
		<SettingDialog
			title="Xlsx Row Setting"
			helpCommand="dialogs/xlsx-row-setting"
			helpLabel="Xlsx Row Setting"
			setting={target}
			handleDialogClose={props.handleDialogClose}
			handleCommit={props.handleCommit}
			footerActions={() => (
				// 編集中のこの設定1件だけのスキーマで読み込む
				<InlinePreviewButton
					toInline={() => ({ inlineXlsxSchema: new XlsxSchema([target], []) })}
				/>
			)}
		>
			<Fieldset>
				<XlsxTableFields target={target} setTarget={setTarget} />
				<Text
					name="dataStart"
					value={target.dataStart}
					handleChange={(ev) =>
						setTarget((cur) => cur.with({ dataStart: ev.target.value }))
					}
				/>
				<CommaSeparatedText
					name="columnIndex"
					values={target.columnIndex}
					handleChange={(columnIndex) =>
						setTarget((cur) => cur.with({ columnIndex }))
					}
				/>
				<CommaSeparatedText
					name="breakKey"
					values={target.breakKey}
					handleChange={(breakKey) =>
						setTarget((cur) => cur.with({ breakKey }))
					}
				/>
				<AddFileInfoCheck target={target} setTarget={setTarget} />
			</Fieldset>
		</SettingDialog>
	);
}
