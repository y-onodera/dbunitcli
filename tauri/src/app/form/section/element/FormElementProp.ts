import type { Dispatch, ReactNode, SetStateAction } from "react";
import type { CommandOption } from "../../../../model/CommandOption";

export type Prop = {
	prefix: string;
	element: CommandOption;
	hidden?: boolean;
	srcType?: string;
	hideDatasetSettingEdit?: boolean;
};
export type FileProp = Prop & {
	path: string;
	setPath: Dispatch<SetStateAction<string>>;
	onSelect?: () => void;
};
export type TextProp = Prop & {
	resourceFiles?: string[];
	showDefaulePath?: boolean;
	afterContent?: (args: { path: string }) => ReactNode;
	children?: (args: {
		path: string;
		setPath: Dispatch<SetStateAction<string>>;
		isValueInDatalist?: boolean;
	}) => ReactNode;
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
