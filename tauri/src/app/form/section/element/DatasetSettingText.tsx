import type { Dispatch, SetStateAction } from "react";
import { InlineActions } from "../../../../components/dialog";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import { srcTypeOf } from "../../../../model/CommandOption";
import { DatasetSettingGenerateButton } from "../dialog/DatasetSettingGenerateDialog";
import DatasetSettingsDialog from "../dialog/DatasetSettingsDialog";
import DatasetTableNamesPreviewButton from "../dialog/DatasetTableNamesPreviewButton";
import {
	EditResourceButton,
	RemoveResourceButton,
} from "../dialog/ResourceEditButton";
import type { TextProp } from "./FormElementProp";
import Text, { TextDropDownMenu } from "./Text";

export default function DatasetSettingText({
	prefix,
	element,
	hidden,
	hideDatasetSettingEdit,
	handleValueChange,
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
			handleValueChange={handleValueChange}
			afterContent={({ path }) =>
				srcTypeOf(datasetSrcInfo) && !hideDatasetSettingEdit ? (
					<InlineActions>
						<DatasetTableNamesPreviewButton
							title="Preview Before Settings"
							setting=""
						/>
						{path && (
							<DatasetTableNamesPreviewButton
								title="Preview Apply Settings"
								setting={path}
							/>
						)}
					</InlineActions>
				) : null
			}
		>
			{({ path, setPath, isValueInDatalist }) => {
				const handleSetPath: Dispatch<SetStateAction<string>> = (action) => {
					const newPath = typeof action === "function" ? action(path) : action;
					setPath(newPath);
					handleValueChange?.(newPath);
				};
				return (
					<TextDropDownMenu
						path={path}
						setPath={handleSetPath}
						prefix={prefix}
						element={element}
						isValueInDatalist={isValueInDatalist}
						editButtons={
							!hideDatasetSettingEdit
								? [
										<EditResourceButton
											dialog={DatasetSettingsDialog}
											key="edit"
											path={path}
											setPath={handleSetPath}
										/>,
										<DatasetSettingGenerateButton
											key="generate"
											path={path}
											setPath={handleSetPath}
										/>,
									]
								: undefined
						}
						removeButton={(closeMenu) => (
							<RemoveResourceButton
								kind="datasetSetting"
								path={path}
								setPath={handleSetPath}
								onRemoved={closeMenu}
							/>
						)}
					/>
				);
			}}
		</Text>
	);
}
