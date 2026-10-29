import { InputLabel, SelectBox } from "../../../../components/element/Input";
import { useFieldErrorTarget } from "../../../../context/FieldErrorProvider";
import FieldErrorMessages from "./FieldErrorMessages";
import type { SelectProp } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

export default function Select(prop: SelectProp) {
	const id = getId(prop.prefix, prop.element.name);
	const name = getName(prop.prefix, prop.element.name);
	const { fieldErrors, clearFieldErrors } = useFieldErrorTarget(
		name,
		!prop.hidden,
	);
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
				handleOnChange={(selected) => {
					// 入力し直したら、この欄の検証エラーは消す
					clearFieldErrors();
					return prop.handleTypeSelect(selected);
				}}
				defaultValue={prop.element.value}
			>
				{prop.element.attribute.selectOption.map((value) => (
					<option key={prop.prefix + prop.element.name + value} value={value}>
						{value}
					</option>
				))}
			</SelectBox>
			<FieldErrorMessages messages={fieldErrors} hidden={prop.hidden} />
		</div>
	);
}
