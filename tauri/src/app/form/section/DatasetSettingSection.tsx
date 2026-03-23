import { useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { SettingElements } from "../../../model/CommandParam";
import Check from "./Check";
import DatasetSettingText from "./DatasetSettingText";
import { DatasetPlainText } from "./DatasetTextFormElement";

export default function DatasetSettingSection({
	settingElements,
}: {
	settingElements: SettingElements;
}) {
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);
	const prefix = settingElements.prefix;

	return (
		<>
			<DatasetSettingText
				prefix={prefix}
				element={settingElements.setting}
				hidden={false}
			/>
			<DatasetPlainText
				prefix={prefix}
				element={settingElements.settingEncoding}
				hidden={false}
			/>
			<div className="pt-2.5">
				<ExpandButton
					toggleOptional={toggleOptional}
					showOptional={showOptional}
					caption="dataset option"
				/>
			</div>
			<DatasetPlainText
				prefix={prefix}
				element={settingElements.regTableInclude}
				hidden={!showOptional}
			/>
			<DatasetPlainText
				prefix={prefix}
				element={settingElements.regTableExclude}
				hidden={!showOptional}
			/>
			<Check
				prefix={prefix}
				element={settingElements.loadData}
				hidden={!showOptional}
			/>
			<Check
				prefix={prefix}
				element={settingElements.includeMetaData}
				hidden={!showOptional}
			/>
		</>
	);
}
