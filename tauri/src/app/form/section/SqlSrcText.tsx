import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import { useResourcesSettings } from "../../../context/WorkspaceResourcesProvider";
import type { DatasetSrcInfo } from "../../../model/CommandParam";
import type { Prop } from "./FormElementProp";
import ResourceText from "./ResourceText";
import SqlSrcDropDownMenu from "./SqlSrcDropDownMenu";

export default function SqlSrcText({ prefix, element, hidden, srcType }: Prop) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();
	const settings = useResourcesSettings();
	const resourceFiles = settings.querys(srcType);

	const handleValueChange = (newValue: string) => {
		if (datasetSrcInfo) {
			setDatasetSrcInfo({
				...datasetSrcInfo,
				srcPath: newValue,
			} as DatasetSrcInfo);
		}
	};

	return (
		<ResourceText
			prefix={prefix}
			element={element}
			hidden={hidden}
			srcType={srcType}
			resourceFiles={resourceFiles}
			onValueChange={handleValueChange}
		>
			{({ path, setPath, isValueInDatalist }) => (
				<SqlSrcDropDownMenu
					path={path}
					setPath={setPath}
					prefix={prefix}
					element={element}
					srcType={srcType}
					isValueInDatalist={isValueInDatalist}
				/>
			)}
		</ResourceText>
	);
}
