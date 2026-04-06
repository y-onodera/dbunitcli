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
	const refreshSelect = useRefreshSelectParameter(select.options?.command);
	const handleTypeSelect = () => refreshSelect(prop.formData(false).values);
	switch (select.options?.command) {
		case "convert":
			return (
				<ConvertForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					convert={select.options}
				/>
			);
		case "compare":
			return (
				<CompareForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					compare={select.options}
				/>
			);
		case "generate":
			return (
				<GenerateForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					generate={select.options}
				/>
			);
		case "run":
			return (
				<RunForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					run={select.options}
				/>
			);
		case "parameterize":
			return (
				<ParameterizeForm
					handleTypeSelect={handleTypeSelect}
					name={select.name}
					parameterize={select.options}
				/>
			);
		default:
			return null;
	}
}
