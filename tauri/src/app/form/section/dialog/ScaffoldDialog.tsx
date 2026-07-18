import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import {
	DialogFooter,
	DialogTitle,
	FullDialog,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { useEnvironment } from "../../../../context/EnvironmentProvider";
import { useWorkspaceResourcesReload } from "../../../../hooks/useWorkspaceResources";
import type { CommandOption } from "../../../../model/CommandOption";
import type { ScaffoldOptions } from "../../../../model/SelectParameter";
import { getErrorMessage } from "../../../../utils/fetchUtils";
import {
	collectFormValues,
	type FormValues,
} from "../../../../utils/formValues";
import { DatasetLoadForm } from "../DatasetLoadForm";
import PlainText from "../element/PlainText";
import Select from "../element/Select";
import {
	type DialogLoadState,
	postDialogForm,
	refreshDialogOptions,
} from "./refreshableDialogForm";

const formId = "scaffoldForm";

export type ScaffoldFormValues = FormValues;

type LoadState = DialogLoadState<ScaffoldOptions>;

// scaffold実行結果はgenerateフォームへのtxt駆動反映が前提のため、parameter targetは選ばせない
function withoutParameterTarget(target: CommandOption): CommandOption {
	return {
		...target,
		attribute: {
			...target.attribute,
			selectOption: target.attribute.selectOption.filter(
				(it) => it !== "parameter",
			),
		},
	};
}

// 反映には-templateが必須（sidecarのexecも-template必須で検証する）
function requiredOption(element: CommandOption): CommandOption {
	return { ...element, attribute: { ...element.attribute, required: true } };
}

function ScaffoldDialog({
	prefill,
	handleReflect,
	handleDialogClose,
}: {
	prefill: ScaffoldFormValues;
	handleReflect: (params: Record<string, string>) => Promise<void>;
	handleDialogClose: () => void;
}) {
	const environment = useEnvironment();
	const reloadResources = useWorkspaceResourcesReload();
	const [loadState, setLoadState] = useState<LoadState>({ status: "loading" });
	const [executing, setExecuting] = useState(false);
	const [executeError, setExecuteError] = useState("");
	const abortControllerRef = useRef<AbortController | null>(null);

	useEffect(() => {
		const controller = new AbortController();
		abortControllerRef.current = controller;
		refreshDialogOptions(
			environment.apiUrl,
			"scaffold/refresh",
			prefill,
			controller.signal,
			setLoadState,
		);
		return () => {
			controller.abort();
		};
	}, [environment.apiUrl, prefill]);

	const refreshDialog = async () => {
		await refreshDialogOptions(
			environment.apiUrl,
			"scaffold/refresh",
			collectFormValues(formId, false).values,
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
			const params = await postDialogForm<Record<string, string>>(
				environment.apiUrl,
				"scaffold/exec",
				values,
			);
			await Promise.all([handleReflect(params), reloadResources()]);
			if (!abortControllerRef.current?.signal.aborted) {
				handleDialogClose();
			}
		} catch (ex) {
			if (!abortControllerRef.current?.signal.aborted) {
				setExecuteError(getErrorMessage(ex));
				setExecuting(false);
			}
		}
	};

	const renderFields = (options: ScaffoldOptions) => (
		<form
			id={formId}
			className="grid gap-6 grid-cols-1"
			noValidate
			onSubmit={(e) => {
				e.preventDefault();
			}}
		>
			<Select
				handleTypeSelect={refreshDialog}
				prefix=""
				element={withoutParameterTarget(options.target)}
			/>
			<PlainText prefix="" element={requiredOption(options.template)} />
			<PlainText prefix="" element={options.unitSetting} />
			<Select
				handleTypeSelect={refreshDialog}
				prefix=""
				element={options.datasetType}
			/>
			<PlainText prefix="" element={options.datasetEncoding} />
			{[options.fixedLength, options.defaultLength, options.align]
				.filter((field): field is NonNullable<typeof field> => field != null)
				.map((field) => (
					<PlainText key={field.name} prefix="" element={field} />
				))}
			<DatasetLoadForm
				handleTypeSelect={refreshDialog}
				name="scaffold"
				srcData={options.dataset}
			/>
		</form>
	);

	return createPortal(
		<FullDialog onClose={handleDialogClose}>
			<div className="overflow-y-auto max-h-[80vh] min-w-[600px] p-4">
				<DialogTitle>Scaffold</DialogTitle>
				{loadState.status === "loading" && (
					<div className="text-content-muted">Loading...</div>
				)}
				{loadState.status === "error" && (
					<div className="text-error">Error: {loadState.message}</div>
				)}
				{loadState.status === "loaded" && renderFields(loadState.options)}
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

export function ScaffoldButton({
	scaffoldPrefill,
	handleReflect,
}: {
	scaffoldPrefill: () => ScaffoldFormValues;
	handleReflect: (params: Record<string, string>) => Promise<void>;
}) {
	const [prefill, setPrefill] = useState<ScaffoldFormValues | null>(null);
	return (
		<div>
			<BlueButton
				title="Scaffold"
				handleClick={() => setPrefill(scaffoldPrefill())}
			/>
			{prefill && (
				<ScaffoldDialog
					prefill={prefill}
					handleReflect={handleReflect}
					handleDialogClose={() => setPrefill(null)}
				/>
			)}
		</div>
	);
}
