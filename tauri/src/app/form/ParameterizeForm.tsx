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
	const paramData = prop.parameterize.paramData;
	const templateOption = prop.parameterize.templateOption;
	const ce = prop.parameterize;
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
				<PlainText prefix="" element={ce.cmd} />
				<PlainText prefix="" element={ce.cmdParam} />
				<FileText prefix="" element={ce.template} />
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
