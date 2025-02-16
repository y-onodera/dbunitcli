import "../../App.css";
import { useRefreshSelectParameter, useSelectParameter } from "../../context/SelectParameterProvider";
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
	const select = useSelectParameter();
	const command = select.command;
	const refreshSelect = useRefreshSelectParameter(command)
	const handleTypeSelect = () => refreshSelect(prop.formData(false).values)
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
