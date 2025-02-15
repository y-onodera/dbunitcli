import { tauri } from "@tauri-apps/api";
import { useState } from "react";
import { type Running, execParameter, saveParameter, useSelectParameter } from "../../../context/SelectParameterProvider";
import { BlueButton, WhiteButton } from "../../element/Button";
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
	const parameter = useSelectParameter();
	const handleClickSave = (command: string, name: string) => {
		saveParameter(command, name, prop.formData(false).values, setRunning)
	};
	const handleClickExec = (command: string, name: string) => {
		execParameter(command, name, prop.formData(true).values, setRunning)
	};
	const openDirectory = async (path: string) => {
		await tauri.invoke("open_directory", { path });
	};
	if (running.command === "exec") {
		throw new Promise(() => handleClickExec(parameter.command, parameter.name));
	}
	if (running.command === "save") {
		throw new Promise(() => handleClickSave(parameter.command, parameter.name));
	}
	return (
		<>
			<ResultDialog hidden={running.resultMessage === ""}>
				<div className="p-4 md:p-5 text-center">
					<h3 className="mb-5 text-lg font-normal text-gray-500 dark:text-gray-400">
						{running.resultMessage}
					</h3>
					{running.resultDir && (
						<BlueButton title="Open Directory" handleClick={() => openDirectory(running.resultDir)} />
					)}
					<WhiteButton title="Close" handleClick={(() => {
						setRunning({ command: "", resultMessage: "", resultDir: "" });
					})} />
				</div>
			</ResultDialog>
			{parameter.command && (
				<div className="fixed bottom-0 right-1 
                                w-full z-50
								bg-gray-100 
                                flex items-center justify-end "
				>
					<BlueButton title="Exec" handleClick={() => {
						const input = prop.formData(true);
						if (!input.validationError) {
							setRunning({
								command: "exec",
								resultMessage: "",
								resultDir: "",
							});
						}
					}} />
					<BlueButton title="Save" handleClick={() => {
						setRunning({
							command: "save",
							resultMessage: "",
							resultDir: "",
						});
					}} />
					<span className="text-sm text-gray-500">*Required</span>
				</div>
			)}
		</>
	);
}
