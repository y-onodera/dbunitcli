import { Fragment, useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { CommandParam } from "../../../model/CommandParam";
import Check from "./CheckFormElement";
import DatasetText from "./DatasetTextFormElement";
import type { SelectProp } from "./FormElementProp";
import SrcTypeSelect from "./SrcTypeSelect";

const TRAVERSAL_OPTIONAL = [
	"recursive",
	"regInclude",
	"regExclude",
	"extension",
] as const;

function isTraversalOptional(name: string): boolean {
	return (TRAVERSAL_OPTIONAL as readonly string[]).includes(name);
}

function renderSrcElement(
	element: CommandParam,
	prefix: string,
	srcType: string,
	hidden: boolean,
	handleTypeSelect: SelectProp["handleTypeSelect"],
): React.ReactNode {
	if (element.attribute.type === "ENUM") {
		return (
			<SrcTypeSelect
				handleTypeSelect={handleTypeSelect}
				prefix={prefix}
				element={element}
				hidden={hidden}
			/>
		);
	}
	if (element.attribute.type === "FLG") {
		return <Check prefix={prefix} element={element} hidden={hidden} />;
	}
	return (
		<DatasetText
			prefix={prefix}
			element={element}
			hidden={hidden}
			srcType={element.name === "src" ? srcType : undefined}
		/>
	);
}

export default function SrcFormSection({
	prefix,
	name,
	elements,
	handleTypeSelect,
}: {
	prefix: string;
	name: string;
	elements: CommandParam[];
	handleTypeSelect: SelectProp["handleTypeSelect"];
}) {
	const [showOptional, setShowOptional] = useState(false);
	const srcType = elements.find((e) => e.name === "srcType")?.value ?? "";
	const firstOptionalName = elements.find((e) =>
		isTraversalOptional(e.name),
	)?.name;
	const toggleOptional = () => setShowOptional(!showOptional);

	return (
		<>
			{elements.map((element) => {
				const isOptional = isTraversalOptional(element.name);
				const showExpandButton = element.name === firstOptionalName;
				return (
					<Fragment key={name + prefix + element.name}>
						{showExpandButton && (
							<div className="pt-2.5">
								<ExpandButton
									toggleOptional={toggleOptional}
									showOptional={showOptional}
									caption="traversal option"
								/>
							</div>
						)}
						{renderSrcElement(
							element,
							prefix,
							srcType,
							isOptional && !showOptional,
							handleTypeSelect,
						)}
					</Fragment>
				);
			})}
		</>
	);
}
