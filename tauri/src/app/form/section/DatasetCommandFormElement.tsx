import { Fragment, useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { CommandParams } from "../../../model/CommandParam";
import Check from "./CheckFormElement";
import { isJdbcField } from "./JdbcFormSection";
import Select from "./SelectFormElement";
import DatasetText from "./DatasetTextFormElement";

export default function DatasetCommandFormElements(
	prop: {
		handleTypeSelect: (selected: string) => Promise<void>;
		hideDatasetSettingEdit?: boolean;
	} & CommandParams,
) {
	const [showOptional, setShowOptional] = useState(false);
	const srcTypeElement = prop.elements.find(
		(element) => element.name === "srcType",
	);
	const srcType = srcTypeElement ? srcTypeElement.value : "";
	const toggleOptional = () => setShowOptional(!showOptional);

	const firstOptionalNonJdbcElementName = prop.optionCaption
		? prop.elements.find((e) => !isJdbcField(e.name) && prop.optional?.(e.name))
				?.name
		: undefined;

	return (
		<>
			{prop.elements.map((element) => {
				const showExpandButton =
					element.name === firstOptionalNonJdbcElementName;
				if (element.attribute.type === "FLG") {
					return (
						<Fragment key={prop.name + prop.prefix + element.name}>
							{showExpandButton && (
								<div className="pt-2.5">
									<ExpandButton
										toggleOptional={toggleOptional}
										showOptional={showOptional}
										caption={prop.optionCaption?.caption}
									/>
								</div>
							)}
							<Check
								prefix={prop.prefix}
								element={element}
								hidden={prop.optional?.(element.name) && !showOptional}
							/>
						</Fragment>
					);
				}
				if (element.attribute.type === "ENUM") {
					return (
						<Fragment key={prop.name + prop.prefix + element.name}>
							{showExpandButton && (
								<div className="pt-2.5">
									<ExpandButton
										toggleOptional={toggleOptional}
										showOptional={showOptional}
										caption={prop.optionCaption?.caption}
									/>
								</div>
							)}
							<Select
								handleTypeSelect={prop.handleTypeSelect}
								prefix={prop.prefix}
								element={element}
								hidden={prop.optional?.(element.name) && !showOptional}
							/>
						</Fragment>
					);
				}
				return (
					<Fragment key={prop.name + prop.prefix + element.name}>
						{showExpandButton && (
							<div className="pt-2.5">
								<ExpandButton
									toggleOptional={toggleOptional}
									showOptional={showOptional}
									caption={prop.optionCaption?.caption}
								/>
							</div>
						)}
						<DatasetText
							prefix={prop.prefix}
							element={element}
							hidden={prop.optional?.(element.name) && !showOptional}
							srcType={element.name === "src" ? srcType : undefined}
							hideDatasetSettingEdit={prop.hideDatasetSettingEdit}
						/>
					</Fragment>
				);
			})}
		</>
	);
}
