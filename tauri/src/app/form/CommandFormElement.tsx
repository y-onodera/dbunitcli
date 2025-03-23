import { isAbsolute, sep } from "@tauri-apps/api/path";
import { open } from "@tauri-apps/plugin-dialog";
import { type Dispatch, type SetStateAction, useEffect, useRef, useState } from "react";
import { ButtonWithIcon } from "../../components/element/Button";
import { DirectoryButton, EditButton, ExpandButton, FileButton } from "../../components/element/ButtonIcon";
import { SettingIcon } from "../../components/element/Icon";
import { CheckBox, ControllTextBox, InputLabel, SelectBox } from "../../components/element/Input";
import { loadDatasetSettings, useSetDatasetSettings } from "../../context/DatasetSettingsProvider";
import { useEnviroment } from "../../context/EnviromentProvider";
import { useResourcesSettings, useWorkspaceContext } from "../../context/WorkspaceResourcesProvider";
import { loadXlsxSchema, useSetXlsxSchema } from "../../context/XlsxSchemaProvider";
import type { Attribute, CommandParam, CommandParams } from "../../model/CommandParam";
import type { DatasetSettings } from "../../model/DatasetSettings";
import type { WorkspaceContext } from "../../model/WorkspaceResources";
import type { XlsxSchema } from "../../model/XlsxSchema";
import DatasetSettingsDialog from "../settings/DatasetSettingsDialog";
import XlsxSchemaDialog from "../settings/XlsxSchemaDialog";

