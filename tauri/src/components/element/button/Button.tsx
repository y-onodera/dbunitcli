import type { ReactNode } from "react";

export function ButtonWithIcon(props: { id: string, handleClick: () => void, children: ReactNode }) {
    return (
        <button
            type="button"
            onClick={props.handleClick}
            id={props.id}
            className="p-2.5 
               ms-2 
               text-sm 
               font-medium 
               text-white 
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
        <Button bgcolor="indigo-500" ringcolor="indigo" textstyle="font-semibold text-white" title={props.title} handleClick={props.handleClick} />
    );
}
export function WhiteButton(props: { title: string, handleClick: () => void }) {
    return (
        <Button bgcolor="white" ringcolor="gray" textstyle="font-medium text-gray-900" title={props.title} handleClick={props.handleClick} />
    );
}
function Button(props: { bgcolor: string, ringcolor: string, textstyle: string, title: string, handleClick: () => void }) {
    return (
        <button
            type="button"
            className={`text-center text-sm ${props.textstyle} 
             bg-${props.bgcolor} 
             rounded-e-lg rounded-s-gray-100 rounded-s-2 
             border border-gray-300 
             transition 
             duration-100 
             ring-${props.ringcolor}-300 
             focus-visible:ring 
             hover:bg-${props.ringcolor}-600 
             active:bg-${props.ringcolor}-700 
             md:text-base`}
            onClick={props.handleClick}
        >
            {props.title}
        </button>
    );
}