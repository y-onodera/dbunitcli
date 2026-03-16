import type { Dispatch, SetStateAction } from "react";
import type { CommandParam, DatasetSrcInfo, SrcInfo } from "../../model/CommandParam";

export type Prop = {
	prefix: string;
	element: CommandParam;
	hidden?: boolean;
	srcType?: string;
	srcInfo?: SrcInfo;
	datasetSrcInfo?: DatasetSrcInfo;
};
export type FileProp = Prop & {
	path: string;
	setPath: Dispatch<SetStateAction<string>>;
	onSelect?: () => void;
	srcType?: string;
	srcInfo?: SrcInfo;
	datasetSrcInfo?: DatasetSrcInfo;
};
export type SelectProp = Prop & {
	handleTypeSelect: (selected: string) => Promise<void>;
};
