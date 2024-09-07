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
import { CopyButton, DeleteButton, EditButton, FixButton } from "../element/ButtonIcon";
import { ControllTextBox } from "../element/Input";

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
	const [hidden, setHidden] = useState(true);
	const [newName, setNewName] = useState(prop.name);
	const handleRenameTextOnChange = (text: string) => setNewName(text);
	return (
		<ul className="space-y-4">
			<li>
				<DeleteButton handleClick={prop.handleMenuDelete} />
			</li>
			<li>
				<div className="flex gap-x-0.5">
					<EditButton title="rename" handleClick={() => setHidden((current) => !current)} />
					{!hidden && <>
						<ControllTextBox name="menuRename" id="menuRename" required={false} w="w-50"
							value={newName} handleChange={(text) => handleRenameTextOnChange(text.target.value)} />
						<FixButton title="" handleClick={() => {
							setHidden((current) => !current);
							prop.handleMenuRename(newName);
						}}
						/>
					</>
					}
				</div>
			</li>
			<li>
				<CopyButton handleClick={prop.handleMenuCopy} />
			</li>
		</ul>
	);
}
