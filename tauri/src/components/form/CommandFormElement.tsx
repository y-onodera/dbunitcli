import { open } from "@tauri-apps/api/dialog";
import { type Dispatch, type SetStateAction, useEffect, useState } from "react";
import { useEnviroment } from "../../context/EnviromentProvider";
import { loadMetadataSettings, useSetMetadataSettings } from "../../context/MetadataSettingsProvider";
import { useResourcesSettings } from "../../context/WorkspaceResourcesProvider";
import type { CommandParam, CommandParams } from "../../model/CommandParam";
import type { MetadataSettings } from "../../model/MetadataSettings";
import { ButtonWithIcon } from "../element/Button";
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
	return (
		<>
			{prop.elements.map((element) => {
				if (element.attribute.type === "FLG") {
					return (
						<Check
							prefix={prop.prefix}
							element={element}
							key={prop.name + prop.prefix + element.name}
						/>
					);
				}
				if (element.attribute.type === "ENUM") {
					return (
						<Select
							handleTypeSelect={prop.handleTypeSelect}
							prefix={prop.prefix}
							element={element}
							key={prop.name + prop.prefix + element.name}
						/>
					);
				}
				return (
					<Text
						prefix={prop.prefix}
						element={element}
						key={prop.name + prop.prefix + element.name}
					/>
				);
			})}
		</>
	);
}
const Text: React.FC<Prop> = ({ prefix, element }) => {
	const [path, setPath] = useState("");
	useEffect(() => {
		setPath(element.value);
	}, [element]);
	return (
		<div>
			<InputLabel
				name={getName(prefix, element.name)}
				id={getId(prefix, element.name)}
				required={element.attribute.required}
			/>
			<div className="flex">
				<ControllTextBox
					name={getName(prefix, element.name)}
					id={getId(prefix, element.name)}
					list={element.name === "setting" ? `${getId(prefix, element.name)}_list` : undefined}
					required={element.attribute.required}
					value={path}
					handleChange={(ev) => setPath(ev.target.value)}
				/>
				{element.name === "setting" && (
					<SettingEdit prefix={prefix} element={element} path={path} setPath={setPath} />
				)}
				{element.attribute.type.includes("FILE") && (
					<FileChooser prefix={prefix} element={element} path={path} setPath={setPath} />
				)}
				{element.attribute.type.includes("DIR") && (
					<DirectoryChooser prefix={prefix} element={element} path={path} setPath={setPath} />
				)}
			</div>
		</div>
	);
};
const SettingEdit: React.FC<FileProp> = ({ prefix, element, path, setPath }) => {
	const environment = useEnviroment();
	const [dialogEdit, setDialogEdit] = useState(false);
	const setMetadataSettings = useSetMetadataSettings();
	const settings = useResourcesSettings().datasetSettings;
	const handleDialogOpen = () => {
		loadMetadataSettings(environment.apiUrl, path ?? "")
			.then((settings: MetadataSettings) => setMetadataSettings(settings))
			.catch((ex) => alert(ex));
		setDialogEdit(true);
	};
	const handleSave = (path: string) => {
		setPath(path)
		setDialogEdit(false);
	};
	return (
		<>
			<datalist id={`${getId(prefix, element.name)}_list`} >
				{settings?.map((setting) => {
					return (
						<option key={setting} value={setting}>{setting}</option>
					)
				})}
			</datalist>
			{dialogEdit && (
				<SettingsDaialog
					settingName={path}
					handleDialogClose={() => setDialogEdit(false)}
					handleSave={handleSave}
				/>
			)}
			<ButtonWithIcon
				handleClick={handleDialogOpen}
				id={`${getId(prefix, element.name)}_edit`}
			>
				<EditIcon fill="white" />
			</ButtonWithIcon>
		</>
	);
};
const FileChooser: React.FC<FileProp> = ({ prefix, element, setPath }) => {
	const handleFileChooserClick = () => {
		open().then((files) => files && setPath(files as string));
	};
	return (
		<ButtonWithIcon
			handleClick={handleFileChooserClick}
			id={`${prefix}_${element.name}FileChooser`}
		>
			<FileIcon title="FileChooser" fill="white" />
		</ButtonWithIcon>
	);
};
const DirectoryChooser: React.FC<FileProp> = ({ prefix, element, setPath }) => {
	const handleDirectoryChooserClick = () => {
		open({ directory: true }).then(
			(files) => files && setPath(files as string),
		);
	};
	return (
		<ButtonWithIcon
			handleClick={handleDirectoryChooserClick}
			id={`${prefix}_${element.name}DirectoryChooser`}
		>
			<DirIcon title="DirectoryChooser" fill="white" />
		</ButtonWithIcon>
	);
};
const Check: React.FC<Prop> = ({ prefix, element }) => {
	return (
		<div>
			<InputLabel
				name={prefix ? `-${prefix}.${element.name}` : `-${element.name}`}
				id={`${prefix}_${element.name}`}
				required={false}
			/>
			<CheckBox
				name={prefix ? `-${prefix}.${element.name}` : `-${element.name}`}
				id={`${prefix}_${element.name}`}
				defaultValue={element.value}
			/>
		</div>
	);
};
const Select: React.FC<SelectProp> = ({
	handleTypeSelect,
	prefix,
	element,
}) => {
	return (
		<div>
			<InputLabel
				name={prefix ? `-${prefix}.${element.name}` : `-${element.name}`}
				id={`${prefix}_${element.name}`}
				required={element.attribute.required}
			/>
			<SelectBox
				name={prefix ? `-${prefix}.${element.name}` : `-${element.name}`}
				id={`${prefix}_${element.name}`}
				required={true}
				handleOnChange={handleTypeSelect}
				defaultValue={element.value}
			>
				{element.attribute.selectOption.map((value) => {
					return (
						<option key={prefix + element.name + value} value={value}>
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