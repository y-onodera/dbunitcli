import { useState } from "react";
import {
	SectionFieldset,
	SectionOptionHeader,
} from "../../../components/dialog";
import type {
	CommandOption,
	TemplateOption,
} from "../../../model/CommandOption";
import EncodingText from "./element/EncodingText";
import PlainText from "./element/PlainText";
import TemplateText from "./element/TemplateText";

// template optionのencodingの入力中の値と、それを追うTemplateFormSectionのhandleValueChange。
// templateGroup・template・SQLスクリプトの編集ダイアログが読み書きする文字コードに使う
export function useTemplateEncoding(templateOption?: TemplateOption) {
	const [encoding, setEncoding] = useState(
		templateOption?.encoding?.value ?? "",
	);
	const handleValueChange = (param: CommandOption) => (value: string) => {
		if (param === templateOption?.encoding) {
			setEncoding(value);
		}
	};
	return { encoding, handleValueChange };
}

export default function TemplateFormSection({
	templateOption,
	showEncoding,
	handleValueChange,
	encoding,
}: {
	templateOption: TemplateOption;
	showEncoding: boolean;
	// コマンド実行時にtemplateGroupを読む文字コード（入力中の値。編集ダイアログの読み書きに使う）
	encoding: string;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
}) {
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);
	return (
		<SectionFieldset>
			<legend>template</legend>
			<SectionOptionHeader
				showOptional={showOptional}
				toggleOptional={toggleOptional}
				caption="template option"
				command="sections/template"
				label="Template"
			/>
			{templateOption.encoding && showEncoding && (
				<EncodingText
					prefix={templateOption.prefix}
					element={templateOption.encoding}
					hidden={!showOptional}
					handleValueChange={handleValueChange(templateOption.encoding)}
				/>
			)}
			<TemplateText
				prefix={templateOption.prefix}
				element={templateOption.templateGroup}
				hidden={!showOptional}
				encoding={encoding}
				handleValueChange={handleValueChange(templateOption.templateGroup)}
			/>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.templateParameterAttribute}
				hidden={!showOptional}
				handleValueChange={handleValueChange(
					templateOption.templateParameterAttribute,
				)}
			/>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.templateVarStart}
				hidden={!showOptional}
				handleValueChange={handleValueChange(templateOption.templateVarStart)}
			/>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.templateVarStop}
				hidden={!showOptional}
				handleValueChange={handleValueChange(templateOption.templateVarStop)}
			/>
		</SectionFieldset>
	);
}
