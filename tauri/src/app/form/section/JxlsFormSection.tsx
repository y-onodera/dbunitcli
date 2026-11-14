import { useState } from "react";
import {
	SectionFieldset,
	SectionOptionHeader,
} from "../../../components/dialog";
import type { CommandOption } from "../../../model/CommandOption";
import type { GenerateTemplateOption } from "../../../model/SelectParameter";
import Check from "./element/Check";
import PlainText from "./element/PlainText";

export default function JxlsFormSection({
	templateOption,
	handleValueChange,
}: {
	templateOption: GenerateTemplateOption;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
}) {
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);
	return (
		<SectionFieldset>
			<legend>jxls</legend>
			<SectionOptionHeader
				showOptional={showOptional}
				toggleOptional={toggleOptional}
				caption="jxls option"
				command="sections/jxls"
				label="Jxls"
			/>
			<PlainText
				prefix={templateOption.prefix}
				element={templateOption.templateParameterAttribute}
				hidden={!showOptional}
				handleValueChange={handleValueChange(
					templateOption.templateParameterAttribute,
				)}
			/>
			{templateOption.formulaProcess && (
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
		</SectionFieldset>
	);
}
