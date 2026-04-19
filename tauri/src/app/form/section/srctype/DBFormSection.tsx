import type {
	CommandOption,
	SqlOptions,
	TableOptions,
} from "../../../../model/CommandOption";
import Check from "../element/Check";

export default function DBFormSection({
	options: settings,
	handleToggleChecked,
}: {
	options: SqlOptions | TableOptions;
	handleToggleChecked: (param: CommandOption) => (checked: boolean) => void;
}) {
	const prefix = settings.prefix;

	return (
		<Check
			prefix={prefix}
			element={settings.useJdbcMetaData}
			handleOnChange={handleToggleChecked(settings.useJdbcMetaData)}
			hidden={false}
		/>
	);
}
