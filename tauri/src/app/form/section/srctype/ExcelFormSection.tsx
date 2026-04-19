import type {
	CommandOption,
	XlsOptions,
	XlsxOptions,
} from "../../../../model/CommandOption";
import XlsxSchemaText from "../element/XlsxSchemaText";

export default function ExcelFormSection({
	options: settings,
	handleValueChange,
}: {
	options: XlsOptions | XlsxOptions;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
}) {
	const prefix = settings.prefix;

	return (
		<XlsxSchemaText
			prefix={prefix}
			element={settings.xlsxSchema}
			handleValueChange={handleValueChange(settings.xlsxSchema)}
			hidden={false}
		/>
	);
}
