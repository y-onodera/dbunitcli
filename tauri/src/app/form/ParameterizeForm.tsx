import type { ParameterizeParams } from "../../model/SelectParameter";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import Check from "./section/element/Check";
import Select from "./section/element/Select";
import Text from "./section/element/TextFormElement";
import TemplateFormSection from "./section/TemplateFormSection";

export function ParameterizeForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	parameterize: ParameterizeParams;
}) {
	const paramData = prop.parameterize.paramData;
	const templateOption = prop.parameterize.templateOption;
	const ce = prop.parameterize.commandElements;
	return (
		<>
			<fieldset className="border border-gray-200 p-3">
				<legend>execute</legend>
				<Select
					handleTypeSelect={prop.handleTypeSelect}
					prefix=""
					element={ce.unit}
				/>
				<Check prefix="" element={ce.parameterize} />
				<Check prefix="" element={ce.ignoreFail} />
				<Text prefix="" element={ce.cmd} />
				<Text prefix="" element={ce.cmdParam} />
				<Text prefix="" element={ce.template} />
				{templateOption && (
					<TemplateFormSection templateOption={templateOption} />
				)}
			</fieldset>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={paramData}
			/>
		</>
	);
}
