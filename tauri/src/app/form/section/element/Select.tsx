import { InputLabel, SelectBox } from "../../../../components/element/Input";
import type { SelectProp } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

export default function Select(prop: SelectProp) {
	const id = getId(prop.prefix, prop.element.name);
	const name = getName(prop.prefix, prop.element.name);
	return (
		<div>
			<InputLabel
				text={name}
				id={id}
				required={prop.element.attribute.required}
				hidden={prop.hidden}
			/>
			<SelectBox
				name={name}
				id={id}
				required={prop.element.attribute.required}
				hidden={prop.hidden}
				handleOnChange={prop.handleTypeSelect}
				defaultValue={prop.element.value}
			>
				{prop.element.attribute.selectOption.map((value) => (
					<option key={prop.prefix + prop.element.name + value} value={value}>
						{value}
					</option>
				))}
			</SelectBox>
		</div>
	);
}
