import { useState } from "react";
import { ExpandButton } from "../../../../components/element/ButtonIcon";
import type {
	CommandParam,
	XlsTypeSettings,
} from "../../../../model/CommandParam";
import Check from "../element/Check";
import ResourceText from "../element/ResourceText";
import XlsxSchemaText from "./XlsxSchemaText";

export default function XlsFormSection({
	srcType,
	settings,
	handleValueChange,
	handleToggleChecked,
}: {
	srcType: string;
	settings: XlsTypeSettings;
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
					caption={`${srcType} option`}
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
