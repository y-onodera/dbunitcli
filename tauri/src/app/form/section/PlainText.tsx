import type { Prop } from "./FormElementProp";
import ResourceText from "./ResourceText";

interface Props extends Prop {
	onValueChange?: (value: string) => void;
}

export default function PlainText({ prefix, element, hidden, onValueChange }: Props) {
	return (
		<ResourceText
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={[]}
			onValueChange={onValueChange}
		/>
	);
}
