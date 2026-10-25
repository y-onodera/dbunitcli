import type { CommandOption, SrcElements } from "../../../model/CommandOption";
import DatasetSettingText from "./element/DatasetSettingText";
import EncodingText from "./element/EncodingText";

export default function SettingFormSection({
	settingElements,
	handleValueChange,
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
		</>
	);
}
