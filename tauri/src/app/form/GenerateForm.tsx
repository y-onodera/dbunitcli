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
	const generate = prop.generate;
	const srcData = generate.srcData;
	return (
		<>
			<fieldset className="border border-gray-200 p-3">
				<legend>generate</legend>
				<Select
					handleTypeSelect={prop.handleTypeSelect}
					prefix=""
					element={generate.generateType}
				/>
				{generate.unit && (
					<Select
						handleTypeSelect={prop.handleTypeSelect}
						prefix=""
						element={generate.unit}
					/>
				)}
				{generate.template && (
					<FileText prefix="" element={generate.template} />
				)}
				{prop.generate.templateOption &&
					generate.generateType.value === "txt" && (
						<TemplateFormSection
							templateOption={prop.generate.templateOption}
							showEncoding={true}
							handleValueChange={() => (_: string) => {}}
						/>
					)}
				<FileText prefix="" element={generate.result} />
				<PlainText prefix="" element={generate.resultPath} />
				{generate.outputEncoding && (
					<PlainText prefix="" element={generate.outputEncoding} />
				)}
			</fieldset>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={srcData}
			/>
		</>
	);
}
