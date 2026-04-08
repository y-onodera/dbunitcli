import type { TextProp } from "./FormElementProp";
import PlainText from "./PlainText";
import ResourceDropDownMenu from "./ResourceDropDownMenu";

export default function FileText({
	prefix,
	element,
	hidden,
	handleValueChange: onValueChange,
}: TextProp) {
	return (
		<PlainText
			prefix={prefix}
			element={element}
			hidden={hidden}
			handleValueChange={onValueChange}
		>
			{({ value, setValue }) => (
				<ResourceDropDownMenu
					path={value}
					setPath={setValue}
					prefix={prefix}
					element={element}
				/>
			)}
		</PlainText>
	);
}
