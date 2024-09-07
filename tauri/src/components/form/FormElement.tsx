import { open } from "@tauri-apps/api/dialog";
import { type Dispatch, type SetStateAction, useEffect, useState } from "react";
import type { CommandParam, CommandParams } from "../../model/CommandParam";
import { ButtonWithIcon } from "../element/Button";
import { DirIcon, EditIcon, FileIcon } from "../element/Icon";
import { CheckBox, InputLabel, SelectBox, TextBox } from "../element/Input";

type Prop = {
	prefix: string;
	element: CommandParam;
};
type FileProp = Prop & {
	setPath: Dispatch<SetStateAction<string>>;
};
type SelectProp = Prop & {
	handleTypeSelect: () => Promise<void>;
};
export default function FormElements(prop: CommandParams) {
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
				name={prefix ? `-${prefix}.${element.name}` : `-${element.name}`}
				id={`${prefix}_${element.name}`}
				required={element.attribute.required}
			/>
			<div className="flex">
				<TextBox
					name={prefix ? `-${prefix}.${element.name}` : `-${element.name}`}
					id={`${prefix}_${element.name}`}
					required={element.attribute.required}
					defaultValue={path}
				/>
				{element.attribute.type.includes("FILE") && (
					<FileChooser prefix={prefix} element={element} setPath={setPath} />
				)}
				{element.attribute.type.includes("DIR") && (
					<DirectoryChooser
						prefix={prefix}
						element={element}
						setPath={setPath}
					/>
				)}
			</div>
		</div>
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
