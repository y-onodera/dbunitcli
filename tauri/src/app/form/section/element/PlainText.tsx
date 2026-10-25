import type { TextProp } from "./FormElementProp";
import Text from "./Text";

export default function PlainText({
	prefix,
	element,
	hidden,
	handleValueChange: onValueChange,
	children,
}: TextProp) {
	return (
		<Text
			prefix={prefix}
			element={element}
			hidden={hidden}
			handleValueChange={onValueChange}
		>
			{children}
		</Text>
	);
}
