import type { Dispatch, ReactNode, SetStateAction } from "react";
import { useState } from "react";
import {
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
} from "../../../../components/element/Input";
import { useWorkspaceContext } from "../../../../context/WorkspaceResourcesProvider";
import type { TextProp } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

interface Props extends TextProp {
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
	resourceFiles = [],
	handleValueChange: onValueChange,
	children,
}: Props) {
	const [path, setPath] = useState(element.value);
	const hasResources = resourceFiles.length > 0;
	const isValueInDatalist = resourceFiles.includes(path);
	const context = useWorkspaceContext();
	const defaultPath = context.getPath(element.attribute.defaultPath);
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
					<ResourceDatalist id={id} resources={resourceFiles} />
					{!hidden && (
						<p className="text-xs text-gray-400 truncate">{defaultPath}</p>
					)}
				</div>
				{!hidden && children && children({ path, setPath, isValueInDatalist })}
			</div>
		</div>
	);
}
