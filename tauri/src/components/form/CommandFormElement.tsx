import { open } from "@tauri-apps/api/dialog";
import { isAbsolute, sep } from "@tauri-apps/api/path";
import { type Dispatch, type SetStateAction, useState } from "react";
import { useEnviroment } from "../../context/EnviromentProvider";
import { loadMetadataSettings, saveMetadataSettings, useMetadataSettings, useSetMetadataSettings } from "../../context/MetadataSettingsProvider";
import { useResourcesSettings, useWorkspaceContext } from "../../context/WorkspaceResourcesProvider";
import type { Attribute, CommandParam, CommandParams } from "../../model/CommandParam";
import type { MetadataSettings } from "../../model/MetadataSettings";
import type { WorkspaceContext } from "../../model/WorkspaceResources";
import { ButtonWithIcon } from "../element/Button";
import { ExpandButton } from "../element/ButtonIcon";
import { DirIcon, EditIcon, FileIcon } from "../element/Icon";
import { CheckBox, ControllTextBox, InputLabel, SelectBox } from "../element/Input";
import SettingsDaialog from "./settings/SettingsDialog";

type Prop = {
	prefix: string;
	element: CommandParam;
};
type FileProp = Prop & {
	path: string;
	setPath: Dispatch<SetStateAction<string>>;
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
				const displayCaption = prop.optionCaption?.display(element.name)
				if (prop.optional?.(element.name) && !showOptional) {
					if (displayCaption) {
						return (
							<ExpandButton
								key={prop.prefix + prop.optionCaption?.caption}
								toggleOptional={toggleOptional}
								showOptional={showOptional}
								caption={prop.optionCaption?.caption}
							/>
						);
					}
					return null;
				}
				if (element.attribute.type === "FLG") {
					return (
						<>
							{displayCaption &&
								<ExpandButton
									key={prop.prefix + prop.optionCaption?.caption}
									toggleOptional={toggleOptional}
									showOptional={showOptional}
									caption={prop.optionCaption?.caption}
								/>
							}
							<Check
								prefix={prop.prefix}
								element={element}
								key={prop.name + prop.prefix + element.name}
							/>
						</>
					);
				}
				if (element.attribute.type === "ENUM") {
					return (
						<>
							{displayCaption &&
								<ExpandButton
									key={prop.prefix + prop.optionCaption?.caption}
									toggleOptional={toggleOptional}
									showOptional={showOptional}
									caption={prop.optionCaption?.caption}
								/>
							}
							<Select
								handleTypeSelect={prop.handleTypeSelect}
								prefix={prop.prefix}
								element={element}
								key={prop.name + prop.prefix + element.name}
							/>
						</>
					);
				}
				return (
					<>
						{displayCaption &&
							<ExpandButton
								key={prop.prefix + prop.optionCaption?.caption}
								toggleOptional={toggleOptional}
								showOptional={showOptional}
								caption={prop.optionCaption?.caption}
							/>
						}
						<Text
							prefix={prop.prefix}
							element={element}
							key={prop.name + prop.prefix + element.name}
						/>
					</>
				);
			})}
		</>
	);
}
function Text(prop: Prop) {
	const [path, setPath] = useState(prop.element.value);
	return (
		<div>
			<InputLabel
				name={getName(prop.prefix, prop.element.name)}
				id={getId(prop.prefix, prop.element.name)}
				required={prop.element.attribute.required}
			/>
			<div className="flex">
				<ControllTextBox
					name={getName(prop.prefix, prop.element.name)}
					id={getId(prop.prefix, prop.element.name)}
					list={prop.element.name === "setting" ? `${getId(prop.prefix, prop.element.name)}_list` : undefined}
					required={prop.element.attribute.required}
					value={path}
					handleChange={(ev) => setPath(ev.target.value)}
				/>
				{prop.element.name === "setting" && (
					<SettingEdit prefix={prop.prefix} element={prop.element} path={path} setPath={setPath} />
				)}
				{prop.element.attribute.type.includes("FILE") && (
					<FileChooser prefix={prop.prefix} element={prop.element} path={path} setPath={setPath} />
				)}
				{prop.element.attribute.type.includes("DIR") && (
					<DirectoryChooser prefix={prop.prefix} element={prop.element} path={path} setPath={setPath} />
				)}
			</div>
		</div>
	);
};
function SettingEdit(prop: FileProp) {
	const environment = useEnviroment();
	const metadataSettings = useMetadataSettings();
	const setMetadataSettings = useSetMetadataSettings();
	const settings = useResourcesSettings().datasetSettings;
	const [dialogEdit, setDialogEdit] = useState(false);
	const handleDialogOpen = () => {
		loadMetadataSettings(environment.apiUrl, prop.path ?? "")
			.then((settings: MetadataSettings) => setMetadataSettings(settings))
			.catch((ex) => alert(ex));
		setDialogEdit(true);
	};
	const handleSave = (path: string) => {
		saveMetadataSettings(environment.apiUrl, path, metadataSettings);
		prop.setPath(path);
		setDialogEdit(false);
	};
	return (
		<>
			<datalist id={`${getId(prop.prefix, prop.element.name)}_list`} >
				{settings?.map((setting) => {
					return (
						<option key={setting} value={setting} />
					)
				})}
			</datalist>
			{dialogEdit && (
				<SettingsDaialog
					fileName={prop.path}
					setFileName={prop.setPath}
					handleDialogClose={() => setDialogEdit(false)}
					handleSave={handleSave}
				/>
			)}
			<ButtonWithIcon
				handleClick={handleDialogOpen}
				id={`${getId(prop.prefix, prop.element.name)}_edit`}
			>
				<EditIcon fill="white" />
			</ButtonWithIcon>
		</>
	);
};
function FileChooser(prop: FileProp) {
	const context = useWorkspaceContext()
	const handleFileChooserClick = () => {
		const getDefaultPath = async (): Promise<string> => {
			return await isAbsolute(prop.path) ? prop.path
				: prop.path ? getPath(context, prop.element.attribute) + sep + prop.path : getPath(context, prop.element.attribute);
		};
		getDefaultPath().then(defaultPath => open({ defaultPath })
			.then((files) => files && prop.setPath((files as string).replace(getPath(context, prop.element.attribute) + sep, ""))));
	};
	return (
		<ButtonWithIcon
			handleClick={handleFileChooserClick}
			id={`${prop.prefix}_${prop.element.name}FileChooser`}
		>
			<FileIcon title="FileChooser" fill="white" />
		</ButtonWithIcon>
	);
};
function DirectoryChooser(prop: FileProp) {
	const context = useWorkspaceContext()
	const handleDirectoryChooserClick = () => {
		const getDefaultPath = async (): Promise<string> => {
			return await isAbsolute(prop.path) ? prop.path
				: prop.path ? getPath(context, prop.element.attribute) + sep + prop.path : getPath(context, prop.element.attribute);
		};
		getDefaultPath().then(defaultPath => open({ defaultPath, directory: true })
			.then((files) => files && prop.setPath((files as string).replace(getPath(context, prop.element.attribute) + sep, ""))));
	};
	return (
		<ButtonWithIcon
			handleClick={handleDirectoryChooserClick}
			id={`${prop.prefix}_${prop.element.name}DirectoryChooser`}
		>
			<DirIcon title="DirectoryChooser" fill="white" />
		</ButtonWithIcon>
	);
};
function Check(prop: Prop) {
	return (
		<div>
			<InputLabel
				name={prop.prefix ? `-${prop.prefix}.${prop.element.name}` : `-${prop.element.name}`}
				id={`${prop.prefix}_${prop.element.name}`}
				required={false}
			/>
			<CheckBox
				name={prop.prefix ? `-${prop.prefix}.${prop.element.name}` : `-${prop.element.name}`}
				id={`${prop.prefix}_${prop.element.name}`}
				defaultValue={prop.element.value}
			/>
		</div>
	);
};
function Select(prop: SelectProp) {
	return (
		<div>
			<InputLabel
				name={prop.prefix ? `-${prop.prefix}.${prop.element.name}` : `-${prop.element.name}`}
				id={`${prop.prefix}_${prop.element.name}`}
				required={prop.element.attribute.required}
			/>
			<SelectBox
				name={prop.prefix ? `-${prop.prefix}.${prop.element.name}` : `-${prop.element.name}`}
				id={`${prop.prefix}_${prop.element.name}`}
				required={true}
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