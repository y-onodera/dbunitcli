export function EditIcon(props: { title?: string, fill?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            height="24"
            viewBox="0 0 24 24"
            width="24"
            fill={props.fill ?? "#5f6368"}
        >
            <title>{props.title ?? "edit"}</title>
            <path d="M0 0h24v24H0z" fill="none" />
            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
        </svg>
    )
}
export function AddIcon(props: { title?: string, fill?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill={props.fill ?? "#5f6368"}
        >
            <title>{props.title ?? "add"}</title>
            <path d="M440-440H200v-80h240v-240h80v240h240v80H520v240h-80v-240Z" />
        </svg>
    )
}
export function RemoveIcon(props: { title?: string, fill?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 0 24 24"
            width="24px"
            fill={props.fill ?? "#5f6368"}
        >
            <title>{props.title ?? "remove"}</title>
            <path d="M0 0h24v24H0z" fill="none" />
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm5 11H7v-2h10v2z" />
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
            fill={props.fill ?? "#5f6368"}
        >
            <title>{props.title ?? "copy"}</title>
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
            fill={props.fill ?? "#5f6368"}
        >
            <title>{props.title ?? "delete"}</title>
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
            fill={props.fill ?? "#5f6368"}
        >
            <title>{props.title ?? "directory"}</title>
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
            fill={props.fill ?? "#5f6368"}
        >
            <title>{props.title ?? "file"}</title>
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
            fill={props.fill ?? "#5f6368"}
        >
            <title>{props.title ?? "setting"}</title>
            <path d="M480-160q-33 0-56.5-23.5T400-240q0-33 23.5-56.5T480-320q33 0 56.5 23.5T560-240q0 33-23.5 56.5T480-160Zm0-240q-33 0-56.5-23.5T400-480q0-33 23.5-56.5T480-560q33 0 56.5 23.5T560-480q0 33-23.5 56.5T480-400Zm0-240q-33 0-56.5-23.5T400-720q0-33 23.5-56.5T480-800q33 0 56.5 23.5T560-720q0 33-23.5 56.5T480-640Z" />
        </svg>
    )
}
export function FixIcon(props: { title?: string, fill?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill={props.fill ?? "#5f6368"}
        >
            <title>{props.title ?? "fix"}</title>
            <path d="M382-240 154-468l57-57 171 171 367-367 57 57-424 424Z" />
        </svg>
    )
}
export function SettingsApplicationIcon(props: { title?: string, fill?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill={props.fill ?? "#5f6368"}
        >
            <title>{props.title ?? "settingsApplication"}</title>
            <path d="M120-160v-80h110l-16-14q-52-46-73-105t-21-119q0-111 66.5-197.5T360-790v84q-72 26-116 88.5T200-478q0 45 17 87.5t53 78.5l10 10v-98h80v240H120Zm717-360h-81q-5-35-21.5-67T690-648l-10-10v98h-80v-240h240v80H730l16 14q41 42 63 89t28 97ZM680-40l-12-60q-12-5-22.5-10.5T624-124l-58 18-40-68 46-40q-2-14-2-26t2-26l-46-40 40-68 58 18q11-8 21.5-13.5T668-380l12-60h80l12 60q12 5 22.5 11t21.5 15l58-20 40 70-46 40q2 12 2 25t-2 25l46 40-40 68-58-18q-11 8-21.5 13.5T772-100l-12 60h-80Zm40-120q33 0 56.5-23.5T800-240q0-33-23.5-56.5T720-320q-33 0-56.5 23.5T640-240q0 33 23.5 56.5T720-160Z" />
        </svg>
    )
}
export function PreviewIcon(props: { title?: string, fill?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill={props.fill ?? "#5f6368"}
        >
            <title>{props.title ?? "preview"}</title>
            <path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-480H200v480Zm133.5-124.5Q269-369 240-440q29-71 93.5-115.5T480-600q82 0 146.5 44.5T720-440q-29 71-93.5 115.5T480-280q-82 0-146.5-44.5Zm248.5-42q46-26.5 72-73.5-26-47-72-73.5T480-540q-56 0-102 26.5T306-440q26 47 72 73.5T480-340q56 0 102-26.5ZM480-440Zm42.5 42.5Q540-415 540-440t-17.5-42.5Q505-500 480-500t-42.5 17.5Q420-465 420-440t17.5 42.5Q455-380 480-380t42.5-17.5Z" />
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
            <title>{props.title ?? "expand"}</title>
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
