import { useState } from "react";
import { SectionFieldset, SectionHelpButton } from "../../../components/dialog";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { CommandOption, SrcElements } from "../../../model/CommandOption";
import Check from "./element/Check";
import PlainText from "./element/PlainText";

export default function TraversalFormSection({
	srcElements,
	handleValueChange,
	handleToggleChecked,
}: {
	srcElements: SrcElements;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
	handleToggleChecked: (param: CommandOption) => (checked: boolean) => void;
}) {
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);
	const prefix = srcElements.prefix;
	return (
		<SectionFieldset>
			<legend>traversal</legend>
			<div className="flex items-center gap-2">
				<ExpandButton
					toggleOptional={toggleOptional}
					showOptional={showOptional}
					caption="traversal option"
				/>
				<SectionHelpButton command="traversal" label="Traversal" />
			</div>
			{srcElements.extension && (
				<PlainText
					prefix={prefix}
					element={srcElements.extension}
					handleValueChange={handleValueChange(srcElements.extension)}
					hidden={!showOptional}
				/>
			)}
			<Check
				prefix={prefix}
				element={srcElements.recursive}
				handleOnChange={handleToggleChecked(srcElements.recursive)}
				hidden={!showOptional}
			/>
			<PlainText
				prefix={prefix}
				element={srcElements.regInclude}
				handleValueChange={handleValueChange(srcElements.regInclude)}
				hidden={!showOptional}
			/>
			<PlainText
				prefix={prefix}
				element={srcElements.regExclude}
				handleValueChange={handleValueChange(srcElements.regExclude)}
				hidden={!showOptional}
			/>
		</SectionFieldset>
	);
}
