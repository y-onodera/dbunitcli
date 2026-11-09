import {
	FixedColumnDef,
	type FixedColumnDefBuilder,
} from "../model/FixedColumnDef";
import {
	type ResourceParser,
	useResourceLoad,
	useSaveResource,
} from "./useResourceFiles";

export const useSaveFixedColumnDef = () => {
	return useSaveResource<FixedColumnDef>("fixedColumnDef");
};

const FIXED_COLUMN_DEF_PARSER: ResourceParser<FixedColumnDef> = {
	empty: () => FixedColumnDef.create(),
	parse: (json) => FixedColumnDef.build(json as FixedColumnDefBuilder),
};

export const useFixedColumnDefData = (
	fileName: string,
): { def: FixedColumnDef; loading: boolean } => {
	const { data, loading } = useResourceLoad(
		"fixedColumnDef",
		fileName,
		FIXED_COLUMN_DEF_PARSER,
	);
	return { def: data, loading };
};
