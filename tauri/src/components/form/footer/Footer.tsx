import { tauri } from "@tauri-apps/api";
import { Body, ResponseType, fetch } from "@tauri-apps/api/http";
import { useState } from "react";
import { useEnviroment } from "../../../context/EnviromentProvider";
import { useSelectParameter } from "../../../context/SelectParameterProvider";
import { BlueButton, WhiteButton } from "../../element/button/Button";
import ResultDialog from "./ResultDialog";

type Running = {
	command: string;
	resultMessage: string;
	resultDir: string;
};

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
	const environment = useEnviroment();
	const parameter = useSelectParameter();
	const handleClickSave = async (command: string, name: string) => {
		await fetch(`${environment.apiUrl + command}/save`, {
			method: "POST",
			responseType: ResponseType.Text,
			headers: { "Content-Type": "application/json" },
			body: Body.json({ name, input: prop.formData(false).values }),
		})
			.then((response) => {
				if (!response.ok) {
					console.error("response.ok:", response.ok);
					console.error("esponse.status:", response.status);
					throw new Error(response.data as string);
				}
				setRunning({
					command: "",
					resultMessage: "Save Success",
					resultDir: "",
				});
			})
			.catch((ex) => {
				setRunning({ command: "", resultMessage: ex.message, resultDir: "" });
			});
	};
	const handleClickExec = async (command: string, name: string) => {
		const input = prop.formData(true).values;
		await fetch(`${environment.apiUrl + command}/exec`, {
			method: "POST",
			responseType: ResponseType.Text,
			headers: { "Content-Type": "application/json" },
			body: Body.json({ name, input }),
		})
			.then((response) => {
				if (!response.ok) {
					console.error("esponse.status:", response.status);
					throw new Error(response.data as string);
				}
				setRunning({
					command: "",
					resultMessage: "Execution Success",
					resultDir: response.data as string,
				});
			})
			.catch((ex) => {
				setRunning({ command: "", resultMessage: ex.message, resultDir: "" });
			});
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
