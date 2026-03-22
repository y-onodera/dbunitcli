import type { ComponentType } from "react";
import type { CommandParams } from "../../../model/CommandParam";
import CommandFormElements from "./CommandFormElement";
import DatasetText from "./DatasetTextFormElement";
import type { Prop } from "./FormElementProp";

export default function DatasetCommandFormSection({
	commandParams,
	handleTypeSelect,
	name,
	textComponent,
}: {
	commandParams: CommandParams;
	handleTypeSelect: (selected: string) => Promise<void>;
	name: string;
	textComponent?: ComponentType<Prop>;
}) {
	return (
		<CommandFormElements
			handleTypeSelect={handleTypeSelect}
			prefix={commandParams.prefix}
			name={name}
			elements={commandParams.elements}
			optionCaption={commandParams.optionCaption}
			optional={commandParams.optional}
			textComponent={textComponent ?? DatasetText}
		/>
	);
}
