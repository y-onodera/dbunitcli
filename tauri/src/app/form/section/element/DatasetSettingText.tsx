import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import DatasetSettingEditButton, {
	RemoveDatasetSettingButton,
} from "../../../settings/DatasetSettingEditButton";
import DatasetTableNamesPreviewButton from "../../../settings/DatasetTableNamesPreviewButton";
import type { TextProp } from "./FormElementProp";
import ResourceDropDownMenu from "./ResourceDropDownMenu";
import ResourceText from "./ResourceText";

export default function DatasetSettingText({
	prefix,
	element,
	hidden,
	hideDatasetSettingEdit,
}: TextProp) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const settings = useResourcesSettings();
	const resourceFiles = settings.datasetSettings;

	return (
		<ResourceText
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={resourceFiles}
			afterContent={({ path }) =>
				datasetSrcInfo.srcType && !hideDatasetSettingEdit ? (
					<div className="mt-2 flex items-center gap-3">
						<DatasetTableNamesPreviewButton title="Preview Before Settings" />
						{path && (
							<DatasetTableNamesPreviewButton
								title="Preview Aply Settings"
								setting={path}
							/>
						)}
					</div>
				) : null
			}
		>
			{({ path, setPath, isValueInDatalist }) => (
				<ResourceDropDownMenu
					path={path}
					setPath={setPath}
					prefix={prefix}
					element={element}
					isValueInDatalist={isValueInDatalist}
					editButtons={
						!hideDatasetSettingEdit
							? [
									<DatasetSettingEditButton
										key="edit"
										path={path}
										setPath={setPath}
									/>,
								]
							: undefined
					}
					removeButton={() => (
						<RemoveDatasetSettingButton path={path} setPath={setPath} />
					)}
				/>
			)}
		</ResourceText>
	);
}
