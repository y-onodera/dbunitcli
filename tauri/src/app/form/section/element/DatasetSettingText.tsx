import type { ComponentType, Dispatch, ReactNode, SetStateAction } from "react";
import { InlineActions } from "../../../../components/dialog";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import {
	type DatasetSettingKind,
	resourceFilesOf,
} from "../../../../hooks/useResourceFiles";
import { srcTypeOf } from "../../../../model/CommandOption";
import type { DefaultPath } from "../../../../model/WorkspaceResources";
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
// 省略するとDataset Settingsを使う。
// showGenerate=falseでgenerateボタンを出さない。
// preview: 横に出すプレビュー。省略するとsrcのsettingの適用前後のPreviewボタン、nullなら出さない
// 候補一覧・削除・編集ダイアログの読込と保存の対象は、オプションの基準ディレクトリ（defaultPath）で決まる
const kindByDefaultPath: Partial<Record<DefaultPath, DatasetSettingKind>> = {
	COMPARE_SETTING: "compareSetting",
	GENERATE_SETTING: "generateSetting",
};

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
	editDialog?: ComponentType<
		EditResourceDialogProps & { kind: DatasetSettingKind }
	>;
	generateEditDialog?: DatasetSettingsEditDialogComponent;
	showGenerate?: boolean;
	preview?: ((path: string) => ReactNode) | null;
}) {
	const kind =
		kindByDefaultPath[element.attribute.defaultPath] ?? "datasetSetting";
	const resourceFiles = resourceFilesOf(useResourcesSettings(), kind);

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
								dialogProps={{ kind }}
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
											kind={kind}
										/>,
									]
								: []),
						]}
						removeButton={(closeMenu) => (
							<RemoveResourceButton
								kind={kind}
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
