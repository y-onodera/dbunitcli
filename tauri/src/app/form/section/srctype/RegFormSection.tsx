import type {
	CommandOption,
	RegOptions,
} from "../../../../model/CommandOption";
import PlainText from "../element/PlainText";

export default function RegFormSection({
	options: settings,
	handleValueChange,
}: {
	options: RegOptions;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
}) {
	const prefix = settings.prefix;

	return (
		<>
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
