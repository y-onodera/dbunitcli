import { useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { CsvqTypeSettings } from "../../../model/CommandParam";
import Check from "./Check";
import { DatasetPlainText } from "./DatasetTextFormElement";
import TemplateText from "./TemplateText";

export default function CsvqFormSection({
	settings,
}: {
	settings: CsvqTypeSettings;
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
					caption="csvq option"
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
			<DatasetPlainText
				prefix={prefix}
				element={settings.encoding}
				hidden={false}
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
