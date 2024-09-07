export function EditIcon(props: { title?: string, fill?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            height="24"
            viewBox="0 0 24 24"
            width="24"
            fill={props.fill ? props.fill : "#5f6368"}
        >
            <title>{props.title ? props.title : "edit"}</title>
            <path d="M0 0h24v24H0z" fill="none" />
            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
        </svg>
    )
} export function AddIcon(props: { title?: string, fill?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill={props.fill ? props.fill : "#5f6368"}
        >
            <title>{props.title ? props.title : "add"}</title>
            <path d="M440-440H200v-80h240v-240h80v240h240v80H520v240h-80v-240Z" />
        </svg>
    )
}
export function CopyIcon(props: { title?: string, fill?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            height="24"
            viewBox="0 0 24 24"
            width="24"
            fill={props.fill ? props.fill : "#5f6368"}
        >
            <title>{props.title ? props.title : "copy"}</title>
            <path d="M0 0h24v24H0z" fill="none" />
            <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z" />
        </svg>
    )
}
export function DeleteIcon(props: { title?: string, fill?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            width="24px"
            viewBox="0 -960 960 960"
            fill={props.fill ? props.fill : "#5f6368"}
        >
            <title>{props.title ? props.title : "delete"}</title>
            <path d="M280-120q-33 0-56.5-23.5T200-200v-520h-40v-80h200v-40h240v40h200v80h-40v520q0 33-23.5 56.5T680-120H280Zm400-600H280v520h400v-520ZM360-280h80v-360h-80v360Zm160 0h80v-360h-80v360ZM280-720v520-520Z" />
        </svg>
    )
}
export function DirIcon(props: { title?: string, fill?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill={props.fill ? props.fill : "#5f6368"}
        >
            <title>{props.title ? props.title : "directory"}</title>
            <path d="M160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h240l80 80h320q33 0 56.5 23.5T880-640H447l-80-80H160v480l96-320h684L837-217q-8 26-29.5 41.5T760-160H160Zm84-80h516l72-240H316l-72 240Zm0 0 72-240-72 240Zm-84-400v-80 80Z" />
        </svg>
    )
}
export function FileIcon(props: { title?: string, fill?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill={props.fill ? props.fill : "#5f6368"}
        >
            <title>{props.title ? props.title : "file"}</title>
            <path d="M240-80q-33 0-56.5-23.5T160-160v-640q0-33 23.5-56.5T240-880h320l240 240v240h-80v-200H520v-200H240v640h360v80H240Zm638 15L760-183v89h-80v-226h226v80h-90l118 118-56 57Zm-638-95v-640 640Z" />
        </svg>
    )
}
export function SettingIcon(props: { title?: string, fill?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill={props.fill ? props.fill : "#5f6368"}
        >
            <title>{props.title ? props.title : "setting"}</title>
            <path d="M480-160q-33 0-56.5-23.5T400-240q0-33 23.5-56.5T480-320q33 0 56.5 23.5T560-240q0 33-23.5 56.5T480-160Zm0-240q-33 0-56.5-23.5T400-480q0-33 23.5-56.5T480-560q33 0 56.5 23.5T560-480q0 33-23.5 56.5T480-400Zm0-240q-33 0-56.5-23.5T400-720q0-33 23.5-56.5T480-800q33 0 56.5 23.5T560-720q0 33-23.5 56.5T480-640Z" />
        </svg>
    )
}
export function ExpandIcon(props: { title?: string, close: boolean }) {
    return (
        <svg
            className="w-3 h-3"
            aria-hidden="true"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 10 6"
            fill="none"
        >
            <title>{props.title ? props.title : "expand"}</title>
            <path
                stroke="currentColor"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="m1 1 4 4 4-4"
                transform={props.close ? "rotate(270,5,2.5)" : ""}
            />
        </svg>
    )
}