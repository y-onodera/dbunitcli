import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import type { QueryDatasourceType } from "../../../../model/QueryDatasource";
import {
	EditResourceButton,
	RemoveResourceButton,
} from "../dialog/ResourceEditButton";
import { sqlEditorDialogs } from "../dialog/SqlEditorDialog";
import type { TextProp } from "./FormElementProp";
import Text, { TextDropDownMenu } from "./Text";

const resourceFilesKey = {
	sql: "sqlFiles",
	table: "tableFiles",
	csvq: "csvqFiles",
} as const satisfies Record<QueryDatasourceType, string>;

export default function SqlSrcText({
	prefix,
	element,
	hidden,
	srcType,
	handleValueChange,
}: TextProp) {
	const settings = useResourcesSettings();
	const type = srcType as QueryDatasourceType;

	return (
		<Text
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={settings[resourceFilesKey[type]]}
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
