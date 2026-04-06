import { useState } from "react";
import { ExpandButton } from "../../../../components/element/ButtonIcon";
import type {
	CommandOption,
	CsvOptions,
} from "../../../../model/CommandOption";
import Check from "../element/Check";
import PlainText from "../element/PlainText";

export default function CsvFormSection({
	options: settings,
	handleValueChange,
	handleToggleChecked,
}: {
	options: CsvOptions;
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
					caption="csv option"
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
				element={settings.delimiter}
				handleValueChange={handleValueChange(settings.delimiter)}
				hidden={!showOptional}
			/>
			<Check
				prefix={prefix}
				element={settings.ignoreQuoted}
				handleOnChange={handleToggleChecked(settings.ignoreQuoted)}
				hidden={!showOptional}
			/>
		</>
	);
}
