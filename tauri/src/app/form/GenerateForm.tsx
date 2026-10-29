import type { ReactNode } from "react";
import { SectionFieldset } from "../../components/dialog";
import { SectionLegend } from "../../components/dialog/SectionFieldset";
import { useResourcesSettings } from "../../context/WorkspaceResourcesProvider";
import type { CommandOption } from "../../model/CommandOption";
import type { GenerateOptions } from "../../model/SelectParameter";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import { RemoveResourceButton } from "./section/dialog/ResourceEditButton";
import { TemplateEditButton } from "./section/dialog/TemplateEditDialog";
import {
	TxtTemplateGenerateButton,
	type TxtTemplateGenerateFormValues,
} from "./section/dialog/TxtTemplateGenerateDialog";
import { XlsxTemplateGenerateButton } from "./section/dialog/XlsxTemplateGenerateDialog";
import Check from "./section/element/Check";
import EncodingText from "./section/element/EncodingText";
import FileText from "./section/element/FileText";
import PlainText from "./section/element/PlainText";
import Select from "./section/element/Select";
import Text, { TextDropDownMenu } from "./section/element/Text";
import JxlsFormSection from "./section/JxlsFormSection";
import TemplateFormSection from "./section/TemplateFormSection";

function TemplateFileText({
	element,
	editButtons,
}: {
	element: CommandOption;
	editButtons: (
		path: string,
		setPath: (value: string) => void,
	) => ReactNode[] | undefined;
}) {
	const settings = useResourcesSettings();
	return (
		<Text
			prefix=""
			element={element}
			showDefaulePath={true}
			resourceFiles={settings.templateFiles}
		>
			{({ path, setPath, isValueInDatalist }) => (
				<TextDropDownMenu
					path={path}
					setPath={setPath}
					prefix=""
					element={element}
					isValueInDatalist={isValueInDatalist}
					editButtons={editButtons(path, setPath)}
					removeButton={() => (
						<RemoveResourceButton
							kind="template"
							path={path}
							setPath={setPath}
						/>
					)}
				/>
			)}
		</Text>
	);
}

export function GenerateForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	generate: GenerateOptions;
	scaffoldPrefill: () => TxtTemplateGenerateFormValues;
	handleScaffoldReflect: (params: Record<string, string>) => Promise<void>;
}) {
	const generate = prop.generate;
	const srcData = generate.srcData;
	const generateTypeValue = generate.generateType.value;
	const isExcelGenerate =
		generateTypeValue === "xlsx" || generateTypeValue === "xls";
	// template欄のdropdown: xlsx/xlsはxlsxTemplate生成、txtはテキストとして編集
	const templateEditButtons = (
		path: string,
		setPath: (value: string) => void,
	) => {
		if (isExcelGenerate) {
			return [<XlsxTemplateGenerateButton key="generate" setPath={setPath} />];
		}
		return [<TemplateEditButton key="edit" path={path} setPath={setPath} />];
	};
	return (
		<>
			<SectionFieldset>
				<SectionLegend
					title="generate"
					command="commands/generate"
					label="Generate"
				/>
				<Select
					handleTypeSelect={prop.handleTypeSelect}
					prefix=""
					element={generate.generateType}
				/>
				{generateTypeValue === "txt" && (
					<TxtTemplateGenerateButton
						scaffoldPrefill={prop.scaffoldPrefill}
						handleReflect={prop.handleScaffoldReflect}
					/>
				)}
				{generate.unit && (
					<Select
						handleTypeSelect={prop.handleTypeSelect}
						prefix=""
						element={generate.unit}
					/>
				)}
				{generate.unitSetting && (
					<FileText prefix="" element={generate.unitSetting} />
				)}
				{generate.unitSettingEncoding && (
					<EncodingText prefix="" element={generate.unitSettingEncoding} />
				)}
				{generate.template && (
					<TemplateFileText
						element={generate.template}
						editButtons={templateEditButtons}
					/>
				)}
				{prop.generate.templateOption && generateTypeValue === "txt" && (
					<TemplateFormSection
						templateOption={prop.generate.templateOption}
						showEncoding={true}
						handleValueChange={() => (_: string) => {}}
					/>
				)}
				{prop.generate.templateOption && isExcelGenerate && (
					<JxlsFormSection templateOption={prop.generate.templateOption} />
				)}
				{[generate.fixedLength, generate.defaultLength, generate.align]
					.filter((field): field is NonNullable<typeof field> => field != null)
					.map((field) => (
						<PlainText key={field.name} prefix="" element={field} />
					))}
				{generate.parallel && (
					<PlainText prefix="" element={generate.parallel} />
				)}
				<FileText prefix="" element={generate.result} />
				<PlainText prefix="" element={generate.resultPath} />
				{generate.outputEncoding && (
					<EncodingText prefix="" element={generate.outputEncoding} />
				)}
				{generate.commit && <Check prefix="" element={generate.commit} />}
				{generate.op && (
					<Select
						prefix=""
						element={generate.op}
						handleTypeSelect={(_: string) => {
							return new Promise((_) => {});
						}}
					/>
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
				{generate.lazyLoad && <Check prefix="" element={generate.lazyLoad} />}
			</SectionFieldset>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={srcData}
				helpCommand="sections/dataset-load-form"
				helpLabel="src"
			/>
		</>
	);
}
