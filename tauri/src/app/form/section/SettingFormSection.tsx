import type { CommandOption, SrcElements } from "../../../model/CommandOption";
import Check from "./element/Check";
import DatasetSettingText from "./element/DatasetSettingText";
import EncodingText from "./element/EncodingText";

export default function SettingFormSection({
	settingElements,
	handleValueChange,
	handleToggleChecked,
}: {
	settingElements: SrcElements;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
	handleToggleChecked: (param: CommandOption) => (checked: boolean) => void;
}) {
	const prefix = settingElements.prefix;

	return (
		<>
			<DatasetSettingText
				prefix={prefix}
				element={settingElements.setting}
				hidden={false}
				handleValueChange={handleValueChange(settingElements.setting)}
			/>
			<EncodingText
				prefix={prefix}
				element={settingElements.settingEncoding}
				handleValueChange={handleValueChange(settingElements.settingEncoding)}
				hidden={false}
			/>
			<Check
				prefix={prefix}
				element={settingElements.settingTargetOnly}
				handleOnChange={handleToggleChecked(settingElements.settingTargetOnly)}
				hidden={false}
			/>
		</>
	);
}
