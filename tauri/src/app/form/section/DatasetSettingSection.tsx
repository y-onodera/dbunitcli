import { useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type {
	CommandParam,
	SettingElements,
} from "../../../model/CommandParam";
import Check from "./element/Check";
import DatasetSettingText from "./element/DatasetSettingText";
import PlainText from "./element/PlainText";
import ResourceText from "./element/ResourceText";

export default function DatasetSettingSection({
	settingElements,
	handleValueChange,
	handleToggleChecked,
}: {
	settingElements: SettingElements;
	handleValueChange: (param: CommandParam) => (newValue: string) => void;
	handleToggleChecked: (param: CommandParam) => (checked: boolean) => void;
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
			<PlainText
				prefix={prefix}
				element={settingElements.settingEncoding}
				handleValueChange={handleValueChange(settingElements.settingEncoding)}
				hidden={false}
			/>
			<div className="pt-2.5">
				<ExpandButton
					toggleOptional={toggleOptional}
					showOptional={showOptional}
					caption="dataset option"
				/>
			</div>
			<PlainText
				prefix={prefix}
				element={settingElements.regTableInclude}
				handleValueChange={handleValueChange(settingElements.regTableInclude)}
				hidden={!showOptional}
			/>
			<PlainText
				prefix={prefix}
				element={settingElements.regTableExclude}
				handleValueChange={handleValueChange(settingElements.regTableExclude)}
				hidden={!showOptional}
			/>
			{settingElements.loadData && (
				<Check
					prefix={prefix}
					element={settingElements.loadData}
					handleOnChange={handleToggleChecked(settingElements.loadData)}
					hidden={!showOptional}
				/>
			)}
			{settingElements.includeMetaData && (
				<Check
					prefix={prefix}
					element={settingElements.includeMetaData}
					handleOnChange={handleToggleChecked(settingElements.includeMetaData)}
					hidden={!showOptional}
				/>
			)}
		</>
	);
}
