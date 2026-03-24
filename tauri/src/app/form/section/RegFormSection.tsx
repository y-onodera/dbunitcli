import { useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { RegTypeSettings } from "../../../model/CommandParam";
import Check from "./Check";
import { DatasetPlainText } from "./DatasetTextFormElement";

export default function RegFormSection({
	settings,
}: {
	settings: RegTypeSettings;
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
					caption="reg option"
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
				element={settings.regDataSplit}
				hidden={false}
			/>
			<DatasetPlainText
				prefix={prefix}
				element={settings.regHeaderSplit}
				hidden={false}
			/>
		</>
	);
}
