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
				const hidden = isOptional && !showOptional;
				let elementNode: React.ReactNode;
				if (element.attribute.type === "ENUM") {
					elementNode = (
						<SrcTypeSelect
							handleTypeSelect={handleTypeSelect}
							prefix={prefix}
							element={element}
							hidden={hidden}
						/>
					);
				} else if (element.attribute.type === "FLG") {
					elementNode = <Check prefix={prefix} element={element} hidden={hidden} />;
				} else {
					elementNode = (
						<DatasetText
							prefix={prefix}
							element={element}
							hidden={hidden}
							srcType={element.name === "src" ? srcType : undefined}
						/>
					);
				}
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
						{elementNode}
					</Fragment>
				);
			})}
		</>
	);
}
