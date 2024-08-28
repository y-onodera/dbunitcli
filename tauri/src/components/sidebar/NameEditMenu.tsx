import { Body, ResponseType, fetch } from "@tauri-apps/api/http";
import { useEffect, useRef, useState } from "react";
import {
	type EditName,
	useEditName,
	useSetEditName,
} from "../../context/EditNameProvider";
import { useEnviroment } from "../../context/EnviromentProvider";
import {
	useSelectParameter,
	useSetSelectParameter,
} from "../../context/SelectParameterProvider";

type MenuEditProp = {
	name: string;
	handleMenuDelete: () => void;
	handleMenuRename: (newName: string) => void;
	handleMenuCopy: () => void;
};
export default function NameEditMenu() {
	const wrapperRef = useRef<HTMLDivElement>(null);
	const editName = useEditName();
	const setEditName = useSetEditName();
	const parameter = useSelectParameter();
	const setParameter = useSetSelectParameter();
	const environment = useEnviroment();
	useEffect(() => {
		function handleClickOutside(event: Event) {
			if (
				wrapperRef.current &&
				!wrapperRef.current.contains(event.target as HTMLElement)
			) {
				setEditName({} as EditName);
			}
		}
		document.addEventListener("mousedown", handleClickOutside);
		return () => {
			document.removeEventListener("mousedown", handleClickOutside);
		};
	}, [setEditName]);
	const handleMenuDelete = async () => {
		await fetch(`${environment.apiUrl + editName.command}/delete`, {
			method: "POST",
			responseType: ResponseType.JSON,
			headers: { "Content-Type": "application/json" },
			body: Body.json({ name: editName.name }),
		})
			.then((response) => {
				if (!response.ok) {
					console.error("response.ok:", response.ok);
					console.error("esponse.status:", response.status);
					throw new Error(response.data as string);
				}
				editName.setMenuList(response.data as string[]);
			})
			.catch((ex) => alert(ex));
	};
	const handleMenuCopy = async () => {
		await fetch(`${environment.apiUrl + editName.command}/copy`, {
			method: "POST",
			responseType: ResponseType.JSON,
			headers: { "Content-Type": "application/json" },
			body: Body.json({ name: editName.name }),
		})
			.then((response) => {
				if (!response.ok) {
					console.error("response.ok:", response.ok);
					console.error("esponse.status:", response.status);
					throw new Error(response.data as string);
				}
				editName.setMenuList(response.data as string[]);
			})
			.catch((ex) => alert(ex));
	};
	const handleMenuRename = async (newName: string) => {
		await fetch(`${environment.apiUrl + editName.command}/rename`, {
			method: "POST",
			responseType: ResponseType.JSON,
			headers: { "Content-Type": "application/json" },
			body: Body.json({ oldName: editName.name, newName }),
		})
			.then((response) => {
				if (!response.ok) {
					console.error("response.ok:", response.ok);
					console.error("esponse.status:", response.status);
					throw new Error(response.data as string);
				}
				editName.setMenuList(response.data as string[]);
				if (
					parameter.command === editName.command &&
					parameter.name === editName.name
				) {
					setParameter(
						parameter.currentParameter(),
						parameter.command,
						newName,
					);
				}
			})
			.catch((ex) => alert(ex));
	};
	return (
		<>
			{editName.name && (
				<div
					ref={wrapperRef}
					className="absolute z-10 p-4 pb-0 text-gray-900 bg-white border border-gray-100 rounded-lg shadow-md"
					style={
						editName.afterEdge
							? { left: editName.x, top: editName.y - 150 }
							: { left: editName.x, top: editName.y }
					}
				>
					<MenuEdit
						name={editName.name}
						handleMenuDelete={() => {
							handleMenuDelete();
							setEditName({} as EditName);
						}}
						handleMenuRename={(newName: string) => {
							handleMenuRename(newName);
							setEditName({} as EditName);
						}}
						handleMenuCopy={() => {
							handleMenuCopy();
							setEditName({} as EditName);
						}}
					/>
				</div>
			)}
		</>
	);
}
function MenuEdit(prop: MenuEditProp) {
	const [visible, setVisible] = useState(false);
	const [newName, setNewName] = useState(prop.name);
	const handleRenameTextOnChange = (text: string) => setNewName(text);
	return (
		<ul className="space-y-4">
			<li>
				<button
					type="button"
					onClick={() => prop.handleMenuDelete()}
					className="flex p-1 items-center text-gray-500 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-500 group"
				>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						height="24px"
						width="24px"
						viewBox="0 -960 960 960"
						fill="#5f6368"
					>
						<title>delete</title>
						<path d="M280-120q-33 0-56.5-23.5T200-200v-520h-40v-80h200v-40h240v40h200v80h-40v520q0 33-23.5 56.5T680-120H280Zm400-600H280v520h400v-520ZM360-280h80v-360h-80v360Zm160 0h80v-360h-80v360ZM280-720v520-520Z" />
					</svg>
					delete
				</button>
			</li>
			<li>
				<div className="flex">
					<button
						type="button"
						onClick={() => setVisible(!visible)}
						className="flex p-1 items-center text-gray-500 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-500 group"
					>
						<svg
							xmlns="http://www.w3.org/2000/svg"
							height="24"
							viewBox="0 0 24 24"
							width="24"
						>
							<title>rename</title>
							<path d="M0 0h24v24H0z" fill="none" />
							<path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
						</svg>
						rename
					</button>
					<input
						type="text"
						onChange={(text) => handleRenameTextOnChange(text.target.value)}
						className="p-2.5 
                      w-50 
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
						value={newName}
						hidden={!visible}
					/>
					<button
						type="button"
						className="p-1"
						hidden={!visible}
						onClick={() => {
							setVisible(!visible);
							prop.handleMenuRename(newName);
						}}
					>
						<svg
							xmlns="http://www.w3.org/2000/svg"
							height="24px"
							viewBox="0 -960 960 960"
							width="24px"
							fill="#5f6368"
						>
							<title>fix</title>
							<path d="M382-240 154-468l57-57 171 171 367-367 57 57-424 424Z" />
						</svg>
					</button>
				</div>
			</li>
			<li>
				<button
					type="button"
					onClick={() => prop.handleMenuCopy()}
					className="flex p-1 items-center text-gray-500 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-500 group"
				>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						height="24"
						viewBox="0 0 24 24"
						width="24"
					>
						<title>copy</title>
						<path d="M0 0h24v24H0z" fill="none" />
						<path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z" />
					</svg>
					copy
				</button>
			</li>
		</ul>
	);
}
