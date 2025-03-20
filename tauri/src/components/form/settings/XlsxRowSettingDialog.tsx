import { useEffect, useRef, useState } from "react";
import type { RowSetting } from "../../../model/XlsxSchema";
import { BlueButton, WhiteButton } from "../../element/Button";
import { CheckBox, ControllTextBox, InputLabel } from "../../element/Input";

export default function XlsxRowSettingDialog(props: {
    setting: RowSetting
    handleDialogClose: () => void;
    handleCommit: (newSettings: RowSetting) => void;
}) {
    const dialogRef = useRef<HTMLDialogElement>(null);
    useEffect(() => { dialogRef.current?.showModal() }, [])
    const [target, setTarget] = useState(props.setting)
    const handleSave = () => {
        props.handleCommit(target);
    };

    return (
        <dialog ref={dialogRef} className="overflow-y-auto fixed 
                        top-0 right-0 left-0 z-50 
                        bg-white
                        border border-gray-200">
            <div className="p-4 rounded-lg mt-2">
                <div className="grid grid-cols-5 pb-2">
                    <InputLabel id="sheetNameLabel" text="Sheet Name" required={true} wStyle="p-2.5 w=1/5" />
                    <ControllTextBox name="sheetName" id="sheetName" required={true} wStyle="col-start-2 col-span-4 mr-2"
                        value={target.sheetName}
                        handleChange={ev => setTarget(cur => cur.with({ sheetName: ev.target.value }))}
                    />
                </div>
                <div className="grid grid-cols-5 pb-2">
                    <InputLabel id="tableNameLabel" text="Table Name" required={true} wStyle="p-2.5 w=1/5" />
                    <ControllTextBox name="tableName" id="tableName" required={true} wStyle="col-start-2 col-span-4 mr-2"
                        value={target.tableName}
                        handleChange={ev => setTarget(cur => cur.with({ tableName: ev.target.value }))}
                    />
                </div>
                <div className="grid grid-cols-5 pb-2">
                    <InputLabel id="headerLabel" text="Header" required={false} wStyle="p-2.5 w=1/5" />
                    <ControllTextBox name="header" id="header" required={false} wStyle="col-start-2 col-span-4 mr-2"
                        value={target.header.join(",")}
                        handleChange={ev => setTarget(cur => cur.with({ header: ev.target.value.split(",") }))}
                    />
                </div>
                <div className="grid grid-cols-5 pb-2">
                    <InputLabel id="dataStartLabel" text="Data Start" required={true} wStyle="p-2.5 w=1/5" />
                    <ControllTextBox name="dataStart" id="dataStart" required={true} wStyle="col-start-2 col-span-4 mr-2"
                        value={target.dataStart}
                        handleChange={ev => setTarget(cur => cur.with({ dataStart: ev.target.value }))}
                    />
                </div>
                <div className="grid grid-cols-5 pb-2">
                    <InputLabel id="columnIndexLabel" text="Column Index" required={true} wStyle="p-2.5 w=1/5" />
                    <ControllTextBox name="columnIndex" id="columnIndex" required={true} wStyle="col-start-2 col-span-4 mr-2"
                        value={target.columnIndex.join(",")}
                        handleChange={ev => setTarget(cur => cur.with({ columnIndex: ev.target.value.split(",") }))}
                    />
                </div>
                <div className="grid grid-cols-5 pb-2">
                    <InputLabel id="breakKeyLabel" text="Break Key" required={true} wStyle="p-2.5 w=1/5" />
                    <ControllTextBox name="breakKey" id="breakKey" required={true} wStyle="col-start-2 col-span-4 mr-2"
                        value={target.breakKey.join(",")}
                        handleChange={ev => setTarget(cur => cur.with({ breakKey: ev.target.value.split(",") }))}
                    />
                </div>
                <div className="grid grid-cols-5 pb-2">
                    <InputLabel id="addFileInfoLabel" text="Add File Info" required={false} wStyle="p-2.5 w=1/5" />
                    <div className="p-2.5">
                        <CheckBox name="addFileInfo" id="addFileInfo" defaultValue={target.addFileInfo ? "true" : "false"}
                            handleOnChange={checked => setTarget(cur => cur.with({ addFileInfo: checked }))} />
                    </div>
                </div>
                <div className="flex items-center justify-end">
                    <BlueButton title="Save" handleClick={handleSave} />
                    <WhiteButton title="Close" handleClick={props.handleDialogClose} />
                </div>
            </div>
        </dialog>
    );
}