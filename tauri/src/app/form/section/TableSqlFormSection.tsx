import { useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { TableSqlTypeSettings } from "../../../model/CommandParam";
import Check from "./Check";
import { DatasetPlainText } from "./DatasetTextFormElement";
import TemplateText from "./TemplateText";

export default function TableSqlFormSection({
	settings,
}: {
	settings: TableSqlTypeSettings;
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
			<DatasetPlainText
				prefix={prefix}
				element={settings.headerName}
				hidden={!showOptional}
			/>
			<Check
				prefix={prefix}
				element={settings.addFileInfo}
				hidden={!showOptional}
			/>
			<Check
				prefix={prefix}
				element={settings.useJdbcMetaData}
				hidden={!showOptional}
			/>
			<TemplateText
				prefix={prefix}
				element={settings.templateGroup}
				hidden={!showOptional}
			/>
			<DatasetPlainText
				prefix={prefix}
				element={settings.templateParameterAttribute}
				hidden={!showOptional}
			/>
			<DatasetPlainText
				prefix={prefix}
				element={settings.templateVarStart}
				hidden={!showOptional}
			/>
			<DatasetPlainText
				prefix={prefix}
				element={settings.templateVarStop}
				hidden={!showOptional}
			/>
		</>
	);
}
