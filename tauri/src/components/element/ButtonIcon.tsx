import type { ReactNode } from 'react';
import { Button } from './Button';
import {
    AddIcon,
    CopyIcon,
    DeleteIcon,
    DirIcon,
    EditIcon,
    ExpandIcon,
    FileIcon,
    FixIcon,
    RemoveIcon,
    SettingIcon,
} from './Icon';

export type IconButtonProps = {
    title?: string;
    handleClick: React.MouseEventHandler<HTMLButtonElement>;
};

export type IconType = 'edit' | 'delete' | 'copy' | 'add' | 'remove' | 'setting' | 'fix' | 'file' | 'dir';

const IconComponents: Record<IconType, React.FC<{ title: string }>> = {
    edit: EditIcon,
    delete: DeleteIcon,
    copy: CopyIcon,
    add: AddIcon,
    remove: RemoveIcon,
    setting: SettingIcon,
    fix: FixIcon,
    file: FileIcon,
    dir: DirIcon,
};

export function IconButton({ iconType, title, handleClick }: IconButtonProps & { iconType: IconType }) {
    const defaultTitle = iconType;
    const finalTitle = title === undefined ? defaultTitle : title;
    const IconComponent = IconComponents[iconType];

    return (
        <ButtonIcon title={finalTitle} handleClick={handleClick}>
            <IconComponent title={finalTitle} />
        </ButtonIcon>
    );
}

export function DirectoryButton(props: IconButtonProps) {
    return <IconButton iconType="dir" {...props} />;
}

export function FileButton(props: IconButtonProps) {
    return <IconButton iconType="file" {...props} />;
}

export function EditButton(props: IconButtonProps) {
    return <IconButton iconType="edit" {...props} />;
}

export function DeleteButton(props: IconButtonProps) {
    return <IconButton iconType="delete" {...props} />;
}

export function CopyButton(props: IconButtonProps) {
    return <IconButton iconType="copy" {...props} />;
}

export function AddButton(props: IconButtonProps) {
    return <IconButton iconType="add" {...props} />;
}

export function RemoveButton(props: IconButtonProps) {
    return <IconButton iconType="remove" {...props} />;
}

export function SettingButton(props: IconButtonProps) {
    return <IconButton iconType="setting" {...props} />;
}

export function FixButton(props: IconButtonProps) {
    return <IconButton iconType="fix" {...props} />;
}

export function ExpandButton(prop: {
    toggleOptional: () => void;
    showOptional: boolean;
    caption: string | undefined;
}) {
    return (
        <ButtonIcon key={prop.caption} title="" handleClick={prop.toggleOptional}>
            <ExpandIcon close={!prop.showOptional} />
            <span className="ms-2 text-left rtl:text-right whitespace-nowrap">
                {prop.showOptional ? `Hide ${prop.caption}` : `Show ${prop.caption}`}
            </span>
        </ButtonIcon>
    );
}

export function ButtonIcon(props: {
    title?: string;
    handleClick: React.MouseEventHandler<HTMLButtonElement>;
    children: ReactNode;
}) {
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