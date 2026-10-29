import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import {
	EditResourceButton,
	RemoveResourceButton,
} from "../dialog/ResourceEditButton";
import XlsxSchemaDialog from "../dialog/XlsxSchemaDialog";
import { XlsxSchemaGenerateButton } from "../dialog/XlsxSchemaGenerateDialog";
import type { TextProp } from "./FormElementProp";
import Text, { TextDropDownMenu } from "./Text";

export default function XlsxSchemaText({
	prefix,
	element,
	hidden,
	handleValueChange,
}: TextProp) {
	const settings = useResourcesSettings();

	return (
		<Text
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={settings.xlsxSchemas}
			showDefaulePath={true}
			handleValueChange={handleValueChange}
		>
			{({ path, setPath, isValueInDatalist }) => (
				<TextDropDownMenu
					path={path}
					setPath={setPath}
					prefix={prefix}
					element={element}
					isValueInDatalist={isValueInDatalist}
					editButtons={[
						<EditResourceButton
							key="edit"
							dialog={XlsxSchemaDialog}
							path={path}
							setPath={setPath}
						/>,
						<XlsxSchemaGenerateButton
							key="generate"
							path={path}
							setPath={setPath}
						/>,
					]}
					removeButton={(closeMenu) => (
						<RemoveResourceButton
							kind="xlsxSchema"
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
