import TemplateEditButton, {
	RemoveTemplateButton,
} from "../../settings/TemplateEditButton";
import type { FileProp } from "./FormElementProp";
import ResourceDropDownMenu from "./ResourceDropDownMenu";

type Props = Omit<FileProp, "onSelect" | "hidden"> & {
	isValueInDatalist: boolean;
};

export default function TemplateDropDownMenu({
	path,
	setPath,
	prefix,
	element,
	srcType,
	isValueInDatalist,
}: Props) {
	return (
		<ResourceDropDownMenu
			path={path}
			setPath={setPath}
			prefix={prefix}
			element={element}
			srcType={srcType}
			isValueInDatalist={isValueInDatalist}
			editButton={<TemplateEditButton path={path} setPath={setPath} />}
			removeButton={() => <RemoveTemplateButton path={path} setPath={setPath} />}
		/>
	);
}
