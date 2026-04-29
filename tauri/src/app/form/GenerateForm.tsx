import type { GenerateOptions } from "../../model/SelectParameter";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import JxlsFormSection from "./section/JxlsFormSection";
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
	const generateTypeValue = generate.generateType.value;
	const isExcelGenerate =
		generateTypeValue === "xlsx" || generateTypeValue === "xls";
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
					generateTypeValue === "txt" && (
						<TemplateFormSection
							templateOption={prop.generate.templateOption}
							showEncoding={true}
							handleValueChange={() => (_: string) => {}}
						/>
					)}
				{prop.generate.templateOption && isExcelGenerate && (
					<JxlsFormSection
						templateOption={prop.generate.templateOption}
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
