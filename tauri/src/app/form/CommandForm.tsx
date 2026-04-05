import "../../App.css";
import { useSelectParameter } from "../../context/SelectParameterProvider";
import { useRefreshSelectParameter } from "../../hooks/useSelectParameter";
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
	const refreshSelect = useRefreshSelectParameter(select.parameter?.command);
	const handleTypeSelect = () => refreshSelect(prop.formData(false).values);
	switch (select.parameter?.command) {
		case "convert":
			return (
				<ConvertForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					convert={select.parameter}
				/>
			);
		case "compare":
			return (
				<CompareForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					compare={select.parameter}
				/>
			);
		case "generate":
			return (
				<GenerateForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					generate={select.parameter}
				/>
			);
		case "run":
			return (
				<RunForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					run={select.parameter}
				/>
			);
		case "parameterize":
			return (
				<ParameterizeForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					parameterize={select.parameter}
				/>
			);
		default:
			return null;
	}
}
