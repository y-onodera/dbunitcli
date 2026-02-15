import { isAbsolute, sep } from "@tauri-apps/api/path";
import { open } from "@tauri-apps/plugin-dialog";
import {
	type Dispatch,
	Fragment,
	type SetStateAction,
	useEffect,
	useRef,
	useState,
} from "react";
import { ButtonWithIcon } from "../../components/element/Button";
import {
	DirectoryButton,
	ExpandButton,
	FileButton,
} from "../../components/element/ButtonIcon";
import { SettingIcon } from "../../components/element/Icon";
import {
	CheckBox,
	ControllTextBox,
	InputLabel,
	SelectBox,
} from "../../components/element/Input";
import {
	useResourcesSettings,
	useWorkspaceContext,
} from "../../context/WorkspaceResourcesProvider";
import type {
	Attribute,
	CommandParam,
	CommandParams,
} from "../../model/CommandParam";
import {
	isSqlRelatedType,
	type QueryDatasourceType,
} from "../../model/QueryDatasource";
import type { WorkspaceContext } from "../../model/WorkspaceResources";
import DatasetSettingEditButton, {
	RemoveDatasetSettingButton,
} from "../settings/DatasetSettingEditButton";
import SqlEditorButton, {
	RemoveSqlEditorButton,
} from "../settings/SqlEditorButton";
import XlsxSchemaEditButton, {
	RemoveXlsxSchemaButton,
} from "../settings/XlsxSchemaEditButton";

