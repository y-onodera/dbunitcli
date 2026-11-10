import { useState } from "react";
import {
	SectionFieldset,
	SectionOptionHeader,
} from "../../../components/dialog";
import type {
	CommandOption,
	TemplateOption,
} from "../../../model/CommandOption";
import { useResettableState } from "../../../utils/useResettableState";
import EncodingText from "./element/EncodingText";
import PlainText from "./element/PlainText";
import TemplateText from "./element/TemplateText";

// template optionのencodingの入力中の値と、それを追うTemplateFormSectionのhandleValueChange。
// ResourceEncodingProviderで、template optionを適用するリソースの文字コードにする。
// 入力欄と同じく要素の値が変わったら戻す
export function useTemplateEncoding(templateOption?: TemplateOption) {
	const encodingValue = templateOption?.encoding?.value ?? "";
	const [encoding, setEncoding] = useResettableState(
		encodingValue,
		encodingValue,
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
}: {
	templateOption: TemplateOption;
	showEncoding: boolean;
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
