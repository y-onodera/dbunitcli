import type { TextProp } from "./FormElementProp";
import Text, { TextDropDownMenu } from "./Text";

export default function FileText({
	prefix,
	element,
	hidden,
	handleValueChange: onValueChange,
}: TextProp) {
	return (
		<Text
			prefix={prefix}
			element={element}
			hidden={hidden}
			showDefaulePath={true}
			handleValueChange={onValueChange}
		>
			{({ path, setPath }) => (
				<TextDropDownMenu
					path={path}
					setPath={setPath}
					prefix={prefix}
					element={element}
				/>
			)}
		</Text>
	);
}
