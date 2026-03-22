import type { CommandParams } from "../../../model/CommandParam";
import CommandFormElements from "./CommandFormElement";

export default function TemplateFormSection({
	commandParams,
	handleTypeSelect,
	name,
}: {
	commandParams: CommandParams;
	handleTypeSelect: (selected: string) => Promise<void>;
	name: string;
}) {
	return (
		<CommandFormElements
			handleTypeSelect={handleTypeSelect}
			name={name}
			prefix={commandParams.prefix}
			elements={commandParams.elements}
			optionCaption={commandParams.optionCaption}
			optional={commandParams.optional}
		/>
	);
}
