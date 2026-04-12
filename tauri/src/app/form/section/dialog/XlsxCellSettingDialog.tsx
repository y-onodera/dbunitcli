import { useState } from "react";
import {
	Arrays,
	Check,
	Fieldset,
	SettingDialog,
	Text,
} from "../../../../components/dialog";
import { ResourceDatalist } from "../../../../components/element/Input";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useSrcInfoSheets } from "../../../../hooks/useXlsxSchema";
import type { CellSetting } from "../../../../model/XlsxSchema";

export default function XlsxCellSettingDialog(props: {
	setting: CellSetting;
	handleDialogClose: () => void;
	handleCommit: (newSettings: CellSetting) => void;
}) {
	const [target, setTarget] = useState(props.setting);
	const datasetSrcInfo = useDatasetSrcInfo();
	const sheetNames = useSrcInfoSheets(datasetSrcInfo);

	return (
		<SettingDialog
			setting={target}
			handleDialogClose={props.handleDialogClose}
			handleCommit={props.handleCommit}
		>
			<Fieldset>
				<Text
					name="sheetName"
					value={target.sheetName}
					list={sheetNames.length > 0 ? "sheetName_list" : undefined}
					handleChange={(ev) =>
						setTarget((cur) => cur.with({ sheetName: ev.target.value }))
					}
				/>
				{sheetNames.length > 0 && (
					<ResourceDatalist id="sheetName" resources={sheetNames} />
				)}
				<Text
					name="tableName"
					value={target.tableName}
					handleChange={(ev) =>
						setTarget((cur) => cur.with({ tableName: ev.target.value }))
					}
				/>
				<Text
					name="header"
					value={target.header.join(",")}
					handleChange={(ev) =>
						setTarget((cur) => cur.with({ header: ev.target.value.split(",") }))
					}
				/>
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
				<Check
					name="addFileInfo"
					value={target.addFileInfo ? "true" : "false"}
					handleOnChange={(checked) =>
						setTarget((cur) => cur.with({ addFileInfo: checked }))
					}
				/>
			</Fieldset>
		</SettingDialog>
	);
}
