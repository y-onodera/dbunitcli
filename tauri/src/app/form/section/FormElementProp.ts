import type { Dispatch, SetStateAction } from "react";
import type { CommandParam } from "../../../model/CommandParam";

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
export type SelectProp = Prop & {
	handleTypeSelect: (selected: string) => Promise<void>;
};
