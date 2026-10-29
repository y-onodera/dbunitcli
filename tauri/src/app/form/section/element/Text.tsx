import { Children, type ReactNode, useState } from "react";
import DropDownMenu from "../../../../components/element/DropDownMenu";
import {
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
} from "../../../../components/element/Input";
import {
	type FieldErrors,
	useFieldErrors,
	useRegisterVisibleField,
	useSetFieldErrors,
} from "../../../../context/FieldErrorProvider";
import { useWorkspaceContext } from "../../../../context/WorkspaceResourcesProvider";
import { DirectoryChooser, FileChooser, OpenInOS } from "./Chooser";
import type { FileProp, TextProp } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

// 計算されたキーの分割代入はReact Compilerが未対応のため、コンポーネント外で行う
function omitFieldErrors(errors: FieldErrors, name: string): FieldErrors {
	const { [name]: _, ...rest } = errors;
	return rest;
}

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
	const workspacePath = context.workspace;
	const showWorkspaceAsSecondary =
		showDefaulePath && workspacePath !== "" && workspacePath !== basePath;
	const fieldErrors = useFieldErrors(fieldName);
	const setFieldErrors = useSetFieldErrors();
	useRegisterVisibleField(fieldName, !hidden);

	const handleChange = (ev: React.ChangeEvent<HTMLInputElement>) => {
		const newValue = ev.target.value;
		setPath(newValue);
		onValueChange?.(newValue);
		// 入力し直したら、この欄の検証エラーは消す
		if (fieldErrors.length > 0) {
			setFieldErrors((current) => omitFieldErrors(current, fieldName));
		}
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
						{!hidden &&
							fieldErrors.map((message) => (
								<p key={message} className="text-xs text-error">
									{message}
								</p>
							))}
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
	showOpenButton?: boolean;
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
	showOpenButton = true,
	removeButton,
	className,
}: Props) {
	const isFileType = element.attribute.type.includes("FILE");
	const isDirType = element.attribute.type.includes("DIR");
	const showOpen = showOpenButton && (isFileType || isDirType);
	// 候補一覧を持たない欄（isValueInDatalist未指定）は値があれば削除できる
	const showRemove = removeButton && (isValueInDatalist ?? path !== "");
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
					{showRemove && <li>{removeButton(closeMenu)}</li>}
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
