import { Fragment, useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { CommandParam } from "../../../model/CommandParam";
import Check from "./CheckFormElement";
import DatasetText from "./DatasetTextFormElement";

const DATASET_OPTIONAL = [
	"regTableInclude",
	"regTableExclude",
	"loadData",
	"includeMetaData",
] as const;

function isDatasetOptional(name: string): boolean {
	return (DATASET_OPTIONAL as readonly string[]).includes(name);
}

function renderSettingElement(
	element: CommandParam,
	prefix: string,
	hidden: boolean,
): React.ReactNode {
	if (element.attribute.type === "FLG") {
		return <Check prefix={prefix} element={element} hidden={hidden} />;
	}
	return <DatasetText prefix={prefix} element={element} hidden={hidden} />;
}

export default function DatasetSettingSection({
	prefix,
	name,
	elements,
}: {
	prefix: string;
	name: string;
	elements: CommandParam[];
}) {
	const [showOptional, setShowOptional] = useState(false);
	const firstOptionalName = elements.find((e) =>
		isDatasetOptional(e.name),
	)?.name;
	const toggleOptional = () => setShowOptional(!showOptional);

	return (
		<>
			{elements.map((element) => {
				const isOptional = isDatasetOptional(element.name);
				const showExpandButton = element.name === firstOptionalName;
				return (
					<Fragment key={name + prefix + element.name}>
						{showExpandButton && (
							<div className="pt-2.5">
								<ExpandButton
									toggleOptional={toggleOptional}
									showOptional={showOptional}
									caption="dataset option"
								/>
							</div>
						)}
						{renderSettingElement(element, prefix, isOptional && !showOptional)}
					</Fragment>
				);
			})}
		</>
	);
}
