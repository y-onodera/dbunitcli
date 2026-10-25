import { useState } from "react";
import {
	SectionFieldset,
	SectionOptionHeader,
} from "../../../components/dialog";
import type {
	CommandOption,
	TemplateOption,
} from "../../../model/CommandOption";
import EncodingText from "./element/EncodingText";
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
		<SectionFieldset>
			<legend>template</legend>
			<SectionOptionHeader
				showOptional={showOptional}
				toggleOptional={toggleOptional}
				caption="template option"
				command="sections/template"
				label="Template"
			/>
			{templateOption.encoding && showEncoding && (
				<EncodingText
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
		</SectionFieldset>
	);
}
