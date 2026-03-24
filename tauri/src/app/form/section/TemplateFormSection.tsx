import type { TemplateOption } from "../../../model/CommandParam";
import ResourceText from "./ResourceText";
import TemplateText from "./TemplateText";

export default function TemplateFormSection({
	templateOption,
}: {
	templateOption: TemplateOption;
}) {
	return (
		<>
			<ResourceText
				prefix={templateOption.prefix}
				element={templateOption.encoding}
				hidden={false}
			/>
			<TemplateText
				prefix={templateOption.prefix}
				element={templateOption.templateGroup}
				hidden={false}
			/>
			<ResourceText
				prefix={templateOption.prefix}
				element={templateOption.templateParameterAttribute}
				hidden={false}
			/>
			<ResourceText
				prefix={templateOption.prefix}
				element={templateOption.templateVarStart}
				hidden={false}
			/>
			<ResourceText
				prefix={templateOption.prefix}
				element={templateOption.templateVarStop}
				hidden={false}
			/>
		</>
	);
}
