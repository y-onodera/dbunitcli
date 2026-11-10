import { Children, type ReactNode } from "react";
import DropDownMenu from "../../../../components/element/DropDownMenu";
import {
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
} from "../../../../components/element/Input";
import {
	useClearFieldErrors,
	useFieldErrors,
} from "../../../../context/FieldErrorProvider";
import { useWorkspaceContext } from "../../../../context/WorkspaceResourcesProvider";
import { useSyncedState } from "../../../../utils/useResettableState";
import { DirectoryChooser, FileChooser, OpenInOS } from "./Chooser";
import FieldErrorMessages from "./FieldErrorMessages";
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
	const [path, setPath] = useSyncedState(element.value);
	const hasResources = resourceFiles.length > 0;
	const isValueInDatalist = resourceFiles.includes(path);
	const id = getId(prefix, element.name);
	const fieldName = getName(prefix, element.name);
	const context = useWorkspaceContext();
	const basePath = showDefaulePath
		? context.getPath(element.attribute.defaultPath)
		: "";
	const workspacePath = context.workspace;
	const showWorkspaceAsSecondary =
		showDefaulePath && workspacePath !== "" && workspacePath !== basePath;
	const fieldErrors = useFieldErrors(fieldName, !hidden);
	const clearFieldErrors = useClearFieldErrors();

	const updatePath = (newPath: string) => {
		setPath(newPath);
		onValueChange?.(newPath);
	};
	const handleChange = (ev: React.ChangeEvent<HTMLInputElement>) =>
		updatePath(ev.target.value);

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
							placeholder={element.attribute.defaultValue}
							handleChange={handleChange}
						/>
						{!hidden && hasResources && (
							<ResourceDatalist id={id} resources={resourceFiles} />
						)}
						{!hidden && showDefaulePath && (
							<p className="text-xs text-content-disabled truncate">
								{basePath}
							</p>
						)}
						{!hidden && showWorkspaceAsSecondary && (
							<p className="text-xs text-content-disabled truncate">
								{workspacePath}
							</p>
						)}
						<FieldErrorMessages messages={fieldErrors} hidden={hidden} />
					</div>
					{!hidden &&
						children &&
						children({
							path,
							// ボタン等から値を変えたときはchangeが起きないため、ここで変更の通知と検証エラーの消去を行う
							setPath: (action) => {
								updatePath(
									typeof action === "function" ? action(path) : action,
								);
								clearFieldErrors(fieldName);
							},
							isValueInDatalist,
						})}
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
	const showOpen = isFileType || isDirType;
	return (
		<DropDownMenu className={className}>
			{(closeMenu) => (
				<>
					{/* Children.mapは各要素のkeyを引き継いだkeyを<li>へ付ける */}
					{Children.map(editButtons, (btn) => (
						<li>{btn}</li>
					))}
					{showOpen && path && (
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
