import { useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { SrcElements } from "../../../model/CommandParam";
import Check from "./CheckFormElement";
import DatasetText from "./DatasetTextFormElement";
import type { SelectProp } from "./FormElementProp";
import SrcTypeSelect from "./SrcTypeSelect";

export default function SrcFormSection({
	srcElements,
	handleTypeSelect,
}: {
	srcElements: SrcElements;
	handleTypeSelect: SelectProp["handleTypeSelect"];
}) {
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);
	const prefix = srcElements.prefix;
	const srcType = srcElements.srcType?.value ?? "";

	return (
		<>
			{srcElements.srcType && (
				<SrcTypeSelect
					handleTypeSelect={handleTypeSelect}
					prefix={prefix}
					element={srcElements.srcType}
					hidden={false}
				/>
			)}
			<DatasetText
				prefix={prefix}
				element={srcElements.src}
				hidden={false}
				srcType={srcType}
			/>
			{srcElements.encoding && (
				<DatasetText
					prefix={prefix}
					element={srcElements.encoding}
					hidden={false}
				/>
			)}
			{srcElements.recursive && (
				<>
					<div className="pt-2.5">
						<ExpandButton
							toggleOptional={toggleOptional}
							showOptional={showOptional}
							caption="traversal option"
						/>
					</div>
					<Check
						prefix={prefix}
						element={srcElements.recursive}
						hidden={!showOptional}
					/>
					{srcElements.regInclude && (
						<DatasetText
							prefix={prefix}
							element={srcElements.regInclude}
							hidden={!showOptional}
						/>
					)}
					{srcElements.regExclude && (
						<DatasetText
							prefix={prefix}
							element={srcElements.regExclude}
							hidden={!showOptional}
						/>
					)}
					{srcElements.extension && (
						<DatasetText
							prefix={prefix}
							element={srcElements.extension}
							hidden={!showOptional}
						/>
					)}
				</>
			)}
		</>
	);
}
