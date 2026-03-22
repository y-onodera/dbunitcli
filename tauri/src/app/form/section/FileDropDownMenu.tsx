import type { FileProp } from "./FormElementProp";
import ResourceDropDownMenu from "./ResourceDropDownMenu";

type Props = Omit<FileProp, "onSelect" | "hidden">;

export default function FileDropDownMenu({
	path,
	setPath,
	prefix,
	element,
	srcType,
}: Props) {
	return (
		<ResourceDropDownMenu
			path={path}
			setPath={setPath}
			prefix={prefix}
			element={element}
			srcType={srcType}
		/>
	);
}
