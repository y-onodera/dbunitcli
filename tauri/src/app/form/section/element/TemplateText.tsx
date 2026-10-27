import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import { RemoveResourceButton } from "../dialog/ResourceEditButton";
import { TemplateEditButton } from "../dialog/TemplateEditDialog";
import type { TextProp } from "./FormElementProp";
import Text, { TextDropDownMenu } from "./Text";

export default function TemplateText({
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
					editButtons={[
						<TemplateEditButton key="edit" path={path} setPath={setPath} />,
					]}
					removeButton={() => (
						<RemoveResourceButton
							kind="template"
							path={path}
							setPath={setPath}
						/>
					)}
				/>
			)}
		</Text>
	);
}
