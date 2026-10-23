import { core } from "@tauri-apps/api";
import { useEffect, useRef, useState } from "react";
import { SectionHelpButton } from "../../components/dialog";
import { BlueButton, WhiteButton } from "../../components/element/Button";
import { BlueEditButton } from "../../components/element/ButtonIcon";
import { JobLog } from "../../components/element/JobLog";
import {
	useParamInputs,
	useSetParamInputs,
} from "../../context/ParameterInputProvider";
import { useSelectParameter } from "../../context/SelectParameterProvider";
import { useJobLog } from "../../hooks/useJobLog";
import {
	type Running,
	useExecParameter,
	useSaveParameter,
	useSaveShell,
} from "../../hooks/useSelectParameter";
import type { CollectedFormValues, FormValues } from "../../utils/formValues";
import { ParameterInputDialog } from "../form/section/dialog/ParameterInputDialog";
import ResultDialog from "./ResultDialog";

const RESET_RUNNING: Running = { resultMessage: "", resultDir: "" };
const PARAMS_LABEL = "Edit Parameters (-P)";

export default function Footer(prop: {
	formData: (validate: boolean) => CollectedFormValues;
}) {
	const paramInputs = useParamInputs();
	const setParamInputs = useSetParamInputs();
	const [showParamDialog, setShowParamDialog] = useState(false);
	const [running, setRunning] = useState<Running>(RESET_RUNNING);
	const [isLoading, setIsLoading] = useState(false);
	const jobLog = useJobLog();
	const abortControllerRef = useRef<AbortController | null>(null);
	const parameter = useSelectParameter();
	const saveParameter = useSaveParameter();
	const execParameter = useExecParameter();
	const saveShell = useSaveShell();

	useEffect(() => {
		setParamInputs(parameter.inputParam ?? {});
	}, [parameter.inputParam, setParamInputs]);

	// アンマウント時に実行中の処理を中断する
	useEffect(() => {
		return () => {
			abortControllerRef.current?.abort();
		};
	}, []);

	// 実行ごとにAbortControllerを作り、中断・置き換え後の結果は捨てる
	const run = async (
		call: (signal: AbortSignal) => Promise<Running | null>,
	) => {
		const controller = new AbortController();
		abortControllerRef.current = controller;
		jobLog.reset();
		setIsLoading(true);
		const result = await call(controller.signal);
		// 応答後の処理中に中断されることもあるため、nullでなくてもsignalを確認する
		if (result && !controller.signal.aborted) {
			setRunning(result);
			setIsLoading(false);
		}
	};

	const withParamInputs = (values: FormValues): FormValues => ({
		...values,
		...Object.fromEntries(
			Object.entries(paramInputs).map(([k, v]) => [`-P${k}`, v]),
		),
	});

	const handleExec = () => {
		const input = prop.formData(true);
		if (input.validationError) {
			return;
		}
		run((signal) =>
			execParameter(withParamInputs(input.values), signal, jobLog.append),
		);
	};

	const handleSave = () =>
		run((signal) =>
			saveParameter(withParamInputs(prop.formData(false).values), signal),
		);

	const handleSaveShell = () => run((signal) => saveShell(signal));

	const openDirectory = async (path: string) => {
		await core.invoke("open_directory", { path });
	};

	const handleCancel = () => {
		abortControllerRef.current?.abort();
		setIsLoading(false);
		setRunning(RESET_RUNNING);
	};

	if (isLoading) {
		return (
			<div className="overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-modal ">
				<div className="relative p-4 w-full max-w-md max-h-full">
					<div className="relative bg-surface rounded-lg shadow-card ">
						<div className="p-4 md:p-5 flex flex-col justify-center items-center">
							<h3 className="mb-5 text-lg font-normal text-content-muted ">
								Now Execution
							</h3>
							<div className="block animate-spin h-10 w-10 border-4 border-primary rounded-full border-t-transparent" />
							<JobLog lines={jobLog.logs} className="mt-4" />
							<div className="mt-4">
								<WhiteButton title="Abandoned" handleClick={handleCancel} />
							</div>
						</div>
					</div>
				</div>
			</div>
		);
	}

	const paramCount = Object.keys(paramInputs).length;

	return (
		<>
			<ResultDialog hidden={running.resultMessage === ""}>
				<div className="p-4 md:p-5">
					<h3 className="mb-5 text-lg font-normal text-content-muted">
						{running.resultMessage}
					</h3>
					<div className="flex items-center justify-end gap-2">
						{running.resultDir && (
							<BlueButton
								title="Open Directory"
								handleClick={() => openDirectory(running.resultDir)}
							/>
						)}
						<WhiteButton
							title="Close"
							handleClick={() => {
								setRunning(RESET_RUNNING);
							}}
						/>
					</div>
				</div>
			</ResultDialog>
			{parameter.command && (
				<div
					className="fixed bottom-0 right-1
                                w-full z-modal
								bg-surface-muted
                                flex items-center p-4 gap-2"
				>
					<div className="ml-auto flex items-center gap-2">
						<ParamsButton
							count={paramCount}
							handleClick={() => setShowParamDialog(true)}
						/>
						<span className="text-sm font-medium text-content-muted">
							{PARAMS_LABEL}
						</span>
						<ParamsStatus count={paramCount} />
						<SectionHelpButton
							command="dialogs/parameters"
							label="Parameters (-P)"
						/>
						<BlueButton title="Exec" handleClick={handleExec} />
						<BlueButton title="Save" handleClick={handleSave} />
						<BlueButton title="Save Shell" handleClick={handleSaveShell} />
						<span className="text-sm text-content-muted">*Required</span>
					</div>
				</div>
			)}
			{showParamDialog && (
				<ParameterInputDialog
					params={paramInputs}
					handleDialogClose={() => setShowParamDialog(false)}
					handleCommit={(newParams) => {
						setParamInputs(newParams);
						setShowParamDialog(false);
					}}
				/>
			)}
		</>
	);
}

function ParamsButton(prop: { count: number; handleClick: () => void }) {
	return (
		<div className="relative">
			<BlueEditButton title={PARAMS_LABEL} handleClick={prop.handleClick} />
			{prop.count > 0 && (
				<span
					data-testid="params-badge"
					className="absolute -top-2 -right-2 min-w-5 h-5 px-1 flex items-center justify-center rounded-full bg-success text-content-inverse text-xs font-semibold pointer-events-none"
				>
					{prop.count}
				</span>
			)}
		</div>
	);
}

function ParamsStatus(prop: { count: number }) {
	if (prop.count === 0) {
		return <span className="text-sm text-content-muted">— No parameters</span>;
	}
	return <span className="text-label">— {prop.count} parameter(s) set</span>;
}
