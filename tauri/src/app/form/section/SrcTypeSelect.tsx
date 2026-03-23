import { useCallback } from "react";
import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import type { SelectProp } from "./FormElementProp";
import Select from "./Select";

export default function SrcTypeSelect(prop: SelectProp) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();

	const handleTypeSelect = useCallback(
		async (selected: string) => {
			await prop.handleTypeSelect(selected);
			if (datasetSrcInfo) {
				setDatasetSrcInfo({ ...datasetSrcInfo, srcType: selected });
			}
		},
		[datasetSrcInfo, setDatasetSrcInfo, prop.handleTypeSelect],
	);

	return <Select {...prop} handleTypeSelect={handleTypeSelect} />;
}
