import { core } from "@tauri-apps/api";
import { useState } from "react";
import { BlueButton, WhiteButton } from "../../components/element/Button";
import { type Running, useExecParameter, useSaveParameter, useSelectParameter } from "../../context/SelectParameterProvider";
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
	const saveParameter = useSaveParameter();
	const execParameter = useExecParameter();
	const openDirectory = async (path: string) => {
		await core.invoke("open_directory", { path });
	};
	if (running.command === "exec") {
		throw new Promise(() => execParameter(prop.formData(true).values, setRunning));
	}
	if (running.command === "save") {
		throw new Promise(() => saveParameter(prop.formData(false).values, setRunning));
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
