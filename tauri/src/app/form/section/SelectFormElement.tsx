import { InputLabel, SelectBox } from "../../../components/element/Input";
import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import type { SelectProp } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

export default function Select(prop: SelectProp) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();

	const handleTypeSelect = async (selected: string) => {
		await prop.handleTypeSelect(selected);
		if (prop.element.name === "srcType" && datasetSrcInfo) {
			setDatasetSrcInfo({ ...datasetSrcInfo, srcType: selected });
		}
	};

	return (
		<div>
			<InputLabel
				text={getName(prop.prefix, prop.element.name)}
				id={getId(prop.prefix, prop.element.name)}
				required={prop.element.attribute.required}
				hidden={prop.hidden}
			/>
			<SelectBox
				name={getName(prop.prefix, prop.element.name)}
				id={getId(prop.prefix, prop.element.name)}
				required={true}
				hidden={prop.hidden}
				handleOnChange={handleTypeSelect}
				defaultValue={prop.element.value}
			>
				{prop.element.attribute.selectOption.map((value) => {
					return (
						<option key={prop.prefix + prop.element.name + value} value={value}>
							{value}
						</option>
					);
				})}
			</SelectBox>
		</div>
	);
}
