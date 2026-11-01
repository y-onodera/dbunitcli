import type { ComponentType, Dispatch, SetStateAction } from "react";
import { InlineActions } from "../../../../components/dialog";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import { srcTypeOf } from "../../../../model/CommandOption";
import {
	DatasetSettingGenerateButton,
	type DatasetSettingsEditDialogComponent,
} from "../dialog/DatasetSettingGenerateDialog";
import DatasetSettingsDialog, {
	DatasetSettingsEditDialog,
} from "../dialog/DatasetSettingsDialog";
import DatasetTableNamesPreviewButton from "../dialog/DatasetTableNamesPreviewButton";
import {
	EditResourceButton,
	type EditResourceDialogProps,
	RemoveResourceButton,
} from "../dialog/ResourceEditButton";
import type { TextProp } from "./FormElementProp";
import Text, { TextDropDownMenu } from "./Text";

// editDialog: 編集ボタンで開くダイアログ、generateEditDialog: 生成結果を確認・保存するダイアログ。
// 省略するとDataset Settingsを使う（compareの-settingはCompare Key Settingsを渡し、Previewも出さない）
export default function DatasetSettingText({
	prefix,
	element,
	hidden,
	handleValueChange,
	editDialog = DatasetSettingsDialog,
	generateEditDialog = DatasetSettingsEditDialog,
	showPreview = true,
}: TextProp & {
	editDialog?: ComponentType<EditResourceDialogProps>;
	generateEditDialog?: DatasetSettingsEditDialogComponent;
	showPreview?: boolean;
}) {
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
				showPreview && srcTypeOf(datasetSrcInfo) ? (
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
						editButtons={[
							<EditResourceButton
								dialog={editDialog}
								key="edit"
								path={path}
								setPath={handleSetPath}
							/>,
							<DatasetSettingGenerateButton
								key="generate"
								setPath={handleSetPath}
								editDialog={generateEditDialog}
							/>,
						]}
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
