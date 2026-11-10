import { useState } from "react";
import {
	Fieldset,
	Select,
	SettingDialog,
	Text,
} from "../../../../components/dialog";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useSrcInfoColumnNames } from "../../../../hooks/useDatasetSettings";
import type { ColumnDef } from "../../../../model/FixedColumnDef";
import { TextWithDatalist } from "./DialogFields";

export default function ColumnDefSettingDialog(props: {
	setting: ColumnDef;
	handleDialogClose: () => void;
	handleCommit: (newSetting: ColumnDef) => void;
}) {
	const [target, setTarget] = useState(props.setting);
	// 打ち直すために空にできるよう、入力中の文字列はモデルの数値と別に持つ
	const [lengthText, setLengthText] = useState(String(props.setting.length));
	const datasetSrcInfo = useDatasetSrcInfo();
	const columnNames = useSrcInfoColumnNames(datasetSrcInfo);

	return (
		<SettingDialog
			title="Column Def Setting"
			helpCommand="dialogs/fixed-column-def"
			helpLabel="Fixed Column Def"
			setting={target}
			handleDialogClose={props.handleDialogClose}
			handleCommit={props.handleCommit}
		>
			<Fieldset>
				<TextWithDatalist
					name="name"
					value={target.name}
					options={columnNames}
					handleChange={(ev) =>
						setTarget((cur) => cur.with({ name: ev.target.value }))
					}
				/>
				<Text
					name="length"
					value={lengthText}
					handleChange={(ev) => {
						const text = ev.target.value.replace(/[^0-9]/g, "");
						setLengthText(text);
						setTarget((cur) => cur.with({ length: Number(text) }));
					}}
				/>
				<Select
					name="align"
					defaultValue={target.align}
					handleOnChange={async (selected) =>
						setTarget((cur) => cur.with({ align: selected }))
					}
				>
					<option value="left">left</option>
					<option value="right">right</option>
				</Select>
				<Text
					name="pad"
					value={target.pad}
					handleChange={(ev) =>
						setTarget((cur) => cur.with({ pad: ev.target.value.slice(0, 1) }))
					}
					handleBlur={(ev) => {
						if (!ev.target.value) {
							setTarget((cur) => cur.with({ pad: " " }));
						}
					}}
				/>
			</Fieldset>
		</SettingDialog>
	);
}
