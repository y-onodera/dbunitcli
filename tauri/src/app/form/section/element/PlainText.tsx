import type { Dispatch, ReactNode, SetStateAction } from "react";
import { useState } from "react";
import {
	ControllTextBox,
	InputLabel,
} from "../../../../components/element/Input";
import type { TextProp } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

interface Props extends TextProp {
	children?: (args: {
		value: string;
		setValue: Dispatch<SetStateAction<string>>;
	}) => ReactNode;
}

export default function PlainText({
	prefix,
	element,
	hidden,
	handleValueChange: onValueChange,
	children,
}: Props) {
	const [value, setValue] = useState(element.value);
	const id = getId(prefix, element.name);
	const fieldName = getName(prefix, element.name);

	const handleChange = (ev: React.ChangeEvent<HTMLInputElement>) => {
		const newValue = ev.target.value;
		setValue(newValue);
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
						hidden={hidden}
						required={element.attribute.required}
						value={value}
						handleChange={handleChange}
					/>
				</div>
				{!hidden && children && children({ value: value, setValue: setValue })}
			</div>
		</div>
	);
}
