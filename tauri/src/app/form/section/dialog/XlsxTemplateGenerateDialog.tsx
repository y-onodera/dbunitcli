import { type ReactElement, useState } from "react";
import { useEnvironment } from "../../../../context/EnvironmentProvider";
import { useSetResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import { useJob } from "../../../../hooks/useJob";
import {
	collectFormValues,
	type FormValues,
} from "../../../../utils/formValues";
import {
	GenerateButton,
	type GenerateExecuteContext,
	GenerateFormDialog,
} from "./GenerateDialog";
import { RefreshableDialogShell } from "./RefreshableDialogShell";
import type { ResourceEditButtonProp } from "./ResourceEditButton";
import { postDialogForm } from "./refreshableDialogForm";
import { XlsxCompareTemplateGenerateDialog } from "./XlsxCompareTemplateGenerateDialog";
import {
	FileNameField,
	fileNameField,
	TemplateTypeField,
	toSavedFileName,
	type XlsxTemplateType,
	type XlsxTemplateTypeDialogProps,
	xlsxTemplateDialogSize,
	xlsxTemplateDialogTitle,
} from "./XlsxTemplateGenerateParts";

const tableDialogConfig = {
	generateType: "xlsxTemplate",
	formId: "xlsxTemplateGenerateForm",
	name: "xlsxTemplateGenerate",
	command: "dialogs/xlsx-template-generate",
	label: "Generate Xlsx Template",
	title: xlsxTemplateDialogTitle,
};

// 生成したテンプレートをtemplateリソースへ保存し、保存したファイル名を返す
function useGenerateXlsxTemplate(endpoint: string) {
	const setResourcesSettings = useSetResourcesSettings();
	return async (
		values: FormValues,
		{
			apiUrl,
			fetchOptions,
		}: Pick<GenerateExecuteContext, "apiUrl" | "fetchOptions">,
	) => {
		const templateFiles = await postDialogForm<string[]>(
			apiUrl,
			endpoint,
			values,
			fetchOptions,
		);
		setResourcesSettings((current) => current.with({ templateFiles }));
		return toSavedFileName(String(values[fileNameField]));
	};
}

// テーブルごとに1シートを持つテンプレート
function XlsxTableTemplateGenerateDialog({
	header,
	fileName,
	onFileNameChange,
	onSaved,
	onClose,
}: XlsxTemplateTypeDialogProps) {
	const generate = useGenerateXlsxTemplate("template/generate");
	return (
		<GenerateFormDialog
			config={tableDialogConfig}
			header={header}
			size={xlsxTemplateDialogSize}
			leading={<FileNameField value={fileName} onChange={onFileNameChange} />}
			onClose={onClose}
			onExecute={generate}
			onDone={onSaved}
		/>
	);
}

const metaDataFormId = "xlsxMetaDataTemplateGenerateForm";
// srcを読まないため、開いた時点で入力できる
const loadedState = { status: "loaded", options: null } as const;

// テーブル定義書のテンプレートはsrcに依らないため、ファイル名だけを入力する
function XlsxMetaDataTemplateGenerateDialog({
	header,
	fileName,
	onFileNameChange,
	onSaved,
	onClose,
}: XlsxTemplateTypeDialogProps) {
	const { apiUrl } = useEnvironment();
	const generate = useGenerateXlsxTemplate("template/generate-metadata");
	const job = useJob();
	const execute = async () => {
		const { values, validationError } = collectFormValues(metaDataFormId, true);
		if (validationError) {
			return;
		}
		const result = await job.run((fetchOptions) =>
			generate(values, { apiUrl, fetchOptions }),
		);
		if (result) {
			onSaved(result.value);
		}
	};
	return (
		<RefreshableDialogShell
			command="dialogs/xlsx-metadata-template-generate"
			label="Generate Table Definition Template"
			title={xlsxTemplateDialogTitle}
			formId={metaDataFormId}
			loadState={loadedState}
			executing={job.running}
			executeError={job.error}
			logs={job.logs}
			onExecute={execute}
			onClose={onClose}
			header={header}
			size={xlsxTemplateDialogSize}
		>
			{() => <FileNameField value={fileName} onChange={onFileNameChange} />}
		</RefreshableDialogShell>
	);
}

const templateTypeDialogs: Record<
	XlsxTemplateType,
	(props: XlsxTemplateTypeDialogProps) => ReactElement
> = {
	table: XlsxTableTemplateGenerateDialog,
	compare: XlsxCompareTemplateGenerateDialog,
	metadata: XlsxMetaDataTemplateGenerateDialog,
};

// templateTypeを選び、種類ごとのダイアログへ切り替える
function XlsxTemplateGenerateDialog({
	onSaved,
	onClose,
}: Pick<XlsxTemplateTypeDialogProps, "onSaved" | "onClose">) {
	const [templateType, setTemplateType] = useState<XlsxTemplateType>("table");
	const [fileName, setFileName] = useState("");
	const Dialog = templateTypeDialogs[templateType];
	return (
		<Dialog
			header={
				<TemplateTypeField value={templateType} onChange={setTemplateType} />
			}
			fileName={fileName}
			onFileNameChange={setFileName}
			onSaved={onSaved}
			onClose={onClose}
		/>
	);
}

// template欄のdropdownに出す「xlsxTemplate生成」
export function XlsxTemplateGenerateButton({
	setPath,
}: Pick<ResourceEditButtonProp, "setPath">) {
	return (
		<GenerateButton setPath={setPath}>
			{(handleDialogClose, handleSave) => (
				<XlsxTemplateGenerateDialog
					onClose={handleDialogClose}
					onSaved={handleSave}
				/>
			)}
		</GenerateButton>
	);
}
