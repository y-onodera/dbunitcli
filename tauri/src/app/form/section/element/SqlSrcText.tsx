import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import { resourceFilesOf } from "../../../../hooks/useResourceFiles";
import type { QueryDatasourceType } from "../../../../model/QueryDatasource";
import {
	EditResourceButton,
	RemoveResourceButton,
} from "../dialog/ResourceEditButton";
import { sqlEditorDialogs } from "../dialog/SqlEditorDialog";
import type { TextProp } from "./FormElementProp";
import Text, { TextDropDownMenu } from "./Text";

// encodingはコマンド実行時にこのSQLを読む文字コードで、編集ダイアログの読み書きに使う
export default function SqlSrcText({
	prefix,
	element,
	hidden,
	srcType,
	handleValueChange,
	queryOnly = false,
	encoding,
}: TextProp & { queryOnly?: boolean; encoding: string }) {
	const settings = useResourcesSettings();
	const type = srcType as QueryDatasourceType;

	return (
		<Text
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={resourceFilesOf(settings, type)}
			showDefaulePath={true}
			handleValueChange={handleValueChange}
		>
			{({ path, setPath, isValueInDatalist }) => (
				<TextDropDownMenu
					path={path}
					setPath={setPath}
					prefix={prefix}
					element={element}
					srcType={srcType}
					isValueInDatalist={isValueInDatalist}
					editButtons={[
						<EditResourceButton
							key="edit"
							dialog={sqlEditorDialogs[type]}
							dialogProps={{ queryOnly, encoding }}
							path={path}
							setPath={setPath}
						/>,
					]}
					removeButton={(closeMenu) => (
						<RemoveResourceButton
							kind={type}
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
