import "../../App.css";
import type React from "react";
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
	const command = select.command;
	const refreshSelect = useRefreshSelectParameter(command)
	const handleTypeSelect = () => refreshSelect(prop.formData(false).values)
	return (
		<>
			{renderForm(command, handleTypeSelect, select)}
		</>
	);
}

function renderForm(
	command: string,
	handleTypeSelect: () => Promise<void>,
	select: ReturnType<typeof useSelectParameter>,
): React.ReactElement | null {
	if (command === "convert") {
		return (
			<ConvertForm
				handleTypeSelect={handleTypeSelect}
				name={select.name}
				convert={select.convert}
			/>
		);
	}
	if (command === "compare") {
		return (
			<CompareForm
				handleTypeSelect={handleTypeSelect}
				name={select.name}
				compare={select.compare}
			/>
		);
	}
	if (command === "generate") {
		return (
			<GenerateForm
				handleTypeSelect={handleTypeSelect}
				name={select.name}
				generate={select.generate}
			/>
		);
	}
	if (command === "run") {
		return (
			<RunForm
				handleTypeSelect={handleTypeSelect}
				name={select.name}
				run={select.run}
			/>
		);
	}
	if (command === "parameterize") {
		return (
			<ParameterizeForm
				handleTypeSelect={handleTypeSelect}
				name={select.name}
				parameterize={select.parameterize}
			/>
		);
	}
	return null;
}
