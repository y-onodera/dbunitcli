import type { ReactNode } from "react";
import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import {
	EditResourceButton,
	RemoveResourceButton,
} from "../dialog/ResourceEditButton";
import TemplateEditDialog from "../dialog/TemplateEditDialog";
import type { TextProp } from "./FormElementProp";
import Text, { TextDropDownMenu } from "./Text";

// editButtonsを渡すと、既定の編集ボタンの代わりにそれをメニューに出す（xlsxテンプレートの雛型生成など）。
// encodingはコマンド実行時にこのテンプレートを読む文字コードで、編集ダイアログの読み書きに使う
export default function TemplateText({
	prefix,
	element,
	hidden,
	handleValueChange,
	editButtons,
	encoding,
}: TextProp & {
	editButtons?: (path: string, setPath: (value: string) => void) => ReactNode[];
	encoding: string;
}) {
	const settings = useResourcesSettings();

	return (
		<Text
			prefix={prefix}
			element={element}
			handleValueChange={handleValueChange}
			hidden={hidden}
			resourceFiles={settings.templateFiles}
			showDefaulePath={true}
		>
			{({ path, setPath, isValueInDatalist }) => (
				<TextDropDownMenu
					path={path}
					setPath={setPath}
					prefix={prefix}
					element={element}
					isValueInDatalist={isValueInDatalist}
					editButtons={
						editButtons?.(path, setPath) ?? [
							<EditResourceButton
								key="edit"
								dialog={TemplateEditDialog}
								dialogProps={{ encoding }}
								path={path}
								setPath={setPath}
							/>,
						]
					}
					removeButton={(closeMenu) => (
						<RemoveResourceButton
							kind="template"
							path={path}
							setPath={setPath}
							onRemoved={closeMenu}
						/>
					)}
				/>
			)}
		</Text>
	);
}
