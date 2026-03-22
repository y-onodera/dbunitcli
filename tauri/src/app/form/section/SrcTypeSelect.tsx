import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import type { SelectProp } from "./FormElementProp";
import PlainSelect from "./PlainSelect";

export default function SrcTypeSelect(prop: SelectProp) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();

	const handleTypeSelect = async (selected: string) => {
		await prop.handleTypeSelect(selected);
		if (datasetSrcInfo) {
			setDatasetSrcInfo({ ...datasetSrcInfo, srcType: selected });
		}
	};

	return <PlainSelect {...prop} handleTypeSelect={handleTypeSelect} />;
}
