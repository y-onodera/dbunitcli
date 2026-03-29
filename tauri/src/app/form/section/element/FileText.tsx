import type { TextProp } from "./FormElementProp";
import ResourceDropDownMenu from "./ResourceDropDownMenu";
import ResourceText from "./ResourceText";

export default function FileText({
	prefix,
	element,
	hidden,
	srcType,
	handleValueChange: onValueChange,
}: TextProp) {
	return (
		<ResourceText
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={[]}
			handleValueChange={onValueChange}
		>
			{({ path, setPath }) => (
				<ResourceDropDownMenu
					path={path}
					setPath={setPath}
					prefix={prefix}
					element={element}
					srcType={srcType}
				/>
			)}
		</ResourceText>
	);
}