type Prop = {
	prefix: string;
	element: CommandParam;
	hidden?: boolean;
	srcType?: string;
};
type FileProp = Prop & {
	path: string;
	setPath: Dispatch<SetStateAction<string>>;
	onSelect?: () => void;
	srcType?: string;
};
type SelectProp = Prop & {
	handleTypeSelect: (selected: string) => Promise<void>;
};
export default function CommandFormElements(
	prop: {
		handleTypeSelect: (selected: string) => Promise<void>;
	} & CommandParams,
) {
	const [showOptional, setShowOptional] = useState(false);
	const srcTypeElement = prop.elements.find(
		(element) => element.name === "srcType",
	);
	const srcType = srcTypeElement ? srcTypeElement.value : "";
	const toggleOptional = () => setShowOptional(!showOptional);

	return (
		<>
			{prop.elements.map((element) => {
				if (element.attribute.type === "FLG") {
					return (
						<Fragment key={prop.name + prop.prefix + element.name}>
							{prop.optionCaption?.display(element.name) && (
								<div className="pt-2.5">
									<ExpandButton
										toggleOptional={toggleOptional}
										showOptional={showOptional}
										caption={prop.optionCaption?.caption}
									/>
								</div>
							)}
							<Check
								prefix={prop.prefix}
								element={element}
								hidden={prop.optional?.(element.name) && !showOptional}
							/>
						</Fragment>
					);
				}
				if (element.attribute.type === "ENUM") {
					return (
						<Fragment key={prop.name + prop.prefix + element.name}>
							{prop.optionCaption?.display(element.name) && (
								<div className="pt-2.5">
									<ExpandButton
										toggleOptional={toggleOptional}
										showOptional={showOptional}
										caption={prop.optionCaption?.caption}
									/>
								</div>
							)}
							<Select
								handleTypeSelect={prop.handleTypeSelect}
								prefix={prop.prefix}
								element={element}
								hidden={prop.optional?.(element.name) && !showOptional}
							/>
						</Fragment>
					);
				}
				return (
					<Fragment key={prop.name + prop.prefix + element.name}>
						{prop.optionCaption?.display(element.name) && (
							<div className="pt-2.5">
								<ExpandButton
									toggleOptional={toggleOptional}
									showOptional={showOptional}
									caption={prop.optionCaption?.caption}
								/>
							</div>
						)}
						<Text
							prefix={prop.prefix}
							element={element}
							hidden={prop.optional?.(element.name) && !showOptional}
							srcType={element.name === "src" ? srcType : undefined}
						/>
					</Fragment>
				);
			})}
		</>
	);
}
function Text(prop: Prop) {
	const [path, setPath] = useState(prop.element.value);
	const { element, srcType } = prop;
	function resources(): string[] {
		const settings = useResourcesSettings();
		let resources: string[] = [];
		if (element.name === "src" && isSqlRelatedType(srcType ?? "")) {
			resources = settings.querys(srcType);
		} else if (element.name === "setting") {
			resources = settings.metadataSetting;
		} else if (element.name === "xlsxSchema") {
			resources = settings.xlsxSchemas;
		} else if (element.name === "jdbcProperties") {
			resources = settings.jdbcFiles;
		} else if (element.name === "templateGroup") {
			resources = settings.templateFiles;
		}
		return resources;
	}
	const resourceFiles = resources();
	const showDatalist =
		element.name === "setting" ||
		element.name === "xlsxSchema" ||
		(element.name === "src" && isSqlRelatedType(srcType ?? "")) ||
		element.name === "jdbcProperties" ||
		element.name === "templateGroup";
	const showDopDownMenu =
		element.attribute.type.includes("FILE") ||
		element.attribute.type.includes("DIR") ||
		showDatalist;
	const isValueInDatalist = resourceFiles?.includes(path) || false;
	return (
		<div>
			<InputLabel
				text={getName(prop.prefix, prop.element.name)}
				id={getId(prop.prefix, prop.element.name)}
				required={prop.element.attribute.required}
				hidden={prop.hidden}
			/>
			<div className="flex">
				<div
					className={`flex-1${!showDopDownMenu && !isValueInDatalist ? " mr-36" : ""}`}
				>
					<ControllTextBox
						name={getName(prop.prefix, prop.element.name)}
						id={getId(prop.prefix, prop.element.name)}
						list={
							showDatalist
								? `${getId(prop.prefix, prop.element.name)}_list`
								: undefined
						}
						hidden={prop.hidden}
						required={prop.element.attribute.required}
						value={path}
						handleChange={(ev) => setPath(ev.target.value)}
					/>
					{showDatalist && !prop.hidden && (
						<ResourceDatalist
							prefix={prop.prefix}
							element={prop.element}
							resources={resourceFiles}
						/>
					)}
				</div>
				<div className="flex">
					{isValueInDatalist &&
						!prop.hidden &&
						(prop.element.name === "setting" ? (
							<RemoveDatasetSettingButton path={path} setPath={setPath} />
						) : prop.element.name === "xlsxSchema" ? (
							<RemoveXlsxSchemaButton path={path} setPath={setPath} />
						) : srcType === "sql" || srcType === "table" ? (
							<RemoveSqlEditorButton
								path={path}
								setPath={setPath}
								type={srcType as QueryDatasourceType}
							/>
						) : null)}
					{showDopDownMenu && !prop.hidden && (
						<DropDownMenu
							prefix={prop.prefix}
							element={prop.element}
							path={path}
							setPath={setPath}
							hidden={prop.hidden}
							srcType={srcType}
						/>
					)}
				</div>
			</div>
		</div>
	);
}
function DropDownMenu({
	prefix,
	element,
	path,
	setPath,
	hidden,
	srcType,
}: FileProp & { srcType?: string; datasources?: string[] }) {
	const [showMenu, setShowMenu] = useState(false);
	const buttonRef = useRef<HTMLDivElement>(null);
	const menuRef = useRef<HTMLDivElement>(null);
	const [menuPosition, setMenuPosition] = useState<"right" | "left">("right");
	useEffect(() => {
		if (showMenu && buttonRef.current) {
			const rect = buttonRef.current.getBoundingClientRect();
			const viewportWidth = window.innerWidth;
			const menuWidth = 96;

			if (rect.right + menuWidth > viewportWidth) {
				setMenuPosition("left");
			} else {
				setMenuPosition("right");
			}
		}
		function handleClickOutside(event: MouseEvent) {
			if (
				menuRef.current &&
				!menuRef.current.contains(event.target as Node) &&
				buttonRef.current &&
				!buttonRef.current.contains(event.target as Node)
			) {
				setShowMenu(false);
			}
		}
		if (showMenu) {
			document.addEventListener("mousedown", handleClickOutside);
			return () => {
				document.removeEventListener("mousedown", handleClickOutside);
			};
		}
	}, [showMenu]);

	return (
		<div className="relative mr-24" ref={buttonRef}>
			<ButtonWithIcon
				handleClick={() => setShowMenu(!showMenu)}
				id={`${prefix}_${element.name}DropDown`}
			>
				<SettingIcon title="" fill="white" />
			</ButtonWithIcon>
			{showMenu && (
				<div
					ref={menuRef}
					className="absolute z-50 p-4 text-gray-900 bg-white border border-gray-100 rounded-lg shadow-md"
					style={{
						...(menuPosition === "right"
							? { left: "100%", top: 0 }
							: { right: "100%", top: 0 }),
					}}
				>
					<ul className="space-y-4">
						{element.name === "setting" && !hidden && (
							<li>
								<DatasetSettingEditButton path={path} setPath={setPath} />
							</li>
						)}
						{element.name === "xlsxSchema" && !hidden && (
							<li>
								<XlsxSchemaEditButton path={path} setPath={setPath} />
							</li>
						)}
						{element.name === "src" &&
							!hidden &&
							isSqlRelatedType(srcType ?? "") && (
								<li>
									<SqlEditorButton
										type={srcType as QueryDatasourceType}
										path={path}
										setPath={setPath}
									/>
								</li>
							)}
						{element.attribute.type.includes("FILE") && (
							<li>
								<FileChooser
									prefix={prefix}
									element={element}
									srcType={srcType}
									path={path}
									setPath={setPath}
									onSelect={() => setShowMenu(false)}
								/>
							</li>
						)}
						{element.attribute.type.includes("DIR") && (
							<li>
								<DirectoryChooser
									prefix={prefix}
									element={element}
									srcType={srcType}
									path={path}
									setPath={setPath}
									onSelect={() => setShowMenu(false)}
								/>
							</li>
						)}
					</ul>
				</div>
			)}
		</div>
	);
}
function ResourceDatalist(prop: {
	prefix: string;
	element: CommandParam;
	resources: string[];
}) {
	const { element, resources } = prop;
	return (
		<datalist id={`${getId(prop.prefix, element.name)}_list`}>
			{resources?.map((resource) => (
				<option key={resource} value={resource} />
			))}
		</datalist>
	);
}
function FileChooser(prop: FileProp) {
	const context = useWorkspaceContext();
	const handleFileChooserClick = () => {
		const getDefaultPath = async (): Promise<string> => {
			return (await isAbsolute(prop.path))
				? prop.path
				: prop.path
					? getPath(context, prop.element.attribute, prop.srcType) +
						sep() +
						prop.path
					: getPath(context, prop.element.attribute, prop.srcType);
		};
		getDefaultPath().then((defaultPath) =>
			open({ defaultPath }).then((files) => {
				if (files) {
					prop.setPath(
						(files as string).replace(
							getPath(context, prop.element.attribute, prop.srcType) + sep(),
							"",
						),
					);
					prop.onSelect?.();
				}
			}),
		);
	};
	return <FileButton handleClick={handleFileChooserClick} />;
}
function DirectoryChooser(prop: FileProp) {
	const context = useWorkspaceContext();
	const handleDirectoryChooserClick = () => {
		const getDefaultPath = async (): Promise<string> => {
			return (await isAbsolute(prop.path))
				? prop.path
				: prop.path
					? getPath(context, prop.element.attribute, prop.srcType) +
						sep() +
						prop.path
					: getPath(context, prop.element.attribute, prop.srcType);
		};
		getDefaultPath().then((defaultPath) =>
			open({ defaultPath, directory: true }).then((files) => {
				if (files) {
					prop.setPath(
						(files as string).replace(
							getPath(context, prop.element.attribute, prop.srcType) + sep(),
							"",
						),
					);
					prop.onSelect?.();
				}
			}),
		);
	};
	return <DirectoryButton handleClick={handleDirectoryChooserClick} />;
}
function Check(prop: Prop) {
	return (
		<div>
			<InputLabel
				text={
					prop.prefix
						? `-${prop.prefix}.${prop.element.name}`
						: `-${prop.element.name}`
				}
				id={`${prop.prefix}_${prop.element.name}`}
				required={false}
				hidden={prop.hidden}
			/>
			<CheckBox
				name={
					prop.prefix
						? `-${prop.prefix}.${prop.element.name}`
						: `-${prop.element.name}`
				}
				id={`${prop.prefix}_${prop.element.name}`}
				hidden={prop.hidden}
				defaultValue={prop.element.value}
			/>
		</div>
	);
}
function Select(prop: SelectProp) {
	return (
		<div>
			<InputLabel
				text={
					prop.prefix
						? `-${prop.prefix}.${prop.element.name}`
						: `-${prop.element.name}`
				}
				id={`${prop.prefix}_${prop.element.name}`}
				required={prop.element.attribute.required}
				hidden={prop.hidden}
			/>
			<SelectBox
				name={
					prop.prefix
						? `-${prop.prefix}.${prop.element.name}`
						: `-${prop.element.name}`
				}
				id={`${prop.prefix}_${prop.element.name}`}
				required={true}
				hidden={prop.hidden}
				handleOnChange={prop.handleTypeSelect}
				defaultValue={prop.element.value}
			>
				{prop.element.attribute.selectOption.map((value) => {
					return (
						<option key={prop.prefix + prop.element.name + value} value={value}>
							{value}
						</option>
					);
				})}
			</SelectBox>
		</div>
	);
}
function getId(prefix: string, name: string): string {
	return prefix ? `${prefix}_${name}` : `${name}`;
}
function getName(prefix: string, name: string): string {
	return prefix ? `-${prefix}.${name}` : `-${name}`;
}
function getPath(
	context: WorkspaceContext,
	attribute: Attribute,
	srcType: string | undefined,
): string {
	if (attribute.defaultPath === "DATASET") {
		if (isSqlRelatedType(srcType ?? "")) {
			return context.datasetBase + sep() + srcType;
		}
		return context.datasetBase;
	}
	if (attribute.defaultPath === "RESULT") {
		return context.resultBase;
	}
	if (attribute.defaultPath === "SETTING") {
		return context.settingBase;
	}
	if (attribute.defaultPath === "TEMPLATE") {
		return context.templateBase;
	}
	if (attribute.defaultPath === "JDBC") {
		return context.jdbcBase;
	}
	if (attribute.defaultPath === "XLSX_SCHEMA") {
		return context.xlsxSchemaBase;
	}
	return context.workspace;
}
