import { useResourcesSettings } from "../../../context/WorkspaceResourcesProvider";
import type { Prop } from "./FormElementProp";
import ResourceText from "./ResourceText";
import TemplateDropDownMenu from "./TemplateDropDownMenu";

export default function TemplateText({
	prefix,
	element,
	hidden,
	srcType,
}: Prop) {
	const settings = useResourcesSettings();

	return (
		<ResourceText
			prefix={prefix}
			element={element}
			hidden={hidden}
			srcType={srcType}
			resourceFiles={settings.templateFiles}
		>
			{({ path, setPath, isValueInDatalist }) => (
				<TemplateDropDownMenu
					path={path}
					setPath={setPath}
					prefix={prefix}
					element={element}
					srcType={srcType}
					isValueInDatalist={isValueInDatalist}
				/>
			)}
		</ResourceText>
	);
}
