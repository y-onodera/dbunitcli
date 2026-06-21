import { SectionFieldset, SectionHelpButton } from "../../components/dialog";
import type { GenerateOptions } from "../../model/SelectParameter";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import JxlsFormSection from "./section/JxlsFormSection";
import Check from "./section/element/Check";
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
			<SectionFieldset>
				<legend>generate</legend>
				<SectionHelpButton command="generate" label="Generate" />
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
				{[generate.fixedLength, generate.defaultLength, generate.align]
					.filter((field): field is NonNullable<typeof field> => field != null)
					.map((field) => (
						<PlainText key={field.name} prefix="" element={field} />
					))}
				<FileText prefix="" element={generate.result} />
				<PlainText prefix="" element={generate.resultPath} />
				{generate.outputEncoding && (
					<PlainText prefix="" element={generate.outputEncoding} />
				)}
				{generate.commit && (
					<Check prefix="" element={generate.commit} />
				)}
				{generate.sqlFilePrefix && (
					<PlainText prefix="" element={generate.sqlFilePrefix} />
				)}
				{generate.sqlFileSuffix && (
					<PlainText prefix="" element={generate.sqlFileSuffix} />
				)}
				{generate.includeAllColumns && (
					<Check prefix="" element={generate.includeAllColumns} />
				)}
				{generate.lazyLoad && (
					<Check prefix="" element={generate.lazyLoad} />
				)}
			</SectionFieldset>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={srcData}
			/>
		</>
	);
}
