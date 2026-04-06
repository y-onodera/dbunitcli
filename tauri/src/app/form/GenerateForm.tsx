import type { GenerateOptions } from "../../model/SelectParameter";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import FileText from "./section/element/FileText";
import PlainText from "./section/element/PlainText";
import Select from "./section/element/Select";
import TemplateFormSection from "./section/TemplateFormSection";

export function GenerateForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	generate: GenerateOptions;
}) {
	const srcData = prop.generate.srcData;
	const ce = prop.generate;
	return (
		<>
			<fieldset className="border border-gray-200 p-3">
				<legend>generate</legend>
				<Select
					handleTypeSelect={prop.handleTypeSelect}
					prefix=""
					element={ce.generateType}
				/>
				{ce.unit && (
					<Select
						handleTypeSelect={prop.handleTypeSelect}
						prefix=""
						element={ce.unit}
					/>
				)}
				{ce.template && <FileText prefix="" element={ce.template} />}
				<FileText prefix="" element={ce.result} />
				<PlainText prefix="" element={ce.resultPath} />
				{ce.outputEncoding && (
					<PlainText prefix="" element={ce.outputEncoding} />
				)}
			</fieldset>
			{prop.generate.templateOption && (
				<fieldset className="border border-gray-200 p-3">
					<legend>{prop.generate.templateOption.prefix}</legend>
					<TemplateFormSection templateOption={prop.generate.templateOption} />
				</fieldset>
			)}
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={srcData}
			/>
		</>
	);
}
