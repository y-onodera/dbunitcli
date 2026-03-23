import { useResourcesSettings } from "../../../context/WorkspaceResourcesProvider";
import TemplateEditButton, {
	RemoveTemplateButton,
} from "../../settings/TemplateEditButton";
import type { Prop } from "./FormElementProp";
import ResourceDropDownMenu from "./ResourceDropDownMenu";
import ResourceText from "./ResourceText";

export default function TemplateText({ prefix, element, hidden }: Prop) {
	const settings = useResourcesSettings();

	return (
		<ResourceText
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={settings.templateFiles}
		>
			{({ path, setPath, isValueInDatalist }) => (
				<ResourceDropDownMenu
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
		</ResourceText>
	);
}
