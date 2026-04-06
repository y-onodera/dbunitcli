import { useState } from "react";
import { ExpandButton } from "../../../../components/element/ButtonIcon";
import type {
	CommandOption,
	RegOptions,
} from "../../../../model/CommandOption";
import Check from "../element/Check";
import PlainText from "../element/PlainText";

export default function RegFormSection({
	options: settings,
	handleValueChange,
	handleToggleChecked,
}: {
	options: RegOptions;
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
					caption="reg option"
				/>
			</div>
			<PlainText
				prefix={prefix}
				element={settings.headerName}
				handleValueChange={handleValueChange(settings.headerName)}
				hidden={!showOptional}
			/>
			<PlainText
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
			<PlainText
				prefix={prefix}
				element={settings.regDataSplit}
				handleValueChange={handleValueChange(settings.regDataSplit)}
				hidden={false}
			/>
			<PlainText
				prefix={prefix}
				element={settings.regHeaderSplit}
				handleValueChange={handleValueChange(settings.regHeaderSplit)}
				hidden={false}
			/>
		</>
	);
}
