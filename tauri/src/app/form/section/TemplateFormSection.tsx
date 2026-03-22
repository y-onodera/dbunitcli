import type { CommandParam, TemplateOption } from "../../../model/CommandParam";
import Check from "./CheckFormElement";
import type { SelectProp } from "./FormElementProp";
import Select from "./SelectFormElement";
import TemplateText from "./TemplateText";

function renderElement(
	element: CommandParam,
	prefix: string,
	handleTypeSelect: SelectProp["handleTypeSelect"],
): React.ReactNode {
	if (element.attribute.type === "FLG") {
		return <Check prefix={prefix} element={element} hidden={false} />;
	}
	if (element.attribute.type === "ENUM") {
		return (
			<Select
				handleTypeSelect={handleTypeSelect}
				prefix={prefix}
				element={element}
				hidden={false}
			/>
		);
	}
	return <TemplateText prefix={prefix} element={element} hidden={false} />;
}

export default function TemplateFormSection({
	commandParams,
	handleTypeSelect,
}: {
	commandParams: TemplateOption;
	handleTypeSelect: SelectProp["handleTypeSelect"];
	name: string;
}) {
	return (
		<>
			{renderElement(commandParams.encoding, commandParams.prefix, handleTypeSelect)}
			{renderElement(commandParams.templateGroup, commandParams.prefix, handleTypeSelect)}
			{renderElement(commandParams.templateParameterAttribute, commandParams.prefix, handleTypeSelect)}
			{renderElement(commandParams.templateVarStart, commandParams.prefix, handleTypeSelect)}
			{renderElement(commandParams.templateVarStop, commandParams.prefix, handleTypeSelect)}
		</>
	);
}
