import { Body, ResponseType, fetch } from "@tauri-apps/api/http";
import {
	useSelectParameter,
	useSetSelectParameter,
} from "../../context/SelectParameterProvider";
import "../../App.css";
import { useEnviroment } from "../../context/EnviromentProvider";
import type { Parameter } from "../../model/CommandParam";
import { CompareForm } from "./CompareForm";
import { ConvertForm } from "./ConvertForm";
import { GenerateForm } from "./GenerateForm";
import { ParameterizeForm } from "./ParameterizeForm";
import { RunForm } from "./RunForm";

export default function CommandForm(prop: {
	formData: (validate: boolean) => {
		values: { [k: string]: FormDataEntryValue };
		validationError: boolean;
	};
}) {
	const environment = useEnviroment();
	const select = useSelectParameter();
	const command = select.command;
	const setParameter = useSetSelectParameter();
	const handleTypeSelect = async () => {
		await fetch(`${environment.apiUrl + command}/refresh`, {
			method: "POST",
			responseType: ResponseType.JSON,
			headers: { "Content-Type": "application/json" },
			body: Body.json(prop.formData(false).values),
		})
			.then((response) =>
				setParameter(response.data as Parameter, command, select.name),
			)
			.catch((ex) => alert(ex));
	};
	return (
		<>
			{command === "convert" ? (
				<ConvertForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					convert={select.convert}
				/>
			) : command === "compare" ? (
				<CompareForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					compare={select.compare}
				/>
			) : command === "generate" ? (
				<GenerateForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					generate={select.generate}
				/>
			) : command === "run" ? (
				<RunForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					run={select.run}
				/>
			) : command === "parameterize" ? (
				<ParameterizeForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					parameterize={select.parameterize}
				/>
			) : (
				<></>
			)}
		</>
	);
}
