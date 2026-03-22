import type { ReactNode } from "react";
import DropDownMenu from "../../../components/element/DropDownMenu";
import { DirectoryChooser, FileChooser, OpenInOS } from "./Chooser";
import type { FileProp } from "./FormElementProp";

type Props = Omit<FileProp, "onSelect" | "hidden"> & {
	isValueInDatalist?: boolean;
	editButton?: ReactNode;
	removeButton?: (closeMenu: () => void) => ReactNode;
	className?: string;
};

export default function ResourceDropDownMenu({
	path,
	setPath,
	prefix,
	element,
	srcType,
	isValueInDatalist,
	editButton,
	removeButton,
	className,
}: Props) {
	const isFileType = element.attribute.type.includes("FILE");
	const isDirType = element.attribute.type.includes("DIR");
	const isFileOrDir = isFileType || isDirType;
	return (
		<DropDownMenu className={className}>
			{(closeMenu) => (
				<>
					{editButton && <li>{editButton}</li>}
					{isValueInDatalist && removeButton && (
						<li>{removeButton(closeMenu)}</li>
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
