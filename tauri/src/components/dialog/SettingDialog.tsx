import { type ReactNode, useEffect, useRef } from "react";
import { useState } from "react";
import { BlueButton, WhiteButton } from "../element/Button";
import { AddButton, RemoveButton } from "../element/ButtonIcon";
import { CheckBox, ControllTextBox, InputLabel, SelectBox } from "../element/Input";

export type SettingDialogProps<T> = {
    setting: T;
    handleDialogClose: () => void;
    handleCommit: (newSettings: T) => void;
    children: ReactNode;
};

export function SettingDialog<T>(props: SettingDialogProps<T>) {
    const dialogRef = useRef<HTMLDialogElement>(null);
    useEffect(() => { dialogRef.current?.showModal() }, []);

    return (
        <dialog ref={dialogRef} onClose={props.handleDialogClose}
            className="overflow-y-auto fixed 
                    top-0 right-0 left-0 z-50
                    bg-white
                    border border-gray-200"
        >
            <div className="p-4 rounded-lg mt-2">
                {props.children}
            </div>
            <div className="flex items-center justify-end">
                <BlueButton title="Save" handleClick={() => props.handleCommit(props.setting)} />
                <WhiteButton title="Close" handleClick={props.handleDialogClose} />
            </div>
        </dialog>
    );
}

export function Fieldset(props: { children: ReactNode }) {
    return (
        <fieldset className="border border-gray-200 p-2.5 m-2">
            {props.children}
        </fieldset>
    );
}

export function Check(props: {
    handleOnChange: ((checked: boolean) => void); name: string, value: string
}) {
    return (
        <div className="grid grid-cols-5 justify-center">
            <InputLabel id={props.name} text={props.name} required={false} wStyle="p-2.5 w=1/5" />
            <div className="p-2.5">
                <CheckBox
                    name={props.name}
                    id={props.name}
                    defaultValue={props.value}
                    handleOnChange={props.handleOnChange}
                />
            </div>
        </div>
    );
}

export function Text(props: { name: string, value: string, handleChange: (text: React.ChangeEvent<HTMLInputElement>) => void, ignoreLabel?: boolean }) {
    return (
        <div className="grid grid-cols-5 justify-center pb-2">
                {!props.ignoreLabel &&
                    <InputLabel id={props.name} text={props.name} required={false} wStyle="p-2.5 w=1/5" />
                }
                <ControllTextBox
                    name={props.name}
                    id={props.name}
                    required={true}
                    wStyle="col-start-2 col-span-3"
                    value={props.value}
                    handleChange={props.handleChange}
                />
            </div>
    );
}

export function Select(props: {
    defaultValue: string,
    handleOnChange: ((selected: string) => Promise<void>),
    name: string,
    children: ReactNode
}) {
    return (
        <div className="grid grid-cols-5 justify-center pb-2">
            <InputLabel id={props.name} text={props.name} required={false} wStyle="p-2.5 w=1/5" />
            <SelectBox
                name={props.name}
                id={props.name}
                required={true}
                wStyle="col-start-2 col-span-2"
                defaultValue={props.defaultValue}
                handleOnChange={props.handleOnChange}
            >
                {props.children}
            </SelectBox>
        </div>
    );
}

export function Arrays(props: {
    name: string, values: string[]
    , handleChange: (text: string, index: number) => void
    , handleRemove: (index: number) => void
    , ignoreLabel?: boolean
}) {
    return (
        <>
            {props.values.length === 0
                ?
                <ArraysText key={0} name={props.name} val={""} index={0}
                    handleChange={props.handleChange}
                    handleRemove={props.handleRemove}
                    ignoreLabel={props.ignoreLabel}
                />
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

export function ArraysText(props: {
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
                <InputLabel id={props.name} text={props.name} required={false} wStyle="p-2.5 w=1/5" />
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

export function KeyValues(props: {
    name: string, values: object
    , handleChange: (index: number, value: { [prop: string]: string }) => void
    , handleRemove: (index: number) => void
}) {
    const entries = Object.entries(props.values)
    return (
        <>
            {entries.length === 0
                ?
                <KeyValueText propKey={""} name={props.name} value={""} index={0}
                    handleChange={props.handleChange}
                    handleRemove={props.handleRemove}
                />
                :
                entries.map(([key, value], index) => {
                    return (
                        <KeyValueText key={key} propKey={key} name={props.name} value={value.toString()} index={index}
                            handleChange={props.handleChange}
                            handleRemove={props.handleRemove}
                        />
                    )
                })
            }
            <div className="grid grid-cols-5 justify-center pb-2">
                {entries.length > 0 &&
                    <div className="col-start-2">
                        <AddButton handleClick={() => props.handleChange(entries.length, { "new item": "" })} />
                    </div>
                }
            </div>
        </>
    );
}

export function KeyValueText(props: {
    name: string, propKey: string, value: string, index: number
    , handleChange: (index: number, value: { [prop: string]: string }) => void
    , handleRemove: (index: number) => void
}) {
    const [key, setKey] = useState(props.propKey);
    const [value, setValue] = useState(props.value);
    const handleKeyBlur = (newVal: React.FocusEvent<HTMLInputElement>) => props.handleChange(props.index, newVal.target.value ? { [newVal.target.value]: value } : {})
    const handleValueBlur = (newVal: React.FocusEvent<HTMLInputElement>) => props.handleChange(props.index, { [key]: newVal.target.value })
    const handleRemove = () => props.handleRemove(props.index)
    return (
        <div className="grid grid-cols-5 pb-2">
            {props.index === 0 &&
                <InputLabel id={props.name} text={props.name} required={false} wStyle="p-2.5 w=1/5" />
            }
            <ControllTextBox
                name={props.name}
                id={props.name}
                required={true}
                wStyle="col-start-2 p-2.5 w=1/5"
                value={key}
                handleChange={(ev) => setKey(ev.target.value)}
                handleBlur={handleKeyBlur}
            />
            <ControllTextBox
                name={props.name}
                id={props.name}
                required={true}
                wStyle="col-start-3 col-span-2 ml-1 "
                value={value}
                disabled={!key || key === ""}
                handleChange={(ev) => setValue(ev.target.value)}
                handleBlur={handleValueBlur}
            />
            {props.index > 0 &&
                <div className="col-start-5">
                    <RemoveButton handleClick={handleRemove} />
                </div>
            }
        </div>
    )
}