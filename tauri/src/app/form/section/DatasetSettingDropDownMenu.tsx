import DropDownMenu from "../../../components/element/DropDownMenu";
import DatasetSettingEditButton, {
	RemoveDatasetSettingButton,
} from "../../settings/DatasetSettingEditButton";
import { DirectoryChooser, FileChooser, OpenInOS } from "./Chooser";
import type { FileProp } from "./FormElementProp";

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
	const isFileType = element.attribute.type.includes("FILE");
	const isDirType = element.attribute.type.includes("DIR");
	const isFileOrDir = isFileType || isDirType;
	return (
		<DropDownMenu>
			{(closeMenu) => (
				<>
					{!hideDatasetSettingEdit && (
						<li>
							<DatasetSettingEditButton path={path} setPath={setPath} />
						</li>
					)}
					{isFileOrDir && path && (
						<li>
							<OpenInOS
								prefix={prefix}
								element={element}
								srcType={srcType}
								path={path}
								setPath={setPath}
							/>
						</li>
					)}
					{isValueInDatalist && (
						<li>
							<RemoveDatasetSettingButton path={path} setPath={setPath} />
						</li>
					)}
					{isFileType && (
						<li>
							<FileChooser
								prefix={prefix}
								element={element}
								srcType={srcType}
								path={path}
								setPath={setPath}
								onSelect={closeMenu}
							/>
						</li>
					)}
					{isDirType && (
						<li>
							<DirectoryChooser
								prefix={prefix}
								element={element}
								srcType={srcType}
								path={path}
								setPath={setPath}
								onSelect={closeMenu}
							/>
						</li>
					)}
				</>
			)}
		</DropDownMenu>
	);
}
