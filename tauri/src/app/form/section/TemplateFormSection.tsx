import { useState } from "react";
import { SectionHelpButton } from "../../../components/dialog";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type {
	CommandOption,
	TemplateOption,
} from "../../../model/CommandOption";
import PlainText from "./element/PlainText";
import TemplateText from "./element/TemplateText";

export default function TemplateFormSection({
	templateOption,
	showEncoding,
	handleValueChange,
}: {
	templateOption: TemplateOption;
	showEncoding: boolean;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
}) {
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);
	return (
		<fieldset className="border border-gray-200 p-3">
			<legend>template</legend>
			<div className="flex items-center gap-2">
				<ExpandButton
					toggleOptional={toggleOptional}
					showOptional={showOptional}
					caption="template option"
				/>
				<SectionHelpButton command="template" label="Template" />
			</div>
			{templateOption.encoding && showEncoding && (
				<PlainText
					prefix={templateOption.prefix}
					element={templateOption.encoding}
					hidden={!showOptional}
					handleValueChange={handleValueChange(templateOption.encoding)}
				/>
			)}
			<TemplateText
				prefix={templateOption.prefix}
				element={templateOption.templateGroup}
				hidden={!showOptional}
				handleValueChange={handleValueChange(templateOption.templateGroup)}
			/>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.templateParameterAttribute}
				hidden={!showOptional}
				handleValueChange={handleValueChange(
					templateOption.templateParameterAttribute,
				)}
			/>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.templateVarStart}
				hidden={!showOptional}
				handleValueChange={handleValueChange(templateOption.templateVarStart)}
			/>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.templateVarStop}
				hidden={!showOptional}
				handleValueChange={handleValueChange(templateOption.templateVarStop)}
			/>
		</fieldset>
	);
}
