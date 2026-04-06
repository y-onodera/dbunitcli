import { useState } from "react";
import { ExpandButton } from "../../../../components/element/ButtonIcon";
import type {
	CommandOption,
	CsvqOptions,
} from "../../../../model/CommandParam";
import Check from "../element/Check";
import PlainText from "../element/PlainText";
import TemplateText from "../element/TemplateText";

export default function CsvqFormSection({
	options: settings,
	handleValueChange,
	handleToggleChecked,
}: {
	options: CsvqOptions;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
	handleToggleChecked: (param: CommandOption) => (checked: boolean) => void;
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
			<PlainText
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
			<PlainText
				prefix={prefix}
				element={settings.templateParameterAttribute}
				handleValueChange={handleValueChange(
					settings.templateParameterAttribute,
				)}
				hidden={!showOptional}
			/>
			<PlainText
				prefix={prefix}
				element={settings.templateVarStart}
				handleValueChange={handleValueChange(settings.templateVarStart)}
				hidden={!showOptional}
			/>
			<PlainText
				prefix={prefix}
				element={settings.templateVarStop}
				handleValueChange={handleValueChange(settings.templateVarStop)}
				hidden={!showOptional}
			/>
		</>
	);
}
