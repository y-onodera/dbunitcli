import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import { useResourcesSettings } from "../../../context/WorkspaceResourcesProvider";
import type { DatasetSrcInfo } from "../../../model/CommandParam";
import XlsxSchemaEditButton, {
	RemoveXlsxSchemaButton,
} from "../../settings/XlsxSchemaEditButton";
import type { Prop } from "./FormElementProp";
import ResourceDropDownMenu from "./ResourceDropDownMenu";
import ResourceText from "./ResourceText";

export default function XlsxSchemaText({ prefix, element, hidden }: Prop) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();
	const settings = useResourcesSettings();

	const handleValueChange = (newValue: string) => {
		if (datasetSrcInfo) {
			setDatasetSrcInfo({
				...datasetSrcInfo,
				xlsxSchema: newValue,
			} as DatasetSrcInfo);
		}
	};

	return (
		<ResourceText
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={settings.xlsxSchemas}
			onValueChange={handleValueChange}
		>
			{({ path, setPath, isValueInDatalist }) => (
				<ResourceDropDownMenu
					path={path}
					setPath={setPath}
					prefix={prefix}
					element={element}
					isValueInDatalist={isValueInDatalist}
					editButtons={[
						<XlsxSchemaEditButton key="edit" path={path} setPath={setPath} />,
					]}
					removeButton={() => (
						<RemoveXlsxSchemaButton path={path} setPath={setPath} />
					)}
				/>
			)}
		</ResourceText>
	);
}
