import {
	FixedColumnDef,
	type FixedColumnDefBuilder,
} from "../model/FixedColumnDef";
import { type ResourceParser, useSaveResource } from "./useResourceFiles";

export const useSaveFixedColumnDef = () => {
	return useSaveResource<FixedColumnDef>("fixedColumnDef");
};

export const FIXED_COLUMN_DEF_PARSER: ResourceParser<FixedColumnDef> = {
	empty: () => FixedColumnDef.create(),
	parse: (content) => FixedColumnDef.build(content as FixedColumnDefBuilder),
};
