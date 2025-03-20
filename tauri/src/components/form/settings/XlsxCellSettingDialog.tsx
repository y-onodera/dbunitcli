import { useEffect, useRef, useState } from "react";
import type { CellSetting } from "../../../model/XlsxSchema";
import { BlueButton, WhiteButton } from "../../element/Button";
import { AddButton, RemoveButton } from "../../element/ButtonIcon";
import { CheckBox, ControllTextBox, InputLabel } from "../../element/Input";

export default function XlsxCellSettingDialog(props: {
    setting: CellSetting
    handleDialogClose: () => void;
    handleCommit: (newSettings: CellSetting) => void;
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
                    <InputLabel id="headerLabel" text="Header" required={true} wStyle="p-2.5 w=1/5" />
                    <ControllTextBox name="header" id="header" required={true} wStyle="col-start-2 col-span-4 mr-2"
                        value={target.header.join(",")}
                        handleChange={ev => setTarget(cur => cur.with({ header: ev.target.value.split(",") }))}
                    />
                </div>
                <Arrays name="rows" values={target.rows.map(row => row.cellAddress.join(","))}
                    handleChange={(text, index) => setTarget(cur => cur.replaceRows(text.split(","), index))}
                    handleRemove={(index) => setTarget(cur => cur.replaceRows([], index))} />
                <div className="grid grid-cols-5 pb-2">
                    <InputLabel id="addFileInfoLabel" text="Add File Info" required={true} wStyle="p-2.5 w=1/5" />
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
function Arrays(props: {
    name: string, values: string[]
    , handleChange: (text: string, index: number) => void
    , handleRemove: (index: number) => void
    , ignoreLabel?: boolean
}) {
    const [text, setText] = useState("");
    return (
        <>
            {props.values.length === 0
                ?
                <div className="grid grid-cols-5 justify-center pb-2">
                    {!props.ignoreLabel &&
                        <InputLabel id={props.name} text={props.name} required={true} wStyle="p-2.5 w=1/5" />
                    }
                    <ControllTextBox
                        name={props.name}
                        id={props.name}
                        required={true}
                        wStyle="col-start-2"
                        value={text}
                        handleChange={(ev) => setText(ev.target.value)}
                        handleBlur={(ev) => props.handleChange(ev.target.value, 0)}
                    />
                </div>
                :
                props.values.map((val, index) => {
                    return (
                        <ArraysText key={val ?? index} name={props.name} val={val} index={index}
                            handleChange={props.handleChange}
                            handleRemove={props.handleRemove}
                            ignoreLabel={props.ignoreLabel}
                        />
                    )
                })
            }
            <div className="grid grid-cols-5 justify-center pb-2">
                {(props.values.length > 0 && props.values[props.values.length - 1]) &&
                    <div className="col-start-2">
                        <AddButton handleClick={() => props.handleChange("new item", props.values.length + 1)} />
                    </div>
                }
            </div>
        </>
    );
}
function ArraysText(props: {
    name: string, val: string, index: number
    , handleChange: (text: string, index: number) => void
    , handleRemove: (index: number) => void
    , ignoreLabel?: boolean
}) {
    const [text, setText] = useState(props.val);
    const handleBlur = (newVal: React.FocusEvent<HTMLInputElement>) => props.handleChange(newVal.target.value, props.index)
    const handleRemove = () => props.handleRemove(props.index)
    return (
        <div className="grid grid-cols-5 pb-2">
            {(!props.ignoreLabel && props.index === 0) &&
                <InputLabel id={props.name} text={props.name} required={true} wStyle="p-2.5 w=1/5" />
            }
            <ControllTextBox
                name={props.name}
                id={props.name}
                required={true}
                wStyle="col-start-2"
                value={text}
                handleChange={(ev) => setText(ev.target.value)}
                handleBlur={handleBlur}
            />
            {props.index > 0 &&
                <div className="col-start-3">
                    <RemoveButton handleClick={handleRemove} />
                </div>
            }
        </div>
    )
}
