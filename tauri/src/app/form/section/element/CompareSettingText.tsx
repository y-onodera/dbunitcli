import type { Dispatch, SetStateAction } from "react";
import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import CompareKeySettingsDialog, {
	CompareKeySettingGenerateButton,
} from "../dialog/CompareKeySettingsDialog";
import {
	EditResourceButton,
	RemoveResourceButton,
} from "../dialog/ResourceEditButton";
import type { TextProp } from "./FormElementProp";
import Text, { TextDropDownMenu } from "./Text";

// compareの-setting（比較キー設定）。比較キー専用の編集・生成ボタンを持つ。
// テーブル名・カラム名の候補は呼び出し側のDatasetSrcInfoProvider（old側のデータセット）から取る
export default function CompareSettingText({
	prefix,
	element,
	hidden,
	handleValueChange,
}: TextProp) {
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
								dialog={CompareKeySettingsDialog}
								key="edit"
								path={path}
								setPath={handleSetPath}
							/>,
							<CompareKeySettingGenerateButton
								key="generate"
								path={path}
								setPath={handleSetPath}
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
