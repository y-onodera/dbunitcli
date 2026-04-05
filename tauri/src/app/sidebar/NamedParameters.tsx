import { useState } from "react";
import { LinkButton } from "../../components/element/Button";
import {
	AddButton,
	ButtonIcon,
	SettingButton,
} from "../../components/element/ButtonIcon";
import { ExpandIcon } from "../../components/element/Icon";
import { useParameterList } from "../../context/WorkspaceResourcesProvider";
import { useLoadSelectParameter } from "../../hooks/useSelectParameter";
import { useAddParameter } from "../../hooks/useWorkspaceResources";
import type { Command } from "../../model/SelectParameter";
import type { EditName } from "../main/Sidebar";

type NamedParameterProp = {
	command: Command;
	namedParameters?: string[];
	handleParameterSelect: (command: Command, name: string) => Promise<void>;
	handleEditNamed: (selected: EditName) => void;
};
export default function NamedParameters({
	setEditName,
}: {
	setEditName: (editName: EditName) => void;
}) {
	const parameters = useParameterList();
	const handleParameterSelect = useLoadSelectParameter();
	const handleEditNamed = (selected: EditName) => setEditName(selected);
	return (
		<ul className="space-y-2 font-medium">
			<Category
				command="convert"
				namedParameters={parameters.convert}
				handleParameterSelect={handleParameterSelect}
				handleEditNamed={handleEditNamed}
			/>
			<Category
				command="compare"
				namedParameters={parameters.compare}
				handleParameterSelect={handleParameterSelect}
				handleEditNamed={handleEditNamed}
			/>
			<Category
				command="generate"
				namedParameters={parameters.generate}
				handleParameterSelect={handleParameterSelect}
				handleEditNamed={handleEditNamed}
			/>
			<Category
				command="run"
				namedParameters={parameters.run}
				handleParameterSelect={handleParameterSelect}
				handleEditNamed={handleEditNamed}
			/>
			<Category
				command="parameterize"
				namedParameters={parameters.parameterize}
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
			<ButtonIcon title="" handleClick={toggleMenu}>
				<ExpandIcon close={close} />
				<span className="ms-2 text-left rtl:text-right whitespace-nowrap">
					{props.command.charAt(0).toUpperCase() + props.command.slice(1)}
				</span>
			</ButtonIcon>
			<ul
				id={`${props.command}-list`}
				className="py-1 space-y-1"
				hidden={close}
			>
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
	const handleAddNewName = useAddParameter(props.command);
	return (
		<>
			{props.namedParameters?.map((menu) => {
				return (
					<li key={menu} className="flex">
						<LinkButton
							title={menu}
							handleClick={() =>
								props.handleParameterSelect(props.command, menu)
							}
						/>
						<SettingButton
							title=""
							handleClick={(target) =>
								props.handleEditNamed({
									command: props.command.toLowerCase(),
									name: menu,
									x: target.clientX,
									y: target.clientY,
									afterEdge: target.clientY > 300,
								})
							}
						/>
					</li>
				);
			})}
			<li>
				<AddButton handleClick={handleAddNewName} />
			</li>
		</>
	);
}
