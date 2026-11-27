import { blankColumnPositions } from "../../../../model/HeaderName";
import { BlankColumnFillButton } from "../dialog/BlankColumnFillDialog";
import type { TextProp } from "./FormElementProp";
import { getName } from "./FormElementProp";
import Text from "./Text";

function blankColumnMessages(value: string): string[] {
	const positions = blankColumnPositions(value);
	return positions.length > 0
		? [`blank column name at: ${positions.join(", ")}`]
		: [];
}

export default function HeaderNameText({
	prefix,
	element,
	hidden,
	handleValueChange,
}: TextProp) {
	const fieldName = getName(prefix, element.name);
	return (
		<Text
			prefix={prefix}
			element={element}
			hidden={hidden}
			handleValueChange={handleValueChange}
			validate={blankColumnMessages}
		>
			{({ path, setPath }) => (
				<BlankColumnFillButton
					fieldName={fieldName}
					value={path}
					onApply={setPath}
				/>
			)}
		</Text>
	);
}
