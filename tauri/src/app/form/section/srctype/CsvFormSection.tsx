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
	const prefix = settings.prefix;

	return (
		<>
			<PlainText
				prefix={prefix}
				element={settings.delimiter}
				handleValueChange={handleValueChange(settings.delimiter)}
				hidden={false}
			/>
			<Check
				prefix={prefix}
				element={settings.ignoreQuoted}
				handleOnChange={handleToggleChecked(settings.ignoreQuoted)}
				hidden={false}
			/>
		</>
	);
}
