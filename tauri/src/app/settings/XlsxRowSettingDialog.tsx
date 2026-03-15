import { useEffect, useState } from "react";
import { Check, Fieldset, SettingDialog, Text } from "../../components/dialog";
import { ResourceDatalist } from "../../components/element/Input";
import { useXlsxSheets } from "../../hooks/useXlsxSchema";
import type { RowSetting } from "../../model/XlsxSchema";
import type { SrcInfo } from "../form/FormElementProp";

export default function XlsxRowSettingDialog(props: {
    setting: RowSetting;
    srcInfo?: SrcInfo;
    handleDialogClose: () => void;
    handleCommit: (newSettings: RowSetting) => void;
}) {
    const [target, setTarget] = useState(props.setting);
    const [sheetNames, setSheetNames] = useState<string[]>([]);
    const loadSheets = useXlsxSheets();

    useEffect(() => {
        if (!props.srcInfo?.srcPath) {
            return;
        }
        loadSheets(props.srcInfo).then(setSheetNames);
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [props.srcInfo?.srcPath, props.srcInfo?.regTableInclude, props.srcInfo?.regTableExclude, props.srcInfo?.recursive, props.srcInfo?.regInclude, props.srcInfo?.regExclude, props.srcInfo?.extension]);

    return (
        <SettingDialog setting={target} handleDialogClose={props.handleDialogClose} handleCommit={props.handleCommit}>
            <Fieldset>
                <Text name="sheetName" value={target.sheetName}
                    list={sheetNames.length > 0 ? "sheetName_list" : undefined}
                    handleChange={ev => setTarget(cur => cur.with({ sheetName: ev.target.value }))}
                />
                {sheetNames.length > 0 && (
                    <ResourceDatalist id="sheetName" resources={sheetNames} />
                )}
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
