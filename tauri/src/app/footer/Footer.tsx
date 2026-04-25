import { core } from "@tauri-apps/api";
import { useEffect, useRef, useState } from "react";
import { BlueButton, WhiteButton } from "../../components/element/Button";
import { useSelectParameter } from "../../context/SelectParameterProvider";
import {
	type Running,
	useExecParameter,
	useSaveParameter,
	useSaveShell,
} from "../../hooks/useSelectParameter";
import ResultDialog from "./ResultDialog";

export default function Footer(prop: {
	formData: (validate: boolean) => {
		values: { [k: string]: FormDataEntryValue };
		validationError: boolean;
	};
}) {
	const [running, setRunning] = useState({
		command: "",
		resultMessage: "",
		resultDir: "",
	} as Running);
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

		if (running.command === "exec") {
			execParameterRef.current(prop.formData(false).values, handleResult, controller.signal);
		} else if (running.command === "save") {
			saveParameterRef.current(prop.formData(false).values, handleResult, controller.signal);
		} else if (running.command === "saveShell") {
			saveShellRef.current(handleResult, controller.signal);
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
		setRunning({ command: "", resultMessage: "", resultDir: "" });
	};

	if (isLoading) {
		return (
			<div className="overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-50 ">
				<div className="relative p-4 w-full max-w-md max-h-full">
					<div className="relative bg-white rounded-lg shadow-sm ">
						<div className="p-4 md:p-5 flex flex-col justify-center items-center">
							<h3 className="mb-5 text-lg font-normal text-gray-500 ">
								Now Execution
							</h3>
							<div className="block animate-spin h-10 w-10 border-4 border-blue-500 rounded-full border-t-transparent" />
							<div className="mt-4">
								<WhiteButton title="Abandoned" handleClick={handleCancel} />
							</div>
						</div>
					</div>
				</div>
			</div>
		);
	}

	return (
		<>
			<ResultDialog hidden={running.resultMessage === ""}>
				<div className="p-4 md:p-5">
					<h3 className="mb-5 text-lg font-normal text-gray-500 dark:text-gray-400">
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
								setRunning({ command: "", resultMessage: "", resultDir: "" });
							}}
						/>
					</div>
				</div>
			</ResultDialog>
			{parameter.command && (
				<div
					className="fixed bottom-0 right-1
                                w-full z-50
								bg-gray-100
                                flex items-center justify-end p-4 gap-2"
				>
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
					<span className="text-sm text-gray-500">*Required</span>
				</div>
			)}
		</>
	);
}
