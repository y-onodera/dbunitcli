import type { Dispatch, ReactNode, SetStateAction } from "react";
import { useState } from "react";
import {
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
} from "../../../components/element/Input";
import { useWorkspaceContext } from "../../../context/WorkspaceResourcesProvider";
import { getPath } from "./Chooser";
import type { Prop } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

interface Props extends Prop {
	resourceFiles?: string[];
	onValueChange?: (value: string) => void;
	children?: (args: {
		path: string;
		setPath: Dispatch<SetStateAction<string>>;
		isValueInDatalist: boolean;
	}) => ReactNode;
}

export default function ResourceText({
	prefix,
	element,
	hidden,
	srcType,
	resourceFiles = [],
	onValueChange,
	children,
}: Props) {
	const [path, setPath] = useState(element.value);
	const hasResources = resourceFiles.length > 0;
	const isValueInDatalist = resourceFiles.includes(path);
	const context = useWorkspaceContext();
	const defaultPath = getPath(context, element.attribute.defaultPath, srcType);
	const id = getId(prefix, element.name);
	const fieldName = getName(prefix, element.name);

	const handleChange = (ev: React.ChangeEvent<HTMLInputElement>) => {
		const newValue = ev.target.value;
		setPath(newValue);
		onValueChange?.(newValue);
	};

	return (
		<div>
			<InputLabel
				text={fieldName}
				id={id}
				required={element.attribute.required}
				hidden={hidden}
			/>
			<div className="flex">
				<div className={children ? "flex-1" : "flex-1 mr-36"}>
					<ControllTextBox
						name={fieldName}
						id={id}
						list={hasResources ? `${id}_list` : undefined}
						hidden={hidden}
						required={element.attribute.required}
						value={path}
						handleChange={handleChange}
					/>
					{!hidden && hasResources && (
						<ResourceDatalist id={id} resources={resourceFiles} />
					)}
					{!hidden && defaultPath && (
						<p className="text-xs text-gray-400 truncate">{defaultPath}</p>
					)}
				</div>
				{!hidden && children && children({ path, setPath, isValueInDatalist })}
			</div>
		</div>
	);
}
