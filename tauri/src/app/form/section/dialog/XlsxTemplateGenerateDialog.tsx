import { useState } from "react";
import {
	ControllTextBox,
	InputLabel,
} from "../../../../components/element/Input";
import { useEnvironment } from "../../../../context/EnvironmentProvider";
import { useSetResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import { useJob } from "../../../../hooks/useJob";
import {
	collectFormValues,
	type FormValues,
} from "../../../../utils/formValues";
import {
	FileGenerateButton,
	GenerateButton,
	type GenerateExecuteContext,
} from "./GenerateDialog";
import { RefreshableDialogShell } from "./RefreshableDialogShell";
import type { ResourceEditButtonProp } from "./ResourceEditButton";
import { postDialogForm } from "./refreshableDialogForm";

const fileNameField = "-fileName";
const xlsxExtension = ".xlsx";

const dialogConfig = {
	generateType: "xlsxTemplate",
	formId: "xlsxTemplateGenerateForm",
	name: "xlsxTemplateGenerate",
	command: "dialogs/xlsx-template-generate",
	label: "Generate Xlsx Template",
	title: "Generate Xlsx Template",
};

// sidecarはxlsxTemplateを常に.xlsxで保存し、拡張子が無ければ補う。template欄へも同名を反映する
export function toSavedFileName(name: string): string {
	return name.toLowerCase().endsWith(xlsxExtension)
		? name
		: name + xlsxExtension;
}

function FileNameField() {
	const [fileName, setFileName] = useState("");
	return (
		<div>
			<InputLabel
				text={fileNameField}
				id="xlsxTemplateFileName"
				required={true}
			/>
			<ControllTextBox
				name={fileNameField}
				id="xlsxTemplateFileName"
				required={true}
				value={fileName}
				handleChange={(ev) => setFileName(ev.target.value)}
			/>
		</div>
	);
}

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

export function XlsxTemplateGenerateButton({
	setPath,
}: Pick<ResourceEditButtonProp, "setPath">) {
	const generate = useGenerateXlsxTemplate("template/generate");
	return (
		<FileGenerateButton
			setPath={setPath}
			config={dialogConfig}
			leading={<FileNameField />}
			generate={generate}
		/>
	);
}

const metaDataFormId = "xlsxMetaDataTemplateGenerateForm";
// srcを読まないため、開いた時点で入力できる
const loadedState = { status: "loaded", options: null } as const;

// テーブル定義書のテンプレートはsrcに依らないため、ファイル名だけを入力する
function XlsxMetaDataTemplateGenerateDialog({
	onSaved,
	onClose,
}: {
	onSaved: (fileName: string) => void;
	onClose: () => void;
}) {
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
			title="Generate Table Definition Template"
			formId={metaDataFormId}
			loadState={loadedState}
			executing={job.running}
			executeError={job.error}
			logs={job.logs}
			onExecute={execute}
			onClose={onClose}
		>
			{() => <FileNameField />}
		</RefreshableDialogShell>
	);
}

// template欄のdropdownに出す「テーブル定義書テンプレート生成」
export function XlsxMetaDataTemplateGenerateButton({
	setPath,
}: Pick<ResourceEditButtonProp, "setPath">) {
	return (
		<GenerateButton
			setPath={setPath}
			title="generate table definition template"
		>
			{(handleDialogClose, handleSave) => (
				<XlsxMetaDataTemplateGenerateDialog
					onClose={handleDialogClose}
					onSaved={handleSave}
				/>
			)}
		</GenerateButton>
	);
}
