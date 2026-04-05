import type { TemplateOption } from "../../../model/CommandParam";
import PlainText from "./element/PlainText";
import ResourceText from "./element/ResourceText";
import TemplateText from "./element/TemplateText";

export default function TemplateFormSection({
	templateOption,
}: {
	templateOption: TemplateOption;
}) {
	return (
		<>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.encoding}
				hidden={false}
			/>
			<TemplateText
				prefix={templateOption.prefix}
				element={templateOption.templateGroup}
				hidden={false}
			/>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.templateParameterAttribute}
				hidden={false}
			/>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.templateVarStart}
				hidden={false}
			/>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.templateVarStop}
				hidden={false}
			/>
		</>
	);
}
