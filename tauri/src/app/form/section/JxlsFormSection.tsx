import { useState } from "react";
import { SectionHelpButton } from "../../../components/dialog";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { TemplateOption } from "../../../model/CommandOption";
import Check from "./element/Check";
import PlainText from "./element/PlainText";

export default function JxlsFormSection({
	templateOption,
	showFormulaProcess,
}: {
	templateOption: TemplateOption;
	showFormulaProcess: boolean;
}) {
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);
	return (
		<fieldset className="border border-border-subtle p-3">
			<legend>jxls</legend>
			<div className="flex items-center gap-2">
				<ExpandButton
					toggleOptional={toggleOptional}
					showOptional={showOptional}
					caption="jxls option"
				/>
				<SectionHelpButton command="generate" label="Jxls" />
			</div>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.templateParameterAttribute}
				hidden={!showOptional}
			/>
			{showFormulaProcess && templateOption.formulaProcess && (
				<Check
					prefix={templateOption.prefix}
					element={templateOption.formulaProcess}
					hidden={!showOptional}
				/>
			)}
			{templateOption.evaluateFormulas && (
				<Check
					prefix={templateOption.prefix}
					element={templateOption.evaluateFormulas}
					hidden={!showOptional}
				/>
			)}
			{templateOption.forceFormulaRecalc && (
				<Check
					prefix={templateOption.prefix}
					element={templateOption.forceFormulaRecalc}
					hidden={!showOptional}
				/>
			)}
			{templateOption.fastFormulaProcess && (
				<Check
					prefix={templateOption.prefix}
					element={templateOption.fastFormulaProcess}
					hidden={!showOptional}
				/>
			)}
			{templateOption.deleteBlankCells && (
				<Check
					prefix={templateOption.prefix}
					element={templateOption.deleteBlankCells}
					hidden={!showOptional}
				/>
			)}
		</fieldset>
	);
}
