import { useState } from "react";
import { ExpandButton } from "../../../../components/element/ButtonIcon";
import type {
	CommandParam,
	FixedTypeSettings,
} from "../../../../model/CommandParam";
import Check from "../element/Check";
import ResourceText from "../element/ResourceText";

export default function FixedFormSection({
	settings,
	handleValueChange,
	handleToggleChecked,
}: {
	settings: FixedTypeSettings;
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
					caption="fixed option"
				/>
			</div>
			<ResourceText
				prefix={prefix}
				element={settings.headerName}
				handleValueChange={handleValueChange(settings.headerName)}
				hidden={!showOptional}
			/>
			<ResourceText
				prefix={prefix}
				element={settings.startRow}
				handleValueChange={handleValueChange(settings.startRow)}
				hidden={!showOptional}
			/>
			<Check
				prefix={prefix}
				element={settings.addFileInfo}
				handleOnChange={handleToggleChecked(settings.addFileInfo)}
				hidden={!showOptional}
			/>
			<ResourceText
				prefix={prefix}
				element={settings.fixedLength}
				handleValueChange={handleValueChange(settings.fixedLength)}
				hidden={false}
			/>
		</>
	);
}
