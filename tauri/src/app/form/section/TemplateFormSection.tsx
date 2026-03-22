import type { CommandParam } from "../../../model/CommandParam";
import CommandFormElements from "./CommandFormElement";

export default function TemplateFormSection({
	handleTypeSelect,
	name,
	prefix,
	elements,
}: {
	handleTypeSelect: (selected: string) => Promise<void>;
	name: string;
	prefix: string;
	elements: CommandParam[];
}) {
	return (
		<CommandFormElements
			handleTypeSelect={handleTypeSelect}
			name={name}
			prefix={prefix}
			elements={elements}
		/>
	);
}
