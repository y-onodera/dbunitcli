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
	const useJdbcMetaData = settings.useJdbcMetaData;

	return (
		useJdbcMetaData && (
			<Check
				prefix={prefix}
				element={useJdbcMetaData}
				handleOnChange={handleToggleChecked(useJdbcMetaData)}
				hidden={false}
			/>
		)
	);
}
