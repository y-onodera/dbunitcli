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
	FixedColumnDef,
	type FixedColumnDefBuilder,
} from "../../../../model/FixedColumnDef";
import type { GenerateOptions } from "../../../../model/SelectParameter";
import { getErrorMessage } from "../../../../utils/fetchUtils";
import {
	collectFormValues,
	type FormValues,
} from "../../../../utils/formValues";
import { DatasetLoadForm } from "../DatasetLoadForm";
import { FixedColumnDefEditDialog } from "./FixedColumnDefDialog";
import ResourceEditButton, {
	type ResourceEditButtonProp,
} from "./ResourceEditButton";
import {
	type DialogLoadState,
	postDialogForm,
	refreshDialogOptions,
} from "./refreshableDialogForm";

const formId = "fixedColumnDefGenerateForm";

// generate/refreshにgenerateType=fixedColumnDefのオプション構造を要求する
function withGenerateType(values: FormValues): FormValues {
	return { ...values, "-generateType": "fixedColumnDef" };
}

function FixedColumnDefGenerateDialog({
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
	const [generated, setGenerated] = useState<FixedColumnDef | null>(null);
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
			const def = await postDialogForm<FixedColumnDefBuilder>(
				environment.apiUrl,
				"fixed-column-def/generate",
				withGenerateType(values),
			);
			if (!abortControllerRef.current?.signal.aborted) {
				setGenerated(FixedColumnDef.build(def));
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
			<FixedColumnDefEditDialog
				def={generated}
				fileName=""
				handleDialogClose={handleDialogClose}
				handleSave={handleSave}
			/>
		);
	}

	return createPortal(
		<FullDialog onClose={handleDialogClose}>
			<div className="overflow-y-auto max-h-[80vh] min-w-[600px] p-4">
				<DialogTitle>Generate Fixed Column Def</DialogTitle>
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
							name="fixedColumnDefGenerate"
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

export function FixedColumnDefGenerateButton({
	setPath,
}: ResourceEditButtonProp) {
	const renderDialog = (open: boolean, closeDialog: () => void) => {
		if (!open) {
			return null;
		}
		return (
			<FixedColumnDefGenerateDialog
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
