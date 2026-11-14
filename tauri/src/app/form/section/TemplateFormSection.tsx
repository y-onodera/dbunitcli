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

// optionの入力中の値と、変更されたのがこのoptionなら値を追うtrack
function useTrackedValue(option: CommandOption | undefined) {
	const [value, setValue] = useSyncedState(option?.value ?? "");
	const track = (param: CommandOption, newValue: string) => {
		if (param === option) {
			setValue(newValue);
		}
	};
	return { value, track };
}

// 空欄はcore側でデフォルトになるため、プレースホルダーのデフォルトに置き換える
function effectiveValue(
	tracked: { value: string },
	option: CommandOption | undefined,
	fallback: string,
) {
	if (tracked.value !== "") {
		return tracked.value;
	}
	return option?.attribute.defaultValue || fallback;
}

// template optionの入力中の値と、それを追うhandleValueChange。
// encodingはResourceEncodingProviderで、template optionを適用するリソースの文字コードにする。
// syntaxはテンプレートとして描画される欄（Generateの-resultPath等）に挿入する式の組み立てに使う
export function useTemplateOptionValues(templateOption?: TemplateOption) {
	const encoding = useTrackedValue(templateOption?.encoding);
	const parameterAttribute = useTrackedValue(
		templateOption?.templateParameterAttribute,
	);
	const varStart = useTrackedValue(templateOption?.templateVarStart);
	const varStop = useTrackedValue(templateOption?.templateVarStop);
	const handleValueChange = (param: CommandOption) => (value: string) => {
		for (const tracked of [encoding, parameterAttribute, varStart, varStop]) {
			tracked.track(param, value);
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
	return { encoding: encoding.value, syntax, handleValueChange };
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
