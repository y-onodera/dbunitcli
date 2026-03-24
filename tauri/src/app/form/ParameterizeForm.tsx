import type { ParameterizeParams } from "../../model/SelectParameter";
import Check from "./section/Check";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import Select from "./section/Select";
import TemplateFormSection from "./section/TemplateFormSection";
import Text from "./section/TextFormElement";

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
					<TemplateFormSection
						commandParams={templateOption}
						name={prop.name}
					/>
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
