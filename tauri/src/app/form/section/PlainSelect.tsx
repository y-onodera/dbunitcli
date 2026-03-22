import { InputLabel, SelectBox } from "../../../components/element/Input";
import type { SelectProp } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

export default function PlainSelect(prop: SelectProp) {
	return (
		<div>
			<InputLabel
				text={getName(prop.prefix, prop.element.name)}
				id={getId(prop.prefix, prop.element.name)}
				required={prop.element.attribute.required}
				hidden={prop.hidden}
			/>
			<SelectBox
				name={getName(prop.prefix, prop.element.name)}
				id={getId(prop.prefix, prop.element.name)}
				required={true}
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
