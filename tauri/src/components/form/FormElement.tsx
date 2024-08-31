import { open } from "@tauri-apps/api/dialog";
import { type Dispatch, type SetStateAction, useEffect, useState } from "react";
import type { CommandParam, CommandParams } from "../../model/CommandParam";

type Prop = {
	prefix: string;
	element: CommandParam;
	setPath?: Dispatch<SetStateAction<string>>;
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
			<label
				htmlFor={`${prefix}_${element.name}`}
				className="block 
          mb-2 
          text-sm text-gray-900 
          font-medium 
          dark:text-white"
			>
				-{prefix && `${prefix}.`}
				{element.name}
				{element.attribute.required && "*"}
			</label>
			<div className="flex">
				<input
					name={prefix ? `-${prefix}.${element.name}` : `-${element.name}`}
					id={`${prefix}_${element.name}`}
					type="text"
					className="block 
                  p-2.5 
                  w-full 
                  z-20 
                  text-sm text-gray-900 
                  bg-gray-50 
                  rounded-lg 
                  border border-gray-300 
                  ring-indigo-300 
                  focus-visible:ring 
                  dark:bg-gray-700 
                  dark:border-gray-600 
                  dark:placeholder-gray-400 
                  dark:text-white 
                  dark:focus:border-blue-500"
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
		<>
			<button
				type="button"
				id={`${prefix}_${element.name}FileChooser`}
				onClick={handleFileChooserClick}
				className="p-2.5 
                            ms-2 
                            text-sm 
                            font-medium 
                            text-white 
                            bg-indigo-500 
                            rounded-lg 
                            border border-gray-300 
                            ring-indigo-300 
                            focus-visible:ring 
                            hover:bg-indigo-600 
                            dark:bg-blue-600 
                            dark:hover:bg-indigo-700 
                            dark:focus:ring-indigo-800"
			>
				<svg
					xmlns="http://www.w3.org/2000/svg"
					height="24px"
					viewBox="0 -960 960 960"
					width="24px"
					fill="white"
				>
					<title>FileChooser</title>
					<path d="M240-80q-33 0-56.5-23.5T160-160v-640q0-33 23.5-56.5T240-880h320l240 240v240h-80v-200H520v-200H240v640h360v80H240Zm638 15L760-183v89h-80v-226h226v80h-90l118 118-56 57Zm-638-95v-640 640Z" />
				</svg>
			</button>
		</>
	);
};
const DirectoryChooser: React.FC<FileProp> = ({ prefix, element, setPath }) => {
	const handleDirectoryChooserClick = () => {
		open({ directory: true }).then(
			(files) => files && setPath(files as string),
		);
	};
	return (
		<>
			<button
				type="button"
				id={`${prefix}_${element.name}DirectoryChooser`}
				onClick={handleDirectoryChooserClick}
				className="p-2.5 
                            ms-2 
                            text-sm 
                            font-medium 
                            text-white 
                            bg-indigo-500 
                            rounded-lg 
                            border border-gray-300 
                            ring-indigo-300 
                            focus-visible:ring 
                            hover:bg-indigo-600 
                            dark:bg-blue-600 
                            dark:hover:bg-indigo-700 
                            dark:focus:ring-indigo-800"
			>
				<svg
					xmlns="http://www.w3.org/2000/svg"
					height="24px"
					viewBox="0 -960 960 960"
					width="24px"
					fill="white"
				>
					<title>DirectoryChooser</title>
					<path d="M160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h240l80 80h320q33 0 56.5 23.5T880-640H447l-80-80H160v480l96-320h684L837-217q-8 26-29.5 41.5T760-160H160Zm84-80h516l72-240H316l-72 240Zm0 0 72-240-72 240Zm-84-400v-80 80Z" />
				</svg>
			</button>
		</>
	);
};
const Check: React.FC<Prop> = ({ prefix, element }) => {
	const [checked, setChecked] = useState(false);
	useEffect(() => {
		setChecked(element.value === "true");
	}, [element]);
	return (
		<div>
			<label
				htmlFor={`${prefix}_${element.name}`}
				className="block 
                                         mb-2 
                                         text-sm text-gray-900 
                                         font-medium 
                                         dark:text-gray-300"
			>
				-{prefix && `${prefix}.`}
				{element.name}
			</label>
			<input
				name={prefix ? `-${prefix}.${element.name}` : `-${element.name}`}
				id={`${prefix}_${element.name}`}
				type="checkbox"
				className="w-4 h-4 
                                                                      text-indigo-500 
                                                                      bg-gray-50 
                                                                      border border-gray-300 
                                                                      ring-indigo-300 
                                                                      focus-visible:ring 
                                                                      dark:bg-blue-600 
                                                                      dark:hover:bg-indigo-700 
                                                                      dark:focus:ring-indigo-800"
				checked={checked}
				value={`${checked}`}
				onChange={() => {
					setChecked(!checked);
				}}
			/>
			<input
				name={prefix ? `-${prefix}.${element.name}` : `-${element.name}`}
				id={`${prefix}_${element.name}hidden`}
				type="hidden"
				value={`${checked}`}
			/>
		</div>
	);
};
const Select: React.FC<SelectProp> = ({
	handleTypeSelect,
	prefix,
	element,
}) => {
	const [selected, setSelected] = useState("");
	useEffect(() => {
		setSelected(element.value);
	}, [element]);
	return (
		<div>
			<label
				htmlFor={`${prefix}_${element.name}`}
				className="block
                                        mb-2 
                                        text-sm text-gray-900
                                        font-medium 
                                        dark:text-white"
			>
				-{prefix && `${prefix}.`}
				{element.name}
				{element.attribute.required && "*"}
			</label>
			<select
				name={prefix ? `-${prefix}.${element.name}` : `-${element.name}`}
				id={`${prefix}_${element.name}`}
				className="block 
                                                     w-40 
                                                     p-2.5 
                                                     z-20 
                                                     bg-gray-50 
                                                     text-sm text-gray-900
                                                     rounded-lg 
                                                     border border-gray-300 
                                                     ring-indigo-300 
                                                     focus:ring 
                                                     focus-visible:ring 
                                                     dark:bg-gray-700 
                                                     dark:border-gray-600 
                                                     dark:placeholder-gray-400 
                                                     dark:text-white 
                                                     dark:focus:ring-blue-500 
                                                     dark:focus:border-blue-500"
				required
				value={selected}
				onChange={(event) => {
					setSelected(event.currentTarget.value);
					handleTypeSelect?.();
				}}
			>
				{element.attribute.selectOption.map((value) => {
					return (
						<option key={prefix + element.name + value} value={value}>
							{value}
						</option>
					);
				})}
			</select>
		</div>
	);
};
