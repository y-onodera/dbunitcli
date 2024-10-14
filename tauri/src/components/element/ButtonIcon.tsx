import type { ReactNode } from "react";
import { Button } from "./Button";
import { AddIcon, CopyIcon, DeleteIcon, EditIcon, FixIcon, RemoveIcon, SettingIcon } from "./Icon";

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
export function RemoveButton(props: { title?: string, handleClick: React.MouseEventHandler<HTMLButtonElement> }) {
    return (
        <ButtonIcon title={props.title === undefined ? "remove" : props.title} handleClick={props.handleClick}>
            <RemoveIcon title={props.title ? props.title : "remove"} />
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
        <Button
            buttonstyle="flex items-center group p-1"
            bgcolor=""
            textstyle="text-gray-500 hover:text-blue-600"
            border="outline-none"
            handleClick={props.handleClick}
        >
            {props.children}
            {props.title}
        </Button>
    );
}