type Prop = {
	prefix: string;
	element: CommandParam;
	hidden?: boolean;
};
type FileProp = Prop & {
	path: string;
	setPath: Dispatch<SetStateAction<string>>;
	onSelect?: () => void;
};
type SelectProp = Prop & {
	handleTypeSelect: () => Promise<void>;
};
export default function CommandFormElements(prop: CommandParams) {
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional)
	return (
		<>
			{prop.elements.map((element) => {
				if (element.attribute.type === "FLG") {
					return (
						<>
							{prop.optionCaption?.display(element.name) && (
								<div className="pt-2.5">
									<ExpandButton
										key={prop.prefix + prop.optionCaption?.caption}
										toggleOptional={toggleOptional}
										showOptional={showOptional}
										caption={prop.optionCaption?.caption}
									/>
								</div>
							)}
							<Check
								prefix={prop.prefix}
								element={element}
								key={prop.name + prop.prefix + element.name}
								hidden={prop.optional?.(element.name) && !showOptional}
							/>
						</>
					);
				}
				if (element.attribute.type === "ENUM") {
					return (
						<>
							{prop.optionCaption?.display(element.name) && (
								<div className="pt-2.5">
									<ExpandButton
										key={prop.prefix + prop.optionCaption?.caption}
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
								key={prop.name + prop.prefix + element.name}
								hidden={prop.optional?.(element.name) && !showOptional}
							/>
						</>
					);
				}
				return (
					<>
						{prop.optionCaption?.display(element.name) && (
							<div className="pt-2.5">
								<ExpandButton
									key={prop.prefix + prop.optionCaption?.caption}
									toggleOptional={toggleOptional}
									showOptional={showOptional}
									caption={prop.optionCaption?.caption}
								/>
							</div>
						)}
						<Text
							prefix={prop.prefix}
							element={element}
							key={prop.name + prop.prefix + element.name}
							hidden={prop.optional?.(element.name) && !showOptional}
						/>
					</>
				);
			})}
		</>
	);
}
function Text(prop: Prop) {
	const [path, setPath] = useState(prop.element.value);
	function showDatalist(elment: CommandParam): boolean {
		return elment.name === "setting" || elment.name === "xlsxSchema";
	}
	function showDopDownMenu(elment: CommandParam): boolean {
		return elment.attribute.type.includes("FILE") || elment.attribute.type.includes("DIR") || showDatalist(elment);
	}
	return (
		<div>
			<InputLabel
				text={getName(prop.prefix, prop.element.name)}
				id={getId(prop.prefix, prop.element.name)}
				required={prop.element.attribute.required}
				hidden={prop.hidden}
			/>
			<div className="flex">
				<div className={`flex-1${!showDopDownMenu(prop.element) ? " mr-36" : ""}`}>
					<ControllTextBox
						name={getName(prop.prefix, prop.element.name)}
						id={getId(prop.prefix, prop.element.name)}
						list={(showDatalist(prop.element)) ? `${getId(prop.prefix, prop.element.name)}_list` : undefined}
						hidden={prop.hidden}
						required={prop.element.attribute.required}
						value={path}
						handleChange={(ev) => setPath(ev.target.value)}
					/>
					{showDatalist(prop.element) && !prop.hidden && (
						<ResourceDatalist prefix={prop.prefix} element={prop.element} />
					)}
				</div>
				{showDopDownMenu(prop.element) && !prop.hidden && (
					<DropDownMenu
						prefix={prop.prefix}
						element={prop.element}
						path={path}
						setPath={setPath}
						hidden={prop.hidden}
					/>
				)}
			</div>
		</div>
	);
};
function DropDownMenu({
	prefix,
	element,
	path,
	setPath,
	hidden,
}: FileProp) {
	const [showChooser, setShowChooser] = useState(false);
	const buttonRef = useRef<HTMLDivElement>(null);
	const menuRef = useRef<HTMLDivElement>(null);
	const [menuPosition, setMenuPosition] = useState<'right' | 'left'>('right');
	useEffect(() => {
		if (showChooser && buttonRef.current) {
			const rect = buttonRef.current.getBoundingClientRect();
			const viewportWidth = window.innerWidth;
			const menuWidth = 96;

			if (rect.right + menuWidth > viewportWidth) {
				setMenuPosition('left');
			} else {
				setMenuPosition('right');
			}
		}
		function handleClickOutside(event: MouseEvent) {
			if (menuRef.current && !menuRef.current.contains(event.target as Node) &&
				buttonRef.current && !buttonRef.current.contains(event.target as Node)) {
				setShowChooser(false);
			}
		}

		if (showChooser) {
			document.addEventListener('mousedown', handleClickOutside);
			return () => {
				document.removeEventListener('mousedown', handleClickOutside);
			};
		}
	}, [showChooser]);

	return (
		<div className="relative mr-24" ref={buttonRef}>
			<ButtonWithIcon handleClick={() => setShowChooser(!showChooser)} id={`${prefix}_${element.name}DropDown`}>
				<SettingIcon title="" fill="white" />
			</ButtonWithIcon>
			{showChooser && (
				<div
					ref={menuRef}
					className="absolute z-50 p-4 text-gray-900 bg-white border border-gray-100 rounded-lg shadow-md"
					style={{
						...(menuPosition === 'right'
							? { left: '100%', top: 0 }
							: { right: '100%', top: 0 })
					}}
				>
					<ul className="space-y-4">
						{element.name === "setting" && !hidden && (
							<SettingEdit prefix={prefix} element={element} path={path} setPath={setPath} />
						)}
						{element.name === "xlsxSchema" && !hidden && (
							<XlsxSchemaEdit prefix={prefix} element={element} path={path} setPath={setPath} />
						)}
						{element.attribute.type.includes("FILE") && (
							<li>
								<FileChooser
									prefix={prefix}
									element={element}
									path={path}
									setPath={setPath}
									onSelect={() => setShowChooser(false)}
								/>
							</li>
						)}
						{element.attribute.type.includes("DIR") && (
							<li>
								<DirectoryChooser
									prefix={prefix}
									element={element}
									path={path}
									setPath={setPath}
									onSelect={() => setShowChooser(false)}
								/>
							</li>
						)}
					</ul>
				</div>
			)}
		</div>
	);
}
function ResourceDatalist(prop: { prefix: string, element: CommandParam }) {
	const settings = useResourcesSettings();
	return (
		<datalist id={`${getId(prop.prefix, prop.element.name)}_list`}>
			{(prop.element.name === "setting" ? settings.datasetSettings : settings.xlsxSchemas)?.map((resource) => (
				<option key={resource} value={resource} />
			))}
		</datalist>
	);
}
function SettingEdit(prop: FileProp) {
	const environment = useEnviroment();
	const setDatasetSettings = useSetDatasetSettings();
	const [dialogEdit, setDialogEdit] = useState(false);
	const handleDialogOpen = () => {
		loadDatasetSettings(environment.apiUrl, prop.path ?? "")
			.then((settings: DatasetSettings) => setDatasetSettings(settings))
			.catch((ex) => alert(ex));
		setDialogEdit(true);
	};
	const handleSave = (path: string) => {
		prop.setPath(path);
		setDialogEdit(false);
	};
	return (
		<>
			{dialogEdit && (
				<DatasetSettingsDialog
					fileName={prop.path}
					setFileName={prop.setPath}
					handleDialogClose={() => setDialogEdit(false)}
					handleSave={handleSave}
				/>
			)}
			<EditButton handleClick={handleDialogOpen} />
		</>
	);
};
function XlsxSchemaEdit(prop: FileProp) {
	const environment = useEnviroment();
	const setXlsxSchema = useSetXlsxSchema();
	const [dialogEdit, setDialogEdit] = useState(false);
	const handleDialogOpen = () => {
		loadXlsxSchema(environment.apiUrl, prop.path ?? "")
			.then((settings: XlsxSchema) => setXlsxSchema(settings))
			.catch((ex) => alert(ex));
		setDialogEdit(true);
	};
	const handleSave = (path: string) => {
		prop.setPath(path);
		setDialogEdit(false);
	};
	return (
		<>
			{dialogEdit && (
				<XlsxSchemaDialog
					fileName={prop.path}
					setFileName={prop.setPath}
					handleDialogClose={() => setDialogEdit(false)}
					handleSave={handleSave}
				/>
			)}
			<EditButton handleClick={handleDialogOpen} />
		</>
	);
};
function FileChooser(prop: FileProp) {
	const context = useWorkspaceContext()
	const handleFileChooserClick = () => {
		const getDefaultPath = async (): Promise<string> => {
			return await isAbsolute(prop.path) ? prop.path
				: prop.path ? getPath(context, prop.element.attribute) + sep() + prop.path : getPath(context, prop.element.attribute);
		};
		getDefaultPath().then(defaultPath => open({ defaultPath })
			.then((files) => {
				if (files) {
					prop.setPath((files as string).replace(getPath(context, prop.element.attribute) + sep(), ""));
					prop.onSelect?.();
				}
			}));
	};
	return (<FileButton handleClick={handleFileChooserClick} />);
};
function DirectoryChooser(prop: FileProp) {
	const context = useWorkspaceContext()
	const handleDirectoryChooserClick = () => {
		const getDefaultPath = async (): Promise<string> => {
			return await isAbsolute(prop.path) ? prop.path
				: prop.path ? getPath(context, prop.element.attribute) + sep() + prop.path : getPath(context, prop.element.attribute);
		};
		getDefaultPath().then(defaultPath => open({ defaultPath, directory: true })
			.then((files) => {
				if (files) {
					prop.setPath((files as string).replace(getPath(context, prop.element.attribute) + sep(), ""));
					prop.onSelect?.();
				}
			}));
	};
	return (<DirectoryButton handleClick={handleDirectoryChooserClick} />);
};
function Check(prop: Prop) {
	return (
		<div>
			<InputLabel
				text={prop.prefix ? `-${prop.prefix}.${prop.element.name}` : `-${prop.element.name}`}
				id={`${prop.prefix}_${prop.element.name}`}
				required={false}
				hidden={prop.hidden}
			/>
			<CheckBox
				name={prop.prefix ? `-${prop.prefix}.${prop.element.name}` : `-${prop.element.name}`}
				id={`${prop.prefix}_${prop.element.name}`}
				hidden={prop.hidden}
				defaultValue={prop.element.value}
			/>
		</div>
	);
};
function Select(prop: SelectProp) {
	return (
		<div>
			<InputLabel
				text={prop.prefix ? `-${prop.prefix}.${prop.element.name}` : `-${prop.element.name}`}
				id={`${prop.prefix}_${prop.element.name}`}
				required={prop.element.attribute.required}
				hidden={prop.hidden}
			/>
			<SelectBox
				name={prop.prefix ? `-${prop.prefix}.${prop.element.name}` : `-${prop.element.name}`}
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
};
function getId(prefix: string, name: string): string {
	return prefix ? `${prefix}_${name}` : `${name}`;
}
function getName(prefix: string, name: string): string {
	return prefix ? `-${prefix}.${name}` : `-${name}`;
}
function getPath(context: WorkspaceContext, attribute: Attribute): string {
	if (attribute.defaultPath === "DATASET") {
		return context.datasetBase
	}
	if (attribute.defaultPath === "RESULT") {
		return context.resultBase
	}
	if (attribute.defaultPath === "SETTING") {
		return context.settingBase
	}
	if (attribute.defaultPath === "TEMPLATE") {
		return context.templateBase
	}
	if (attribute.defaultPath === "JDBC") {
		return context.jdbcBase
	}
	if (attribute.defaultPath === "XLSX_SCHEMA") {
		return context.xlsxSchemaBase
	}
	return context.workspace
}
