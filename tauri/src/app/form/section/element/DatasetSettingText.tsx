import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import DatasetSettingEditButton, {
	RemoveDatasetSettingButton,
} from "../dialog/DatasetSettingEditButton";
import DatasetTableNamesPreviewButton from "../dialog/DatasetTableNamesPreviewButton";
import type { TextProp } from "./FormElementProp";
import Text, { TextDropDownMenu } from "./Text";

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
		<Text
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={resourceFiles}
			showDefaulePath={true}
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
				<TextDropDownMenu
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
		</Text>
	);
}
