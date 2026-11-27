import { blankColumnPositions } from "../../../../model/HeaderName";
import { BlankColumnFillButton } from "../dialog/BlankColumnFillDialog";
import FieldErrorMessages from "./FieldErrorMessages";
import type { TextProp } from "./FormElementProp";
import { getName } from "./FormElementProp";
import Text from "./Text";

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
			afterContent={({ path }) => (
				<BlankColumnMessage value={path} hidden={hidden} />
			)}
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

function BlankColumnMessage(props: { value: string; hidden?: boolean }) {
	const positions = blankColumnPositions(props.value);
	return (
		<FieldErrorMessages
			messages={
				positions.length > 0
					? [`blank column name at: ${positions.join(", ")}`]
					: []
			}
			hidden={props.hidden}
		/>
	);
}
