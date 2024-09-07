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
import { CopyButton, DeleteButton, EditButton } from "../element/ButtonIcon";

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
				<DeleteButton handleClick={prop.handleMenuDelete} />
			</li>
			<li>
				<div className="flex">
					<EditButton title="rename" handleClick={() => setVisible(!visible)} />
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
				<CopyButton handleClick={prop.handleMenuCopy} />
			</li>
		</ul>
	);
}
