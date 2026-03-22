import { useState } from "react";
import { ControllTextBox, InputLabel } from "../../../components/element/Input";
import type { Prop } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

interface Props extends Prop {
	onValueChange?: (value: string) => void;
}

export default function PlainText({
	prefix,
	element,
	hidden,
	onValueChange,
}: Props) {
	const [path, setPath] = useState(element.value);
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
				<div className="flex-1 mr-36">
					<ControllTextBox
						name={fieldName}
						id={id}
						hidden={hidden}
						required={element.attribute.required}
						value={path}
						handleChange={handleChange}
					/>
				</div>
			</div>
		</div>
	);
}
