import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import type { TextProp } from "./FormElementProp";
import ResourceText from "./ResourceText";
import SqlSrcDropDownMenu from "./SqlSrcDropDownMenu";

export default function SqlSrcText({
	prefix,
	element,
	hidden,
	srcType,
	handleValueChange,
}: TextProp) {
	const settings = useResourcesSettings();
	const resourceFiles = settings.querys(srcType);

	return (
		<ResourceText
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={resourceFiles}
			handleValueChange={handleValueChange}
		>
			{({ path, setPath, isValueInDatalist }) => (
				<SqlSrcDropDownMenu
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
