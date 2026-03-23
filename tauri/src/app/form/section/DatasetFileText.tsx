import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import type { DatasetSrcInfo } from "../../../model/CommandParam";
import ResourceDropDownMenu from "./ResourceDropDownMenu";
import type { Prop } from "./FormElementProp";
import ResourceText from "./ResourceText";

export default function DatasetFileText({
	prefix,
	element,
	hidden,
	srcType,
}: Prop) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();

	const handleValueChange = (newValue: string) => {
		if (datasetSrcInfo && element.name in datasetSrcInfo) {
			setDatasetSrcInfo({
				...datasetSrcInfo,
				[element.name]: newValue,
			} as DatasetSrcInfo);
		}
	};

	return (
		<ResourceText
			prefix={prefix}
			element={element}
			hidden={hidden}
			srcType={srcType}
			resourceFiles={[]}
			onValueChange={handleValueChange}
		>
			{({ path, setPath }) => (
				<ResourceDropDownMenu
					path={path}
					setPath={setPath}
					prefix={prefix}
					element={element}
					srcType={srcType}
				/>
			)}
		</ResourceText>
	);
}
