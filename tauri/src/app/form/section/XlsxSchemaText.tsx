import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import { useResourcesSettings } from "../../../context/WorkspaceResourcesProvider";
import type { DatasetSrcInfo } from "../../../model/CommandParam";
import type { Prop } from "./FormElementProp";
import ResourceText from "./ResourceText";
import XlsxSchemaDropDownMenu from "./XlsxSchemaDropDownMenu";

export default function XlsxSchemaText({
	prefix,
	element,
	hidden,
	srcType,
}: Prop) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();
	const settings = useResourcesSettings();

	const handleValueChange = (newValue: string) => {
		if (datasetSrcInfo) {
			setDatasetSrcInfo({ ...datasetSrcInfo, xlsxSchema: newValue } as DatasetSrcInfo);
		}
	};

	return (
		<ResourceText
			prefix={prefix}
			element={element}
			hidden={hidden}
			srcType={srcType}
			resourceFiles={settings.xlsxSchemas}
			onValueChange={handleValueChange}
		>
			{({ path, setPath, isValueInDatalist }) => (
				<XlsxSchemaDropDownMenu
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
