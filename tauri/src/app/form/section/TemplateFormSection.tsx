import { Fragment, useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { CommandParam, CommandParams } from "../../../model/CommandParam";
import Check from "./CheckFormElement";
import type { SelectProp } from "./FormElementProp";
import Select from "./SelectFormElement";
import TemplateText from "./TemplateText";

function renderElement(
	element: CommandParam,
	prefix: string,
	hidden: boolean,
	handleTypeSelect: SelectProp["handleTypeSelect"],
): React.ReactNode {
	if (element.attribute.type === "FLG") {
		return <Check prefix={prefix} element={element} hidden={hidden} />;
	}
	if (element.attribute.type === "ENUM") {
		return (
			<Select
				handleTypeSelect={handleTypeSelect}
				prefix={prefix}
				element={element}
				hidden={hidden}
			/>
		);
	}
	return <TemplateText prefix={prefix} element={element} hidden={hidden} />;
}

export default function TemplateFormSection({
	commandParams,
	handleTypeSelect,
	name,
}: {
	commandParams: CommandParams;
	handleTypeSelect: SelectProp["handleTypeSelect"];
	name: string;
}) {
	const [showOptional, setShowOptional] = useState(false);
	const firstOptionalName = commandParams.optionCaption
		? commandParams.elements.find((e) => commandParams.optional?.(e.name))?.name
		: undefined;
	const toggleOptional = () => setShowOptional(!showOptional);

	return (
		<>
			{commandParams.elements.map((element) => {
				const isOptional = commandParams.optional?.(element.name) ?? false;
				const showExpandButton = element.name === firstOptionalName;
				return (
					<Fragment key={name + commandParams.prefix + element.name}>
						{showExpandButton && (
							<div className="pt-2.5">
								<ExpandButton
									toggleOptional={toggleOptional}
									showOptional={showOptional}
									caption={commandParams.optionCaption?.caption}
								/>
							</div>
						)}
						{renderElement(
							element,
							commandParams.prefix,
							isOptional && !showOptional,
							handleTypeSelect,
						)}
					</Fragment>
				);
			})}
		</>
	);
}
