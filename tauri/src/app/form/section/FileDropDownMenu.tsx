import DropDownMenu from "../../../components/element/DropDownMenu";
import { DirectoryChooser, FileChooser, OpenInOS } from "./Chooser";
import type { FileProp } from "./FormElementProp";

type Props = Omit<FileProp, "onSelect" | "hidden">;

export default function FileDropDownMenu({
	path,
	setPath,
	prefix,
	element,
	srcType,
}: Props) {
	const isFileType = element.attribute.type.includes("FILE");
	const isDirType = element.attribute.type.includes("DIR");
	return (
		<DropDownMenu>
			{(closeMenu) => (
				<>
					{path && (
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
