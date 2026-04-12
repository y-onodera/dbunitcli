import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import TemplateEditButton, {
	RemoveTemplateButton,
} from "../dialog/TemplateEditButton";
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
						<RemoveTemplateButton path={path} setPath={setPath} />
					)}
				/>
			)}
		</Text>
	);
}
