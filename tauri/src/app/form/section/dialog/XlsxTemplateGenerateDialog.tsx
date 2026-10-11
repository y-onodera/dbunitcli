import { useState } from "react";
import {
	ControllTextBox,
	InputLabel,
} from "../../../../components/element/Input";
import { useSetResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import type { FormValues } from "../../../../utils/formValues";
import { FileGenerateButton } from "./GenerateDialog";
import type { ResourceEditButtonProp } from "./ResourceEditButton";
import { postDialogForm } from "./refreshableDialogForm";

const fileNameField = "-fileName";
const xlsxExtension = ".xlsx";

const dialogConfig = {
	generateType: "xlsxTemplate",
	formId: "xlsxTemplateGenerateForm",
	name: "xlsxTemplateGenerate",
	command: "dialogs/xlsx-template-generate",
	label: "Xlsx Template Generate",
	title: "Generate Xlsx Template",
};

// sidecarはxlsxTemplateを常に.xlsxで保存し、拡張子が無ければ補う。template欄へも同名を反映する
function toSavedFileName(name: string): string {
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

// useSetResourcesSettingsをダイアログ表示時にのみ呼ぶため、生成処理はフックとして渡す
function useGenerateXlsxTemplate() {
	const setResourcesSettings = useSetResourcesSettings();
	return async (
		values: FormValues,
		{ apiUrl, isAborted }: { apiUrl: string; isAborted: () => boolean },
	) => {
		const templateFiles = await postDialogForm<string[]>(
			apiUrl,
			"template/generate",
			values,
		);
		setResourcesSettings((current) => current.with({ templateFiles }));
		return isAborted() ? null : toSavedFileName(String(values[fileNameField]));
	};
}

export function XlsxTemplateGenerateButton({
	setPath,
}: Pick<ResourceEditButtonProp, "setPath">) {
	return (
		<FileGenerateButton
			setPath={setPath}
			config={dialogConfig}
			leading={<FileNameField />}
			useGenerate={useGenerateXlsxTemplate}
		/>
	);
}
