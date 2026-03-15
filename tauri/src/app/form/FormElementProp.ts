import type { Dispatch, SetStateAction } from "react";
import type { CommandParam, SrcInfo } from "../../model/CommandParam";

export type { SrcInfo };

export type Prop = {
	prefix: string;
	element: CommandParam;
	hidden?: boolean;
	srcType?: string;
	srcInfo?: SrcInfo;
};
export type FileProp = Prop & {
	path: string;
	setPath: Dispatch<SetStateAction<string>>;
	onSelect?: () => void;
	srcType?: string;
	srcInfo?: SrcInfo;
};
export type SelectProp = Prop & {
	handleTypeSelect: (selected: string) => Promise<void>;
};
