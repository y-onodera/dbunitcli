import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import {
	DialogFooter,
	DialogTitle,
	FullDialog,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { AddButton } from "../../../../components/element/ButtonIcon";
import { useEnvironment } from "../../../../context/EnvironmentProvider";
import {
	DatasetSettings,
	type DatasetSettingsBuilder,
} from "../../../../model/DatasetSettings";
import type { GenerateOptions } from "../../../../model/SelectParameter";
import { getErrorMessage } from "../../../../utils/fetchUtils";
import {
	collectFormValues,
	type FormValues,
} from "../../../../utils/formValues";
import { DatasetLoadForm } from "../DatasetLoadForm";
import { DatasetSettingsEditDialog } from "./DatasetSettingsDialog";
import ResourceEditButton, {
	type ResourceEditButtonProp,
} from "./ResourceEditButton";
import {
	type DialogLoadState,
	postDialogForm,
	refreshDialogOptions,
} from "./refreshableDialogForm";

const formId = "datasetSettingGenerateForm";

// generate/refreshにgenerateType=settingsのオプション構造を要求する
function withGenerateType(values: FormValues): FormValues {
	return { ...values, "-generateType": "settings" };
}

function DatasetSettingGenerateDialog({
	handleSave,
	handleDialogClose,
}: {
	handleSave: (fileName: string) => void;
	handleDialogClose: () => void;
}) {
	const environment = useEnvironment();
	const [loadState, setLoadState] = useState<DialogLoadState<GenerateOptions>>({
		status: "loading",
	});
	const [generated, setGenerated] = useState<DatasetSettings | null>(null);
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
			const settings = await postDialogForm<DatasetSettingsBuilder>(
				environment.apiUrl,
				"dataset-setting/generate",
				withGenerateType(values),
			);
			if (!abortControllerRef.current?.signal.aborted) {
				setGenerated(DatasetSettings.build(settings));
			}
		} catch (ex) {
			if (!abortControllerRef.current?.signal.aborted) {
				setExecuteError(getErrorMessage(ex));
				setExecuting(false);
			}
		}
	};

	// 生成後は既存の編集ダイアログで内容確認・命名保存する
	if (generated) {
		return (
			<DatasetSettingsEditDialog
				settings={generated}
				fileName=""
				handleDialogClose={handleDialogClose}
				handleSave={handleSave}
			/>
		);
	}

	return createPortal(
		<FullDialog onClose={handleDialogClose}>
			<div className="overflow-y-auto max-h-[80vh] min-w-[600px] p-4">
				<DialogTitle>Generate Dataset Setting</DialogTitle>
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
						<DatasetLoadForm
							handleTypeSelect={refreshDialog}
							name="datasetSettingGenerate"
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

export function DatasetSettingGenerateButton({
	setPath,
}: ResourceEditButtonProp) {
	const renderDialog = (open: boolean, closeDialog: () => void) => {
		if (!open) {
			return null;
		}
		return (
			<DatasetSettingGenerateDialog
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
