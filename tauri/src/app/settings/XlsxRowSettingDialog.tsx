import { useState } from "react";
import { Check, Fieldset, SettingDialog, Text } from "../../components/dialog/SettingDialog";
import type { RowSetting } from "../../model/XlsxSchema";

export default function XlsxRowSettingDialog(props: {
    setting: RowSetting
    handleDialogClose: () => void;
    handleCommit: (newSettings: RowSetting) => void;
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
                <Text name="dataStart" value={target.dataStart}
                    handleChange={ev => setTarget(cur => cur.with({ dataStart: ev.target.value }))}
                />
                <Text name="columnIndex" value={target.columnIndex.join(",")}
                    handleChange={ev => setTarget(cur => cur.with({ columnIndex: ev.target.value.split(",") }))}
                />
                <Text name="breakKey" value={target.breakKey.join(",")}
                    handleChange={ev => setTarget(cur => cur.with({ breakKey: ev.target.value.split(",") }))}
                />
                <Check name="addFileInfo" value={target.addFileInfo ? "true" : "false"}
                    handleOnChange={checked => setTarget(cur => cur.with({ addFileInfo: checked }))} />
            </Fieldset>
        </SettingDialog>
    );
}
