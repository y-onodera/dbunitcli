import { type ReactNode, useState } from "react";

export function InputLabel(props: {
    text: string, id: string, required: boolean, hidden?: boolean, wStyle?: string
}) {
    return (
        <label
            htmlFor={props.id}
            style={props.hidden ? { display: "none" } : {}}
            className={`block 
                   ${props.wStyle ? props.wStyle : "w-full"}
                   font-medium text-sm text-gray-900 
                   `}
        >
            {props.text}{props.required && "*"}
        </label>
    )
}
export function ControllTextBox(props: {
    name: string, id: string, required: boolean, value: string
    , wStyle?: string, list?: string, disabled?: boolean, hidden?: boolean
    , handleChange: (ev: React.ChangeEvent<HTMLInputElement>) => void
    , handleBlur?: (ev: React.FocusEvent<HTMLInputElement>) => void
}) {
    return (
        <input
            name={props.name}
            id={props.id}
            type="text"
            list={props.list ?? ""}
            style={props.hidden ? { display: "none" } : {}}
            className={inputStyle(props.wStyle ? props.wStyle : "w-full")}
            required={props.required}
            disabled={!!props.disabled}
            value={props.value}
            onChange={props.handleChange}
            onBlur={ev => props.handleBlur?.(ev)}
            autoComplete={props.list ? "off" : "on"}
        />
    )
}
export function SelectBox(props: { name: string, id: string, required: boolean, hidden?: boolean, wStyle?: string, defaultValue?: string, handleOnChange?: (selected: string) => Promise<void>, children: ReactNode }) {
    const [selected, setSelected] = useState(props.defaultValue ? props.defaultValue : "");
    return (
        <select
            name={props.name}
            id={props.id}
            className={inputStyle(props.wStyle ? props.wStyle : "w-40")}
            required={props.required}
            style={props.hidden ? { display: "none" } : {}}
            value={selected}
            onChange={(event) => {
                setSelected(event.currentTarget.value);
                props.handleOnChange?.(event.currentTarget.value);
            }}
        >
            {props.children}
        </select>
    );
}
export function CheckBox(props: { name: string, id: string, hidden?: boolean, defaultValue?: string, handleOnChange?: (checked: boolean) => void }) {
    const [checked, setChecked] = useState(props.defaultValue === "true");
    return (
        <>
            <input
                name={props.name}
                id={props.id}
                type="checkbox"
                className="w-4 h-4
                           text-indigo-500
                           border border-gray-300
                           ring-indigo-300
                           focus-visible:ring-3 "
                style={props.hidden ? { display: "none" } : {}}
                checked={checked}
                value={`${checked}`}
                onChange={() => {
                    setChecked(!checked);
                    props.handleOnChange?.(!checked)
                }}
            />
            <input
                name={props.name}
                id={`${props.id}hidden`}
                type="hidden"
                value={`${checked}`}
            />
        </>
    )
}
function inputStyle(wStyle: string) {
    return `block 
               p-2.5 
               ${wStyle} 
               z-20 
               text-sm text-gray-900
               rounded-lg 
               bg-gray-50 
               disabled:bg-gray-300
               border border-gray-300 
               ring-indigo-300
               focus-visible:ring-3 `
}