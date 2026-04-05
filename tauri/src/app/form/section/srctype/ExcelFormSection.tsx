import { useState } from "react";
import { ExpandButton } from "../../../../components/element/ButtonIcon";
import type {
	CommandParam,
	XlsTypeSettings,
	XlsxTypeSettings,
} from "../../../../model/CommandParam";
import Check from "../element/Check";
import ResourceText from "../element/ResourceText";
import XlsxSchemaText from "./XlsxSchemaText";

export default function ExcelFormSection({
	settings,
	handleValueChange,
	handleToggleChecked,
}: {
	settings: XlsTypeSettings | XlsxTypeSettings;
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
					caption={`${settings.srcType.value} option`}
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
			<XlsxSchemaText
				prefix={prefix}
				element={settings.xlsxSchema}
				handleValueChange={handleValueChange(settings.xlsxSchema)}
				hidden={!showOptional}
			/>
		</>
	);
}
