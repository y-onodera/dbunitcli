import type { ReactNode } from "react";
import { AddIcon, CopyIcon, DeleteIcon, EditIcon } from "./Icon";

export function EditButton(prop: { title?: string, handleClick: () => void }) {
    return (
        <ButtonIcon title={prop.title ? prop.title : "edit"} handleClick={prop.handleClick}>
            <EditIcon title={prop.title ? prop.title : "edit"} />
        </ButtonIcon>
    );
}
export function DeleteButton(prop: { title?: string, handleClick: () => void }) {
    return (
        <ButtonIcon title={prop.title ? prop.title : "delete"} handleClick={prop.handleClick}>
            <DeleteIcon title={prop.title ? prop.title : "delete"} />
        </ButtonIcon>
    );
}
export function CopyButton(prop: { title?: string, handleClick: () => void }) {
    return (
        <ButtonIcon title={prop.title ? prop.title : "copy"} handleClick={prop.handleClick}>
            <CopyIcon title={prop.title ? prop.title : "copy"} />
        </ButtonIcon>
    );
}
export function AddButton(prop: { title?: string, handleClick: () => void }) {
    return (
        <ButtonIcon title={prop.title ? prop.title : "add"} handleClick={prop.handleClick}>
            <AddIcon title={prop.title ? prop.title : "add"} />
        </ButtonIcon>
    );
}
export function ButtonIcon(props: { title?: string, handleClick: () => void, children: ReactNode }) {
    return (
        <button
            type="button"
            onClick={props.handleClick}
            className="flex items-center group
                        p-1 
                        text-gray-500
                        ring-indigo-300 
                        focus:ring 
                        focus-visible:ring
                        hover:text-blue-600"
        >
            {props.children}
            {props.title}
        </button>
    );
}