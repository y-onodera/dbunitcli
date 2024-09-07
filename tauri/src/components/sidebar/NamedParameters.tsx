import { Body, ResponseType, fetch } from "@tauri-apps/api/http";
import { useEffect, useState } from "react";
import { type EditName, useSetEditName } from "../../context/EditNameProvider";
import { useEnviroment } from "../../context/EnviromentProvider";
import { useSetSelectParameter } from "../../context/SelectParameterProvider";
import type { Parameter } from "../../model/CommandParam";
import { LinkeButton } from "../element/Button";
import { AddButton } from "../element/ButtonIcon";
import { ExpandIcon, SettingIcon } from "../element/Icon";

type NamedParameters = {
	convert: string[];
	compare: string[];
	generate: string[];
	run: string[];
	parameterize: string[];
};
type NamedParameterProp = {
	command: string;
	namedParameters?: string[];
	handleParameterSelect: (command: string, name: string) => Promise<void>;
	handleEditNamed: (selected: EditName) => void;
};
export default function NamedParameters() {
	const environment = useEnviroment();
	const [parameters, setParameters] = useState<NamedParameters>();
	useEffect(() => {
		const handlMenuInit = async () => {
			await fetch(`${environment.apiUrl}parameter/list`, {
				method: "GET",
				responseType: ResponseType.JSON,
			})
				.then((response) => {
					if (!response.ok) {
						console.error("response.ok:", response.ok);
						console.error("esponse.status:", response.status);
						throw new Error(response.data as string);
					}
					setParameters(response.data as NamedParameters);
				})
				.catch((ex) => alert(ex));
		};
		handlMenuInit();
	}, [environment]);
	const setParameter = useSetSelectParameter();
	const handleParameterSelect = async (command: string, name: string) => {
		await fetch(`${environment.apiUrl + command}/load`, {
			method: "POST",
			responseType: ResponseType.JSON,
			headers: { "Content-Type": "application/json" },
			body: Body.json({ name }),
		})
			.then((response) => {
				if (!response.ok) {
					console.error("response.ok:", response.ok);
					console.error("esponse.status:", response.status);
					throw new Error(response.data as string);
				}
				setParameter(response.data as Parameter, command, name);
			})
			.catch((ex) => alert(ex));
	};
	const setEditName = useSetEditName();
	const handleEditNamed = (selected: EditName) => setEditName(selected);
	return (
		<ul className="space-y-2 font-medium">
			<Category
				command="Convert"
				namedParameters={parameters?.convert}
				handleParameterSelect={handleParameterSelect}
				handleEditNamed={handleEditNamed}
			/>
			<Category
				command="Compare"
				namedParameters={parameters?.compare}
				handleParameterSelect={handleParameterSelect}
				handleEditNamed={handleEditNamed}
			/>
			<Category
				command="Generate"
				namedParameters={parameters?.generate}
				handleParameterSelect={handleParameterSelect}
				handleEditNamed={handleEditNamed}
			/>
			<Category
				command="Run"
				namedParameters={parameters?.run}
				handleParameterSelect={handleParameterSelect}
				handleEditNamed={handleEditNamed}
			/>
			<Category
				command="Parameterize"
				namedParameters={parameters?.parameterize}
				handleParameterSelect={handleParameterSelect}
				handleEditNamed={handleEditNamed}
			/>
		</ul>
	);
}
function Category(prop: NamedParameterProp) {
	const [close, setClose] = useState(true);
	const toggleMenu = () => setClose(!close);
	return (
		<li>
			<button
				type="button"
				onClick={toggleMenu}
				className="flex items-center 
                            w-full 
							p-2 
                            text-base text-gray-900 
                            rounded-lg 
                            group 
                            ring-indigo-300 
                            focus-visible:ring
                            hover:bg-gray-100 "
			>
				<ExpandIcon close={close} />
				<span
					className="ms-2  
                           text-left 
                           rtl:text-right 
                           whitespace-nowrap"
				>
					{prop.command}
				</span>
			</button>
			<ul id={`${prop.command}-list`} className="py-1 space-y-1" hidden={close}>
				<Parameters
					command={prop.command}
					namedParameters={prop.namedParameters}
					handleParameterSelect={prop.handleParameterSelect}
					handleEditNamed={prop.handleEditNamed}
				/>
			</ul>
		</li>
	);
}
function Parameters(prop: NamedParameterProp) {
	const environment = useEnviroment();
	const [menuList, setMenuList] = useState([] as string[]);
	useEffect(
		() =>
			setMenuList((current) =>
				prop.namedParameters ? [...prop.namedParameters] : current,
			),
		[prop.namedParameters],
	);
	const handleAddNewName = async () => {
		await fetch(`${environment.apiUrl + prop.command.toLowerCase()}/add`, {
			method: "GET",
			responseType: ResponseType.JSON,
		})
			.then((response) => {
				if (!response.ok) {
					console.error("response.ok:", response.ok);
					console.error("esponse.status:", response.status);
					throw new Error(response.data as string);
				}
				setMenuList(response.data as string[]);
			})
			.catch((ex) => alert(ex));
	};
	return (
		<>
			{menuList?.map((menu) => {
				return (
					<li key={menu} className="flex">
						<LinkeButton title={menu}
							handleClick={() =>
								prop.handleParameterSelect(prop.command.toLowerCase(), menu)
							}
						/>
						<button
							type="button"
							onClick={(target) =>
								prop.handleEditNamed({
									command: prop.command.toLowerCase(),
									name: menu,
									x: target.clientX,
									y: target.clientY,
									afterEdge: target.clientY > 300,
									setMenuList,
								})
							}
							className="p-1
                           ring-indigo-300 
                           focus-visible:ring "
						>
							<SettingIcon />
						</button>
					</li>
				);
			})}
			<li>
				<AddButton handleClick={handleAddNewName} />
			</li>
		</>
	);
}
