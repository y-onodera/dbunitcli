import type { ReactNode } from "react";

export function ButtonWithIcon(props: { id: string, handleClick: React.MouseEventHandler<HTMLButtonElement>, children: ReactNode }) {
    return (
        <Button
            buttonstyle="p-2.5 ms-2"
            bgcolor="bg-indigo-500 hover:bg-indigo-600"
            textstyle="font-medium text-sm text-white"
            border="border border-gray-300"
            handleClick={props.handleClick} >
            {props.children}
        </Button>
    );
}
export function BlueButton(props: { title: string, handleClick: React.MouseEventHandler<HTMLButtonElement> }) {
    return (
        <Button
            buttonstyle=""
            bgcolor="bg-indigo-500 hover:bg-indigo-600"
            textstyle="text-center text-sm font-semibold text-white"
            border="border border-gray-300"
            handleClick={props.handleClick} >
            {props.title}
        </Button>
    );
}
export function WhiteButton(props: { title: string, handleClick: React.MouseEventHandler<HTMLButtonElement> }) {
    return (
        <Button
            buttonstyle=""
            bgcolor="bg-white hover:bg-gray-300"
            textstyle="text-center text-sm font-semibold text-gray-500"
            border="border border-gray-300"
            handleClick={props.handleClick} >
            {props.title}
        </Button>
    );
}
export function LinkButton(props: { title: string, handleClick: React.MouseEventHandler<HTMLButtonElement> }) {
    return (
        <Button
            buttonstyle="flex items-center justify-start w-full p-1 ms-2"
            bgcolor="hover:bg-gray-100"
            textstyle="text-gray-500 hover:text-blue-600"
            border="outline-none"
            handleClick={props.handleClick} >
            {props.title}
        </Button>
    );
}
export function Button(props: {
    buttonstyle: string
    , bgcolor: string
    , textstyle: string
    , border: string
    , children: ReactNode
    , handleClick: React.MouseEventHandler<HTMLButtonElement>
}) {
    return (
        <button
            type="button"
            className={`${props.buttonstyle} 
                         ${props.textstyle} 
                         ${props.bgcolor} 
                         rounded-lg 
                         ${props.border} 
                         transition duration-100 
                         ring-indigo-300 
                         focus-visible:ring`}
            onClick={props.handleClick}
        >
            {props.children}
        </button>
    );
}
