import { CheckBox, InputLabel } from "../../../../components/element/Input";
import { useFieldErrors } from "../../../../context/FieldErrorProvider";
import FieldErrorMessages from "./FieldErrorMessages";
import type { CheckProp } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

export default function Check(prop: CheckProp) {
	const name = getName(prop.prefix, prop.element.name);
	const id = getId(prop.prefix, prop.element.name);
	const fieldErrors = useFieldErrors(name, !prop.hidden);
	return (
		<div>
			<InputLabel text={name} id={id} required={false} hidden={prop.hidden} />
			<CheckBox
				name={name}
				id={id}
				hidden={prop.hidden}
				defaultValue={prop.element.value}
				handleOnChange={prop.handleOnChange}
			/>
			<FieldErrorMessages messages={fieldErrors} hidden={prop.hidden} />
		</div>
	);
}
