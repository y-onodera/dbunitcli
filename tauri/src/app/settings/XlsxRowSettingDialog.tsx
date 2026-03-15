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
    const srcPath = props.srcInfo?.srcPath ?? "";
    const regTableInclude = props.srcInfo?.regTableInclude ?? "";
    const regTableExclude = props.srcInfo?.regTableExclude ?? "";
    const recursive = props.srcInfo?.recursive ?? "";
    const regInclude = props.srcInfo?.regInclude ?? "";
    const regExclude = props.srcInfo?.regExclude ?? "";
    const extension = props.srcInfo?.extension ?? "";

    useEffect(() => {
        if (!srcPath) {
            return;
        }
        loadSheets({ srcPath, regTableInclude, regTableExclude, recursive, regInclude, regExclude, extension }).then(setSheetNames);
    }, [srcPath, regTableInclude, regTableExclude, recursive, regInclude, regExclude, extension, loadSheets]);

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
