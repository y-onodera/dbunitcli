import type { ReactNode } from "react";

export function ButtonWithIcon(props: { id: string, handleClick: () => void, children: ReactNode }) {
    return (
        <button
            type="button"
            onClick={props.handleClick}
            id={props.id}
            className="p-2.5 ms-2 
                        font-medium text-sm text-white 
                        bg-indigo-500 
                        rounded-lg 
                        border border-gray-300 
                        ring-indigo-300 
                        focus-visible:ring 
                        hover:bg-indigo-600 "
        >
            {props.children}
        </button>
    );
}
export function BlueButton(props: { title: string, handleClick: () => void }) {
    return (
        <Button bgcolor="bg-indigo-500 hover:bg-indigo-600" textstyle="font-semibold text-white" title={props.title} handleClick={props.handleClick} />
    );
}
export function WhiteButton(props: { title: string, handleClick: () => void }) {
    return (
        <Button bgcolor="bg-white hover:bg-gray-600" textstyle="font-semibold text-gray-500" title={props.title} handleClick={props.handleClick} />
    );
}
function Button(props: { bgcolor: string, textstyle: string, title: string, handleClick: () => void }) {
    return (
        <button
            type="button"
            className={`text-center text-sm ${props.textstyle} 
                         ${props.bgcolor} 
                         rounded-e-lg rounded-s-gray-100 rounded-s-2 
                         border border-gray-300 
                         transition duration-100 
                         ring-indigo-300 
                         focus-visible:ring`}
            onClick={props.handleClick}
        >
            {props.title}
        </button>
    );
}
export function LinkButton(props: { title: string, handleClick: () => void }) {
    return (
        <button
            type="button"
            onClick={props.handleClick}
            className="flex items-center justify-start
                        w-full 
                        p-1 ms-2 
                        text-gray-500 
                        rounded-lg 
                        outline-none 
                        ring-indigo-300 
                        focus-visible:ring
                        hover:bg-gray-100
                        hover:text-blue-600 "
        >
            {props.title}
        </button>
    );
}
