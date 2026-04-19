import type {
	CommandOption,
	FixedOptions,
} from "../../../../model/CommandOption";
import PlainText from "../element/PlainText";

export default function FixedFormSection({
	options: settings,
	handleValueChange,
}: {
	options: FixedOptions;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
}) {
	const prefix = settings.prefix;

	return (
		<PlainText
			prefix={prefix}
			element={settings.fixedLength}
			handleValueChange={handleValueChange(settings.fixedLength)}
			hidden={false}
		/>
	);
}
