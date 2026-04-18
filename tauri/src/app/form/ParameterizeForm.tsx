import type { ParameterizeOptions } from "../../model/SelectParameter";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import Check from "./section/element/Check";
import FileText from "./section/element/FileText";
import PlainText from "./section/element/PlainText";
import Select from "./section/element/Select";
import TemplateFormSection from "./section/TemplateFormSection";

export function ParameterizeForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	parameterize: ParameterizeOptions;
}) {
	const parameterize = prop.parameterize;
	const paramData = parameterize.paramData;
	const templateOption = parameterize.templateOption;
	return (
		<>
			<fieldset className="border border-gray-200 p-3">
				<legend>execute</legend>
				<Select
					handleTypeSelect={prop.handleTypeSelect}
					prefix=""
					element={parameterize.unit}
				/>
				<Check prefix="" element={parameterize.parameterize} />
				<Check prefix="" element={parameterize.ignoreFail} />
				<PlainText prefix="" element={parameterize.cmd} />
				<PlainText prefix="" element={parameterize.cmdParam} />
				<FileText prefix="" element={parameterize.template} />
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
