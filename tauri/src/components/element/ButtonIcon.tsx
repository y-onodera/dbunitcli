import type { ReactNode } from "react";
import { AddIcon, CopyIcon, DeleteIcon, EditIcon, FixIcon, SettingIcon } from "./Icon";

export function EditButton(props: { title?: string, handleClick: React.MouseEventHandler<HTMLButtonElement> }) {
    return (
        <ButtonIcon title={props.title === undefined ? "edit" : props.title} handleClick={props.handleClick}>
            <EditIcon title={props.title ? props.title : "edit"} />
        </ButtonIcon>
    );
}
export function DeleteButton(props: { title?: string, handleClick: React.MouseEventHandler<HTMLButtonElement> }) {
    return (
        <ButtonIcon title={props.title === undefined ? "delete" : props.title} handleClick={props.handleClick}>
            <DeleteIcon title={props.title ? props.title : "delete"} />
        </ButtonIcon>
    );
}
export function CopyButton(props: { title?: string, handleClick: React.MouseEventHandler<HTMLButtonElement> }) {
    return (
        <ButtonIcon title={props.title === undefined ? "copy" : props.title} handleClick={props.handleClick}>
            <CopyIcon title={props.title ? props.title : "copy"} />
        </ButtonIcon>
    );
}
export function AddButton(props: { title?: string, handleClick: React.MouseEventHandler<HTMLButtonElement> }) {
    return (
        <ButtonIcon title={props.title === undefined ? "add" : props.title} handleClick={props.handleClick}>
            <AddIcon title={props.title ? props.title : "add"} />
        </ButtonIcon>
    );
}
export function SettingButton(props: { title?: string, handleClick: React.MouseEventHandler<HTMLButtonElement> }) {
    return (
        <ButtonIcon title={props.title === undefined ? "setting" : props.title} handleClick={props.handleClick}>
            <SettingIcon title={props.title ? props.title : "setting"} />
        </ButtonIcon>
    );
}
export function FixButton(props: { title?: string, handleClick: React.MouseEventHandler<HTMLButtonElement> }) {
    return (
        <ButtonIcon title={props.title === undefined ? "fix" : props.title} handleClick={props.handleClick}>
            <FixIcon title={props.title ? props.title : "fix"} />
        </ButtonIcon>
    );
}
export function ButtonIcon(props: { title?: string, handleClick: React.MouseEventHandler<HTMLButtonElement>, children: ReactNode }) {
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