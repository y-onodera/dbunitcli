import "../../App.css";
import { useEffect, useState } from "react";
import { BlueEditButton } from "../../components/element/ButtonIcon";
import { useSelectParameter } from "../../context/SelectParameterProvider";
import { useRefreshSelectParameter } from "../../hooks/useSelectParameter";
import { CompareForm } from "./CompareForm";
import { ConvertForm } from "./ConvertForm";
import { GenerateForm } from "./GenerateForm";
import { ParameterizeForm } from "./ParameterizeForm";
import { RunForm } from "./RunForm";
import { ParameterInputDialog } from "./section/dialog/ParameterInputDialog";

export default function CommandForm(prop: {
	formData: (validate: boolean) => {
		values: { [k: string]: FormDataEntryValue };
		validationError: boolean;
	};
}) {
	const select = useSelectParameter();
	const refreshSelect = useRefreshSelectParameter(select.options?.command);
	const handleTypeSelect = () => refreshSelect(prop.formData(false).values);
	const [paramInputs, setParamInputs] = useState<{ [key: string]: string }>(
		{},
	);
	const [showParamDialog, setShowParamDialog] = useState(false);

	useEffect(() => {
		setParamInputs({});
	}, [select.name]);

	const renderCommandForm = () => {
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
	};

	const commandForm = renderCommandForm();
	if (!commandForm) {
		return null;
	}

	const paramCount = Object.keys(paramInputs).length;

	return (
		<>
			{commandForm}
			<fieldset className="border border-gray-200 p-3">
				<legend>Parameters (-P)</legend>
				<div className="flex items-center gap-2">
					<BlueEditButton
						title="Edit Parameters"
						handleClick={() => setShowParamDialog(true)}
					/>
					{paramCount > 0 && (
						<span className="text-sm text-content-muted">
							{paramCount} parameter(s) set
						</span>
					)}
				</div>
				{Object.entries(paramInputs)
					.filter(([name]) => name !== "")
					.map(([name, value]) => (
						<input
							key={name}
							type="hidden"
							name={`-P${name}`}
							value={value}
						/>
					))}
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
			</fieldset>
		</>
	);
}
