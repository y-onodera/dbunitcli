import { Body, ResponseType, fetch } from "@tauri-apps/api/http";
import { useEffect, useState } from "react";
import { type EditName, useSetEditName } from "../../context/EditNameProvider";
import { useEnviroment } from "../../context/EnviromentProvider";
import { useSetSelectParameter } from "../../context/SelectParameterProvider";
import type { Parameter } from "../../model/CommandParam";
import { LinkButton } from "../element/Button";
import { AddButton, ButtonIcon, SettingButton } from "../element/ButtonIcon";
import { ExpandIcon } from "../element/Icon";

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
function Category(props: NamedParameterProp) {
	const [close, setClose] = useState(true);
	const toggleMenu = () => setClose(!close);
	return (
		<li>
			<ButtonIcon title="" handleClick={toggleMenu} >
				<ExpandIcon close={close} />
				<span
					className="ms-2  
                           text-left 
                           rtl:text-right 
                           whitespace-nowrap"
				>
					{props.command}
				</span>
			</ButtonIcon>
			<ul id={`${props.command}-list`} className="py-1 space-y-1" hidden={close}>
				<Parameters
					command={props.command}
					namedParameters={props.namedParameters}
					handleParameterSelect={props.handleParameterSelect}
					handleEditNamed={props.handleEditNamed}
				/>
			</ul>
		</li>
	);
}
function Parameters(props: NamedParameterProp) {
	const environment = useEnviroment();
	const [menuList, setMenuList] = useState([] as string[]);
	useEffect(
		() =>
			setMenuList((current) =>
				props.namedParameters ? [...props.namedParameters] : current,
			),
		[props.namedParameters],
	);
	const handleAddNewName = async () => {
		await fetch(`${environment.apiUrl + props.command.toLowerCase()}/add`, {
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
						<LinkButton title={menu}
							handleClick={() =>
								props.handleParameterSelect(props.command.toLowerCase(), menu)
							}
						/>
						<SettingButton title="" handleClick={(target) =>
							props.handleEditNamed({
								command: props.command.toLowerCase(),
								name: menu,
								x: target.clientX,
								y: target.clientY,
								afterEdge: target.clientY > 300,
								setMenuList,
							})
						} />
					</li>
				);
			})}
			<li>
				<AddButton handleClick={handleAddNewName} />
			</li>
		</>
	);
}
