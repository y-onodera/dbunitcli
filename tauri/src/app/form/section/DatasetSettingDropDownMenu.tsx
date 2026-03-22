import DatasetSettingEditButton, {
	RemoveDatasetSettingButton,
} from "../../settings/DatasetSettingEditButton";
import type { FileProp } from "./FormElementProp";
import ResourceDropDownMenu from "./ResourceDropDownMenu";

type Props = Omit<FileProp, "onSelect" | "hidden"> & {
	isValueInDatalist: boolean;
};

export default function DatasetSettingDropDownMenu({
	path,
	setPath,
	prefix,
	element,
	srcType,
	isValueInDatalist,
	hideDatasetSettingEdit,
}: Props) {
	return (
		<ResourceDropDownMenu
			path={path}
			setPath={setPath}
			prefix={prefix}
			element={element}
			srcType={srcType}
			isValueInDatalist={isValueInDatalist}
			editButton={
				!hideDatasetSettingEdit ? (
					<DatasetSettingEditButton path={path} setPath={setPath} />
				) : undefined
			}
			removeButton={() => (
				<RemoveDatasetSettingButton path={path} setPath={setPath} />
			)}
		/>
	);
}
