import { useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { TemplateOption } from "../../../model/CommandOption";
import PlainText from "./element/PlainText";
import TemplateText from "./element/TemplateText";

export default function TemplateFormSection({
	templateOption,
}: {
	templateOption: TemplateOption;
}) {
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);
	return (
		<fieldset className="border border-gray-200 p-3">
			<legend>template</legend>
			<ExpandButton
				toggleOptional={toggleOptional}
				showOptional={showOptional}
				caption="template option"
			/>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.encoding}
				hidden={!showOptional}
			/>
			<TemplateText
				prefix={templateOption.prefix}
				element={templateOption.templateGroup}
				hidden={!showOptional}
			/>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.templateParameterAttribute}
				hidden={!showOptional}
			/>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.templateVarStart}
				hidden={!showOptional}
			/>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.templateVarStop}
				hidden={!showOptional}
			/>
		</fieldset>
	);
}
