import XlsxSchemaEditButton, {
	RemoveXlsxSchemaButton,
} from "../../settings/XlsxSchemaEditButton";
import type { FileProp } from "./FormElementProp";
import ResourceDropDownMenu from "./ResourceDropDownMenu";

type Props = Omit<FileProp, "onSelect" | "hidden"> & {
	isValueInDatalist: boolean;
};

export default function XlsxSchemaDropDownMenu({
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
			editButtons={[<XlsxSchemaEditButton path={path} setPath={setPath} />]}
			removeButton={() => (
				<RemoveXlsxSchemaButton path={path} setPath={setPath} />
			)}
		/>
	);
}
