import type { ReactNode } from "react";
import { useState } from "react";
import DropDownMenu from "../../../../components/element/DropDownMenu";
import {
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
} from "../../../../components/element/Input";
import { useWorkspaceContext } from "../../../../context/WorkspaceResourcesProvider";
import { DirectoryChooser, FileChooser, OpenInOS } from "./Chooser";
import type { FileProp, TextProp } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

export default function Text({
	prefix,
	element,
	hidden,
	resourceFiles = [],
	showDefaulePath = false,
	handleValueChange: onValueChange,
	children,
	afterContent,
}: TextProp) {
	const [path, setPath] = useState(element.value);
	const hasResources = resourceFiles.length > 0;
	const isValueInDatalist = resourceFiles.includes(path);
	const id = getId(prefix, element.name);
	const fieldName = getName(prefix, element.name);
	const context = useWorkspaceContext();
	const basePath = showDefaulePath
		? context.getPath(element.attribute.defaultPath)
		: "";

	const handleChange = (ev: React.ChangeEvent<HTMLInputElement>) => {
		const newValue = ev.target.value;
		setPath(newValue);
		onValueChange?.(newValue);
	};

	return (
		<>
			<div>
				<InputLabel
					text={fieldName}
					id={id}
					required={element.attribute.required}
					hidden={hidden}
				/>
				<div className="flex">
					<div className="flex-1">
						<ControllTextBox
							name={fieldName}
							id={id}
							list={hasResources ? `${id}_list` : undefined}
							hidden={hidden}
							required={element.attribute.required}
							value={path}
							handleChange={handleChange}
						/>
						{!hidden && resourceFiles && (
							<ResourceDatalist id={id} resources={resourceFiles} />
						)}
						{!hidden && showDefaulePath && (
							<p className="text-xs text-gray-400 truncate">{basePath}</p>
						)}
					</div>
					{!hidden &&
						children &&
						children({ path, setPath, isValueInDatalist })}
				</div>
			</div>
			{afterContent?.({ path })}
		</>
	);
}
type Props = Omit<FileProp, "onSelect" | "hidden"> & {
	isValueInDatalist?: boolean;
	editButtons?: ReactNode[];
	removeButton?: (closeMenu: () => void) => ReactNode;
	className?: string;
};
export function TextDropDownMenu({
	path,
	setPath,
	prefix,
	element,
	srcType,
	isValueInDatalist,
	editButtons,
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
					{editButtons?.map((btn) => (
						<li key={btn?.toString()}>{btn}</li>
					))}
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
					{isValueInDatalist && removeButton && (
						<li>{removeButton(closeMenu)}</li>
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
