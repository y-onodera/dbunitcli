import { useState } from "react";
import {
	Check,
	Fieldset,
	SettingDialog,
	Text,
} from "../../../../components/dialog";
import { ButtonIcon } from "../../../../components/element/ButtonIcon";
import { HelpIcon } from "../../../../components/element/Icon";
import { ResourceDatalist } from "../../../../components/element/Input";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useSrcInfoSheets } from "../../../../hooks/useXlsxSchema";
import type { RowSetting } from "../../../../model/XlsxSchema";
import { openHelpWindow } from "../../../../utils/helpWindow";

export default function XlsxRowSettingDialog(props: {
	setting: RowSetting;
	handleDialogClose: () => void;
	handleCommit: (newSettings: RowSetting) => void;
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
			<div className="flex justify-end px-4 pt-2">
				<ButtonIcon
					title="Help"
					handleClick={() => {
						openHelpWindow("xlsx-row-setting", "Xlsx Row Setting");
					}}
				>
					<HelpIcon />
				</ButtonIcon>
			</div>
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
				<Text
					name="dataStart"
					value={target.dataStart}
					handleChange={(ev) =>
						setTarget((cur) => cur.with({ dataStart: ev.target.value }))
					}
				/>
				<Text
					name="columnIndex"
					value={target.columnIndex.join(",")}
					handleChange={(ev) =>
						setTarget((cur) =>
							cur.with({ columnIndex: ev.target.value.split(",") }),
						)
					}
				/>
				<Text
					name="breakKey"
					value={target.breakKey.join(",")}
					handleChange={(ev) =>
						setTarget((cur) =>
							cur.with({ breakKey: ev.target.value.split(",") }),
						)
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
