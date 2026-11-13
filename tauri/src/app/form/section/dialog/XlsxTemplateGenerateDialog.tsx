import { useState } from "react";
import { AddButton } from "../../../../components/element/ButtonIcon";
import {
	ControllTextBox,
	InputLabel,
} from "../../../../components/element/Input";
import { useSetResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import type { GenerateOptions } from "../../../../model/SelectParameter";
import type { FormValues } from "../../../../utils/formValues";
import {
	FileGenerateButton,
	type GenerateExecuteContext,
} from "./GenerateDialog";
import { RefreshableDialogShell } from "./RefreshableDialogShell";
import ResourceEditButton, {
	type ResourceEditButtonProp,
} from "./ResourceEditButton";
import { postDialogForm } from "./refreshableDialogForm";
import { useRefreshableDialog } from "./useRefreshableDialog";

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

function useGenerateXlsxTemplate() {
	const setResourcesSettings = useSetResourcesSettings();
	return async (
		values: FormValues,
		{ apiUrl, fetchOptions }: GenerateExecuteContext,
	) => {
		const templateFiles = await postDialogForm<string[]>(
			apiUrl,
			"template/generate",
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
	const generate = useGenerateXlsxTemplate();
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

// テーブル定義書のテンプレートはsrcに依らないため、ファイル名だけを入力する
function XlsxMetaDataTemplateGenerateDialog({
	onSaved,
	onClose,
}: {
	onSaved: (fileName: string) => void;
	onClose: () => void;
}) {
	const setResourcesSettings = useSetResourcesSettings();
	const dialog = useRefreshableDialog<GenerateOptions>({
		refreshEndpoint: "generate/refresh",
		formId: metaDataFormId,
		generateType: dialogConfig.generateType,
	});
	const execute = async () => {
		const result = await dialog.execute(async (values, context) => {
			const templateFiles = await postDialogForm<string[]>(
				dialog.apiUrl,
				"template/generate-metadata",
				values,
				context,
			);
			setResourcesSettings((current) => current.with({ templateFiles }));
			return toSavedFileName(String(values[fileNameField]));
		});
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
			loadState={dialog.loadState}
			executing={dialog.executing}
			executeError={dialog.executeError}
			logs={dialog.logs}
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
		<ResourceEditButton
			renderDialog={(open, closeDialog) =>
				open ? (
					<XlsxMetaDataTemplateGenerateDialog
						onClose={closeDialog}
						onSaved={(fileName) => {
							setPath(fileName);
							closeDialog();
						}}
					/>
				) : null
			}
			renderTrigger={(openDialog) => (
				<AddButton
					title="generate table definition template"
					handleClick={openDialog}
				/>
			)}
		/>
	);
}
