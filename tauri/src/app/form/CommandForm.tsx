import "../../App.css";
import { useSelectParameter } from "../../context/SelectParameterProvider";
import { useRefreshSelectParameter } from "../../hooks/useSelectParameter";
import { CompareForm } from "./CompareForm";
import { ConvertForm } from "./ConvertForm";
import { GenerateForm } from "./GenerateForm";
import { ParameterizeForm } from "./ParameterizeForm";
import { RunForm } from "./RunForm";
import type { ScaffoldFormValues } from "./section/dialog/ScaffoldDialog";

export default function CommandForm(prop: {
	formData: (validate: boolean) => {
		values: { [k: string]: FormDataEntryValue };
		validationError: boolean;
	};
}) {
	const select = useSelectParameter();
	const refreshSelect = useRefreshSelectParameter(select.options?.command);
	const handleTypeSelect = () => refreshSelect(prop.formData(false).values);
	// generateフォームの-src.*をscaffoldダイアログの-dataset.*初期値として引き継ぐ
	const scaffoldPrefill = (): ScaffoldFormValues => {
		const prefill: ScaffoldFormValues = { "-target": "ddl" };
		for (const [key, value] of Object.entries(prop.formData(false).values)) {
			if (key.startsWith("-src.")) {
				prefill[`-dataset.${key.substring("-src.".length)}`] = value;
			}
		}
		return prefill;
	};
	// scaffold実行結果(txt駆動generateパラメータ)を現在のフォーム値へマージして再描画する
	const handleScaffoldReflect = async (params: Record<string, string>) =>
		refreshSelect({ ...prop.formData(false).values, ...params });

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
						scaffoldPrefill={scaffoldPrefill}
						handleScaffoldReflect={handleScaffoldReflect}
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

	return commandForm;
}
