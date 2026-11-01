import type { ComponentType, Dispatch, ReactNode, SetStateAction } from "react";
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
// 省略するとDataset Settingsを使う（compareの-settingはCompare Key Settingsを渡す）。
// showGenerate=falseでgenerateボタンを出さない（generateの-unitSettingは雛型生成が用途に合わないため）。
// preview: 横に出すプレビュー。省略するとsrcのsettingの適用前後のPreviewボタン、nullなら出さない
// （compareの-settingはnull、generateの-unitSettingはunitSettingのプレビューを渡す）
export default function DatasetSettingText({
	prefix,
	element,
	hidden,
	handleValueChange,
	editDialog = DatasetSettingsDialog,
	generateEditDialog = DatasetSettingsEditDialog,
	showGenerate = true,
	preview = settingPreview,
}: TextProp & {
	editDialog?: ComponentType<EditResourceDialogProps>;
	generateEditDialog?: DatasetSettingsEditDialogComponent;
	showGenerate?: boolean;
	preview?: ((path: string) => ReactNode) | null;
}) {
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
			afterContent={({ path }) => preview?.(path)}
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
							...(showGenerate
								? [
										<DatasetSettingGenerateButton
											key="generate"
											setPath={handleSetPath}
											editDialog={generateEditDialog}
										/>,
									]
								: []),
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

function settingPreview(path: string) {
	return <SettingPreviewButtons path={path} />;
}

// srcのsettingを適用する前と、pathを適用した後のテーブル一覧のPreview
function SettingPreviewButtons({ path }: { path: string }) {
	const datasetSrcInfo = useDatasetSrcInfo();
	if (!srcTypeOf(datasetSrcInfo)) {
		return null;
	}
	return (
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
	);
}
