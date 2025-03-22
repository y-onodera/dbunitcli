import { useState } from "react";
import { Arrays, Check, Fieldset, SettingDialog, Text } from "../../components/dialog/SettingDialog";
import type { CellSetting } from "../../model/XlsxSchema";

export default function XlsxCellSettingDialog(props: {
    setting: CellSetting
    handleDialogClose: () => void;
    handleCommit: (newSettings: CellSetting) => void;
}) {
    const [target, setTarget] = useState(props.setting)

    return (
        <SettingDialog setting={target} handleDialogClose={props.handleDialogClose} handleCommit={props.handleCommit}>
            <Fieldset>
                <Text name="sheetName" value={target.sheetName}
                    handleChange={ev => setTarget(cur => cur.with({ sheetName: ev.target.value }))}
                />
                <Text name="tableName" value={target.tableName}
                    handleChange={ev => setTarget(cur => cur.with({ tableName: ev.target.value }))}
                />
                <Text name="header" value={target.header.join(",")}
                    handleChange={ev => setTarget(cur => cur.with({ header: ev.target.value.split(",") }))}
                />
                <Arrays name="rows" values={target.rows.map(row => row.cellAddress.join(","))}
                    handleChange={(text, index) => setTarget(cur => cur.replaceRows(text.split(","), index))}
                    handleRemove={(index) => setTarget(cur => cur.replaceRows([], index))} />
                <Check name="addFileInfo" value={target.addFileInfo ? "true" : "false"}
                    handleOnChange={checked => setTarget(cur => cur.with({ addFileInfo: checked }))} />
            </Fieldset>
        </SettingDialog>
    );
}
