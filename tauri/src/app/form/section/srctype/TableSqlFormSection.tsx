import { useState } from "react";
import { ExpandButton } from "../../../../components/element/ButtonIcon";
import type {
	CommandParam,
	TableSqlTypeSettings,
} from "../../../../model/CommandParam";
import Check from "../element/Check";
import ResourceText from "../element/ResourceText";
import TemplateText from "../element/TemplateText";

export default function TableSqlFormSection({
	settings,
	handleValueChange,
	handleToggleChecked,
}: {
	settings: TableSqlTypeSettings;
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
					caption={settings.optionCaption?.caption}
				/>
			</div>
			<ResourceText
				prefix={prefix}
				element={settings.headerName}
				handleValueChange={handleValueChange(settings.headerName)}
				hidden={!showOptional}
			/>
			<Check
				prefix={prefix}
				element={settings.addFileInfo}
				handleOnChange={handleToggleChecked(settings.addFileInfo)}
				hidden={!showOptional}
			/>
			<Check
				prefix={prefix}
				element={settings.useJdbcMetaData}
				handleOnChange={handleToggleChecked(settings.useJdbcMetaData)}
				hidden={!showOptional}
			/>
			<TemplateText
				prefix={prefix}
				element={settings.templateGroup}
				handleValueChange={handleValueChange(settings.templateGroup)}
				hidden={!showOptional}
			/>
			<ResourceText
				prefix={prefix}
				element={settings.templateParameterAttribute}
				handleValueChange={handleValueChange(
					settings.templateParameterAttribute,
				)}
				hidden={!showOptional}
			/>
			<ResourceText
				prefix={prefix}
				element={settings.templateVarStart}
				handleValueChange={handleValueChange(settings.templateVarStart)}
				hidden={!showOptional}
			/>
			<ResourceText
				prefix={prefix}
				element={settings.templateVarStop}
				handleValueChange={handleValueChange(settings.templateVarStop)}
				hidden={!showOptional}
			/>
		</>
	);
}
