import type { TemplateOption } from "../../../model/CommandParam";
import ResourceText from "./ResourceText";
import TemplateText from "./TemplateText";

export default function TemplateFormSection({
	commandParams,
}: {
	commandParams: TemplateOption;
	name: string;
}) {
	return (
		<>
			<ResourceText prefix={commandParams.prefix} element={commandParams.encoding} hidden={false} />
			<TemplateText prefix={commandParams.prefix} element={commandParams.templateGroup} hidden={false} />
			<ResourceText prefix={commandParams.prefix} element={commandParams.templateParameterAttribute} hidden={false} />
			<ResourceText prefix={commandParams.prefix} element={commandParams.templateVarStart} hidden={false} />
			<ResourceText prefix={commandParams.prefix} element={commandParams.templateVarStop} hidden={false} />
		</>
	);
}
