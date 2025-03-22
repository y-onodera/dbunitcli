import { useEffect, useState } from "react";
import { LinkButton } from "../../components/element/Button";
import { AddButton, ButtonIcon, SettingButton } from "../../components/element/ButtonIcon";
import { ExpandIcon } from "../../components/element/Icon";
import { type EditName, useSetEditName } from "../../context/EditNameProvider";
import { useLoadSelectParameter } from "../../context/SelectParameterProvider";
import { useAddParameter, useParameterList } from "../../context/WorkspaceResourcesProvider";


type NamedParameterProp = {
	command: string;
	namedParameters?: string[];
	handleParameterSelect: (command: string, name: string) => Promise<void>;
	handleEditNamed: (selected: EditName) => void;
};
export default function NamedParameters() {
	const parameters = useParameterList();
	const handleParameterSelect = useLoadSelectParameter();
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
	const [menuList, setMenuList] = useState([] as string[]);
	useEffect(
		() => setMenuList((current) => props.namedParameters ? [...props.namedParameters] : current)
		, [props.namedParameters],
	);
	const handleAddNewName = useAddParameter(props.command)
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
