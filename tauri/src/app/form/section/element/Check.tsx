import { CheckBox, InputLabel } from "../../../../components/element/Input";
import type { CheckProp } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

export default function Check(prop: CheckProp) {
	return (
		<div>
			<InputLabel
				text={getName(prop.prefix, prop.element.name)}
				id={getId(prop.prefix, prop.element.name)}
				required={false}
				hidden={prop.hidden}
			/>
			<CheckBox
				name={getName(prop.prefix, prop.element.name)}
				id={getId(prop.prefix, prop.element.name)}
				hidden={prop.hidden}
				defaultValue={prop.element.value}
				handleOnChange={prop?.handleOnChange}
			/>
		</div>
	);
}
