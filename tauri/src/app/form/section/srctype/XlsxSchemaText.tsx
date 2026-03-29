import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import XlsxSchemaEditButton, {
	RemoveXlsxSchemaButton,
} from "../../../settings/XlsxSchemaEditButton";
import type { TextProp } from "../element/FormElementProp";
import ResourceDropDownMenu from "../element/ResourceDropDownMenu";
import ResourceText from "../element/ResourceText";

export default function XlsxSchemaText({
	prefix,
	element,
	hidden,
	handleValueChange,
}: TextProp) {
	const settings = useResourcesSettings();

	return (
		<ResourceText
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={settings.xlsxSchemas}
			handleValueChange={handleValueChange}
		>
			{({ path, setPath, isValueInDatalist }) => (
				<ResourceDropDownMenu
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
		</ResourceText>
	);
}
