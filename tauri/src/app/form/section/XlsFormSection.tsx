import { useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { XlsTypeSettings } from "../../../model/CommandParam";
import Check from "./Check";
import { DatasetPlainText } from "./DatasetTextFormElement";
import XlsxSchemaText from "./XlsxSchemaText";

export default function XlsFormSection({
	settings,
}: {
	settings: XlsTypeSettings;
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
			<DatasetPlainText
				prefix={prefix}
				element={settings.startRow}
				hidden={!showOptional}
			/>
			<Check
				prefix={prefix}
				element={settings.addFileInfo}
				hidden={!showOptional}
			/>
			<XlsxSchemaText
				prefix={prefix}
				element={settings.xlsxSchema}
				hidden={!showOptional}
			/>
		</>
	);
}
