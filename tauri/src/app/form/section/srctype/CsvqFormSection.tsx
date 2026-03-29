import { useState } from "react";
import { ExpandButton } from "../../../../components/element/ButtonIcon";
import type {
	CommandParam,
	CsvqTypeSettings,
} from "../../../../model/CommandParam";
import Check from "../element/Check";
import ResourceText from "../element/ResourceText";
import TemplateText from "../element/TemplateText";

export default function CsvqFormSection({
	settings,
	handleValueChange,
	handleToggleChecked,
}: {
	settings: CsvqTypeSettings;
	handleValueChange: (param: CommandParam) => (newValue: string) => void;
	handleToggleChecked: (param: CommandParam) => (checked: boolean) => void;
}) {
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);
	const prefix = settings.prefix;

	return (
		<>
			<div className="pt-2.5">
				<ExpandButton
					toggleOptional={toggleOptional}
					showOptional={showOptional}
					caption="csvq option"
				/>
			</div>
			<ResourceText
				prefix={prefix}
				element={settings.headerName}
				handleValueChange={handleValueChange(settings.headerName)}
				hidden={!showOptional}
			/>
			<Check
				prefix={prefix}
				element={settings.addFileInfo}
				handleOnChange={handleToggleChecked(settings.addFileInfo)}
				hidden={!showOptional}
			/>
			<TemplateText
				prefix={prefix}
				element={settings.templateGroup}
				hidden={!showOptional}
			/>
			<ResourceText
				prefix={prefix}
				element={settings.templateParameterAttribute}
				handleValueChange={handleValueChange(
					settings.templateParameterAttribute,
				)}
				hidden={!showOptional}
			/>
			<ResourceText
				prefix={prefix}
				element={settings.templateVarStart}
				handleValueChange={handleValueChange(settings.templateVarStart)}
				hidden={!showOptional}
			/>
			<ResourceText
				prefix={prefix}
				element={settings.templateVarStop}
				handleValueChange={handleValueChange(settings.templateVarStop)}
				hidden={!showOptional}
			/>
		</>
	);
}
