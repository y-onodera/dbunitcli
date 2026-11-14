import { useState } from "react";
import {
	SectionFieldset,
	SectionOptionHeader,
} from "../../../components/dialog";
import type {
	CommandOption,
	TemplateOption,
} from "../../../model/CommandOption";
import {
	DEFAULT_TEMPLATE_EXPRESSION_SYNTAX,
	type TemplateExpressionSyntax,
} from "../../../model/TemplateExpression";
import { useSyncedState } from "../../../utils/useResettableState";
import EncodingText from "./element/EncodingText";
import PlainText from "./element/PlainText";
import TemplateText from "./element/TemplateText";

// template optionのencodingの入力中の値と、それを追うTemplateFormSectionのhandleValueChange。
// ResourceEncodingProviderで、template optionを適用するリソースの文字コードにする
export function useTemplateEncoding(templateOption?: TemplateOption) {
	const [encoding, setEncoding] = useSyncedState(
		templateOption?.encoding?.value ?? "",
	);
	const handleValueChange = (param: CommandOption) => (value: string) => {
		if (param === templateOption?.encoding) {
			setEncoding(value);
		}
	};
	return { encoding, handleValueChange };
}

// 空欄はcore側で既定値になるため、プレースホルダーの既定値に置き換える
function effectiveValue(
	value: string,
	option: CommandOption | undefined,
	fallback: string,
) {
	if (value !== "") {
		return value;
	}
	return option?.attribute.defaultValue || fallback;
}

// template optionの式の書き方（templateParameterAttribute・templateVarStart/Stop）の入力中の値と、
// それを追うhandleValueChange。テンプレートとして描画される欄（-resultPath等）に挿入する式の組み立てに使う
export function useTemplateExpressionSyntax(templateOption?: TemplateOption) {
	const [parameterAttribute, setParameterAttribute] = useSyncedState(
		templateOption?.templateParameterAttribute?.value ?? "",
	);
	const [varStart, setVarStart] = useSyncedState(
		templateOption?.templateVarStart?.value ?? "",
	);
	const [varStop, setVarStop] = useSyncedState(
		templateOption?.templateVarStop?.value ?? "",
	);
	const handleValueChange = (param: CommandOption) => (value: string) => {
		if (param === templateOption?.templateParameterAttribute) {
			setParameterAttribute(value);
		} else if (param === templateOption?.templateVarStart) {
			setVarStart(value);
		} else if (param === templateOption?.templateVarStop) {
			setVarStop(value);
		}
	};
	const syntax: TemplateExpressionSyntax = {
		parameterAttribute: effectiveValue(
			parameterAttribute,
			templateOption?.templateParameterAttribute,
			DEFAULT_TEMPLATE_EXPRESSION_SYNTAX.parameterAttribute,
		),
		varStart: effectiveValue(
			varStart,
			templateOption?.templateVarStart,
			DEFAULT_TEMPLATE_EXPRESSION_SYNTAX.varStart,
		),
		varStop: effectiveValue(
			varStop,
			templateOption?.templateVarStop,
			DEFAULT_TEMPLATE_EXPRESSION_SYNTAX.varStop,
		),
	};
	return { syntax, handleValueChange };
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
