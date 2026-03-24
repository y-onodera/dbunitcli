import { useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { CsvTypeSettings } from "../../../model/CommandParam";
import Check from "./Check";
import { DatasetPlainText } from "./DatasetTextFormElement";

export default function CsvFormSection({
	settings,
}: {
	settings: CsvTypeSettings;
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
					caption="csv option"
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
			<DatasetPlainText
				prefix={prefix}
				element={settings.delimiter}
				hidden={!showOptional}
			/>
			<Check
				prefix={prefix}
				element={settings.ignoreQuoted}
				hidden={!showOptional}
			/>
		</>
	);
}
