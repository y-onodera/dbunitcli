import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import {
	DialogFooter,
	DialogTitle,
	FullDialog,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { AddButton } from "../../../../components/element/ButtonIcon";
import {
	ControllTextBox,
	InputLabel,
} from "../../../../components/element/Input";
import { useEnvironment } from "../../../../context/EnvironmentProvider";
import { useSetResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import type { GenerateOptions } from "../../../../model/SelectParameter";
import { getErrorMessage } from "../../../../utils/fetchUtils";
import {
	collectFormValues,
	type FormValues,
} from "../../../../utils/formValues";
import { DatasetLoadForm } from "../DatasetLoadForm";
import ResourceEditButton, {
	type ResourceEditButtonProp,
} from "./ResourceEditButton";
import {
	type DialogLoadState,
	postDialogForm,
	refreshDialogOptions,
} from "./refreshableDialogForm";

const formId = "xlsxTemplateGenerateForm";
const fileNameField = "-fileName";
const xlsxExtension = ".xlsx";

// generate/refreshにgenerateType=xlsxTemplateのオプション構造を要求する
function withGenerateType(values: FormValues): FormValues {
	return { ...values, "-generateType": "xlsxTemplate" };
}

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

function XlsxTemplateGenerateDialog({
	handleSave,
	handleDialogClose,
}: {
	handleSave: (fileName: string) => void;
	handleDialogClose: () => void;
}) {
	const environment = useEnvironment();
	const setResourcesSettings = useSetResourcesSettings();
	const [loadState, setLoadState] = useState<DialogLoadState<GenerateOptions>>({
		status: "loading",
	});
	const [executing, setExecuting] = useState(false);
	const [executeError, setExecuteError] = useState("");
	const abortControllerRef = useRef<AbortController | null>(null);

	useEffect(() => {
		const controller = new AbortController();
		abortControllerRef.current = controller;
		refreshDialogOptions(
			environment.apiUrl,
			"generate/refresh",
			withGenerateType({}),
			controller.signal,
			setLoadState,
		);
		return () => {
			controller.abort();
		};
	}, [environment.apiUrl]);

	const refreshDialog = async () => {
		await refreshDialogOptions(
			environment.apiUrl,
			"generate/refresh",
			withGenerateType(collectFormValues(formId, false).values),
			abortControllerRef.current?.signal,
			setLoadState,
		);
	};

	const handleExecute = async () => {
		const { values, validationError } = collectFormValues(formId, true);
		if (validationError) {
			return;
		}
		setExecuting(true);
		setExecuteError("");
		try {
			const templateFiles = await postDialogForm<string[]>(
				environment.apiUrl,
				"template/generate",
				values,
			);
			setResourcesSettings((current) => current.with({ templateFiles }));
			if (!abortControllerRef.current?.signal.aborted) {
				handleSave(toSavedFileName(String(values[fileNameField])));
			}
		} catch (ex) {
			if (!abortControllerRef.current?.signal.aborted) {
				setExecuteError(getErrorMessage(ex));
				setExecuting(false);
			}
		}
	};

	return createPortal(
		<FullDialog onClose={handleDialogClose}>
			<div className="overflow-y-auto max-h-[80vh] min-w-[600px] p-4">
				<DialogTitle
					command="dialogs/xlsx-template-generate"
					label="Xlsx Template Generate"
				>
					Generate Xlsx Template
				</DialogTitle>
				{loadState.status === "loading" && (
					<div className="text-content-muted">Loading...</div>
				)}
				{loadState.status === "error" && (
					<div className="text-error">Error: {loadState.message}</div>
				)}
				{loadState.status === "loaded" && (
					<form
						id={formId}
						className="grid gap-6 grid-cols-1"
						noValidate
						onSubmit={(e) => {
							e.preventDefault();
						}}
					>
						<FileNameField />
						<DatasetLoadForm
							handleTypeSelect={refreshDialog}
							name="xlsxTemplateGenerate"
							srcData={loadState.options.srcData}
						/>
					</form>
				)}
				{executeError && (
					<div className="text-error">Error: {executeError}</div>
				)}
			</div>
			<DialogFooter>
				<BlueButton
					title="Execute"
					handleClick={handleExecute}
					disabled={loadState.status !== "loaded" || executing}
				/>
				<WhiteButton title="Close" handleClick={handleDialogClose} />
			</DialogFooter>
		</FullDialog>,
		document.body,
	);
}

export function XlsxTemplateGenerateButton({
	setPath,
}: Pick<ResourceEditButtonProp, "setPath">) {
	const renderDialog = (open: boolean, closeDialog: () => void) => {
		if (!open) {
			return null;
		}
		return (
			<XlsxTemplateGenerateDialog
				handleDialogClose={closeDialog}
				handleSave={(fileName) => {
					setPath(fileName);
					closeDialog();
				}}
			/>
		);
	};

	return (
		<ResourceEditButton
			renderDialog={renderDialog}
			renderTrigger={(openDialog) => (
				<AddButton title="generate" handleClick={openDialog} />
			)}
		/>
	);
}
