import { core } from "@tauri-apps/api";
import { useEffect, useRef, useState } from "react";
import { BlueButton, WhiteButton } from "../../components/element/Button";
import { BlueEditButton } from "../../components/element/ButtonIcon";
import {
	useParamInputs,
	useSetParamInputs,
} from "../../context/ParameterInputProvider";
import { useSelectParameter } from "../../context/SelectParameterProvider";
import {
	type Running,
	useExecParameter,
	useSaveParameter,
	useSaveShell,
} from "../../hooks/useSelectParameter";
import { ParameterInputDialog } from "../form/section/dialog/ParameterInputDialog";
import ResultDialog from "./ResultDialog";

const RESET_RUNNING: Running = { command: "", resultMessage: "", resultDir: "" };
const PARAMS_LABEL = "Edit Parameters (-P)";

export default function Footer(prop: {
	formData: (validate: boolean) => {
		values: { [k: string]: FormDataEntryValue };
		validationError: boolean;
	};
}) {
	const paramInputs = useParamInputs();
	const setParamInputs = useSetParamInputs();
	const [showParamDialog, setShowParamDialog] = useState(false);
	const [running, setRunning] = useState<Running>(RESET_RUNNING);
	const [isLoading, setIsLoading] = useState(false);
	const executingRef = useRef(false);
	const abortControllerRef = useRef<AbortController | null>(null);
	const parameter = useSelectParameter();
	const saveParameter = useSaveParameter();
	const execParameter = useExecParameter();
	const saveShell = useSaveShell();

	const execParameterRef = useRef(execParameter);
	const saveParameterRef = useRef(saveParameter);
	const saveShellRef = useRef(saveShell);
	execParameterRef.current = execParameter;
	saveParameterRef.current = saveParameter;
	saveShellRef.current = saveShell;

	useEffect(() => {
		setParamInputs({});
	}, [parameter.name]);

	useEffect(() => {
		if (running.command === "" || executingRef.current) {
			return;
		}
		executingRef.current = true;
		let active = true;
		setIsLoading(true);
		const controller = new AbortController();
		abortControllerRef.current = controller;

		const handleResult = (result: Running) => {
			if (active) {
				executingRef.current = false;
				abortControllerRef.current = null;
				setRunning(result);
				setIsLoading(false);
			}
		};

		const formValues = prop.formData(false).values;
		const paramExtra = Object.fromEntries(
			Object.entries(paramInputs).map(([k, v]) => [`-P${k}`, v]),
		);
		if (running.command === "exec") {
			execParameterRef.current(
				{ ...formValues, ...paramExtra },
				handleResult,
				controller.signal,
			);
		} else if (running.command === "save") {
			saveParameterRef.current(
				{ ...formValues, ...paramExtra },
				handleResult,
				controller.signal,
			);
		} else if (running.command === "saveShell") {
			saveShellRef.current(
				{ ...formValues, ...paramExtra },
				handleResult,
				controller.signal,
			);
		}

		return () => {
			active = false;
			abortControllerRef.current?.abort();
			abortControllerRef.current = null;
		};
	}, [running.command]);

	const openDirectory = async (path: string) => {
		await core.invoke("open_directory", { path });
	};

	const handleCancel = () => {
		abortControllerRef.current?.abort();
		abortControllerRef.current = null;
		executingRef.current = false;
		setIsLoading(false);
		setRunning(RESET_RUNNING);
	};

	if (isLoading) {
		return (
			<div className="overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-50 ">
				<div className="relative p-4 w-full max-w-md max-h-full">
					<div className="relative bg-surface rounded-lg shadow-sm ">
						<div className="p-4 md:p-5 flex flex-col justify-center items-center">
							<h3 className="mb-5 text-lg font-normal text-content-muted ">
								Now Execution
							</h3>
							<div className="block animate-spin h-10 w-10 border-4 border-link rounded-full border-t-transparent" />
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
                                w-full z-50
								bg-surface-muted
                                flex items-center p-4 gap-2"
				>
					<div className="ml-auto flex items-center gap-2">
						<BlueEditButton
							title={PARAMS_LABEL}
							handleClick={() => setShowParamDialog(true)}
						/>
						<span className="text-sm font-medium text-content-muted">
							{PARAMS_LABEL}
						</span>
						{paramCount > 0 && (
							<span className="text-sm text-content-muted">
								— {paramCount} parameter(s) set
							</span>
						)}
						<BlueButton
							title="Exec"
							handleClick={() => {
								const input = prop.formData(true);
								if (!input.validationError) {
									setRunning({
										command: "exec",
										resultMessage: "",
										resultDir: "",
									});
								}
							}}
						/>
						<BlueButton
							title="Save"
							handleClick={() => {
								setRunning({
									command: "save",
									resultMessage: "",
									resultDir: "",
								});
							}}
						/>
						<BlueButton
							title="Save Shell"
							handleClick={() => {
								setRunning({
									command: "saveShell",
									resultMessage: "",
									resultDir: "",
								});
							}}
						/>
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
