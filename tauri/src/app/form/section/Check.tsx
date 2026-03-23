import { CheckBox, InputLabel } from "../../../components/element/Input";
import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import type { DatasetSrcInfo } from "../../../model/CommandParam";
import type { Prop } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

export default function Check(prop: Prop) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();

	const handleOnChange = (checked: boolean) => {
		if (datasetSrcInfo && prop.element.name in datasetSrcInfo) {
			setDatasetSrcInfo({
				...datasetSrcInfo,
				[prop.element.name]: checked,
			} as DatasetSrcInfo);
		}
	};

	return (
		<div>
			<InputLabel
				text={getName(prop.prefix, prop.element.name)}
				id={getId(prop.prefix, prop.element.name)}
				required={false}
				hidden={prop.hidden}
			/>
			<CheckBox
				name={getName(prop.prefix, prop.element.name)}
				id={getId(prop.prefix, prop.element.name)}
				hidden={prop.hidden}
				defaultValue={prop.element.value}
				handleOnChange={handleOnChange}
			/>
		</div>
	);
}
