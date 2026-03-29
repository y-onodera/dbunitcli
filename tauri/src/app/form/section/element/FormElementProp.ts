import type { Dispatch, SetStateAction } from "react";
import type { CommandParam } from "../../../../model/CommandParam";

export type Prop = {
	prefix: string;
	element: CommandParam;
	hidden?: boolean;
	srcType?: string;
	hideDatasetSettingEdit?: boolean;
};
export type FileProp = Prop & {
	path: string;
	setPath: Dispatch<SetStateAction<string>>;
	onSelect?: () => void;
	srcType?: string;
};
export type TextProp = Prop & {
	resourceFiles?: string[];
	handleValueChange?: (value: string) => void;
};
export type SelectProp = Prop & {
	handleTypeSelect: (selected: string) => Promise<void>;
};
export type CheckProp = Prop & {
	handleOnChange?: (checked: boolean) => void;
};

export function getId(prefix: string, name: string): string {
	return prefix ? `${prefix}_${name}` : `${name}`;
}

export function getName(prefix: string, name: string): string {
	return prefix ? `-${prefix}.${name}` : `-${name}`;
}
