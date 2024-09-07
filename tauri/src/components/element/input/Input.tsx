import { type ReactNode, useEffect, useState } from "react";

export function InputLabel(props: { name: string, id: string, required: boolean }) {
    return (
        <label
            htmlFor={props.id}
            className="block 
                   mb-2 
                   text-sm text-gray-900 
                   font-medium "
        >
            {props.name}{props.required && "*"}
        </label>
    )
}
export function CheckBox(props: { name: string, id: string, defaultValue?: string }) {
    const [checked, setChecked] = useState(false);
    useEffect(() => {
        setChecked(props.defaultValue === "true");
    }, [props.defaultValue]);
    return (
        <>
            <input
                name={props.name}
                id={props.id}
                type="checkbox"
                className="w-4 h-4 
                           text-indigo-500 
                           bg-gray-50 
                           border border-gray-300 
                           ring-indigo-300 
                           focus-visible:ring "
                checked={checked}
                value={`${checked}`}
                onChange={() => {
                    setChecked(!checked);
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
export function TextBox(props: { name: string, id: string, required: boolean, defaultValue?: string }) {
    return (
        <input
            name={props.name}
            id={props.id}
            type="text"
            className={inputStyle("w-full")}
            required={props.required}
            defaultValue={props.defaultValue}
        />
    )
}
export function SelectBox(props: { name: string, id: string, required: boolean, defaultValue?: string, handleOnChange?: () => Promise<void>, children: ReactNode }) {
    const [selected, setSelected] = useState("");
    useEffect(() => {
        if (props.defaultValue) {
            setSelected(props.defaultValue);
        }
    }, [props.defaultValue]);
    return (
        <select
            name={props.name}
            id={props.id}
            className={inputStyle("w-40")}
            required={props.required}
            value={selected}
            onChange={(event) => {
                setSelected(event.currentTarget.value);
                props.handleOnChange?.();
            }}
        >
            {props.children}
        </select>
    );
}
function inputStyle(w: string) {
    return `block 
               ${w} 
               p-2.5 
               z-20 
               bg-gray-50 
               text-sm text-gray-900
               rounded-lg 
               border border-gray-300 
               ring-indigo-300 
               focus:ring 
               focus-visible:ring `
}