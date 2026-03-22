import type { CommandParam } from "../../../model/CommandParam";
import ResourceText from "./ResourceText";

export default function JdbcTextField({
	prefix,
	element,
	onValueChange,
}: {
	prefix: string;
	element: CommandParam;
	onValueChange: (name: string, value: string) => void;
}) {
	return (
		<ResourceText
			prefix={prefix}
			element={element}
			resourceFiles={[]}
			onValueChange={(value) => onValueChange(element.name, value)}
		/>
	);
}
