import type { Dispatch, ReactNode, SetStateAction } from "react";
import { useState } from "react";
import {
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
} from "../../../components/element/Input";
import type { Prop } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

interface Props extends Prop {
	resourceFiles: string[];
	onValueChange?: (value: string) => void;
	children: (args: {
		path: string;
		setPath: Dispatch<SetStateAction<string>>;
		isValueInDatalist: boolean;
	}) => ReactNode;
}

export default function ResourceText({
	prefix,
	element,
	hidden,
	srcType: _srcType,
	resourceFiles,
	onValueChange,
	children,
}: Props) {
	const [path, setPath] = useState(element.value);
	const isValueInDatalist = resourceFiles.includes(path);
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
				<div className="flex-1">
					<ControllTextBox
						name={fieldName}
						id={id}
						list={`${id}_list`}
						hidden={hidden}
						required={element.attribute.required}
						value={path}
						handleChange={handleChange}
					/>
					{!hidden && (
						<ResourceDatalist
							id={id}
							resources={resourceFiles}
						/>
					)}
				</div>
				{!hidden && children({ path, setPath, isValueInDatalist })}
			</div>
		</div>
	);
}
