import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import {
	RemoveXlsxSchemaButton,
	XlsxSchemaEditButton,
} from "../dialog/XlsxSchemaDialog";
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
						<XlsxSchemaEditButton key="edit" path={path} setPath={setPath} />,
					]}
					removeButton={() => (
						<RemoveXlsxSchemaButton path={path} setPath={setPath} />
					)}
				/>
			)}
		</Text>
	);
}
