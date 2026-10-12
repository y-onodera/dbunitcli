import { useState } from "react";
import "../../App.css";
import { useSelectParameter } from "../../context/SelectParameterProvider";
import { useRefreshSelectParameter } from "../../hooks/useSelectParameter";
import { CompareForm } from "./CompareForm";
import { ConvertForm } from "./ConvertForm";
import { GenerateForm } from "./GenerateForm";
import { ParameterizeForm } from "./ParameterizeForm";
import { RunForm } from "./RunForm";
import type { TxtTemplateGenerateFormValues } from "./section/dialog/TxtTemplateGenerateDialog";

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
	const scaffoldPrefill = (): TxtTemplateGenerateFormValues => {
		const prefill: TxtTemplateGenerateFormValues = { "-target": "ddl" };
		for (const [key, value] of Object.entries(prop.formData(false).values)) {
			if (key.startsWith("-src.")) {
				prefill[`-dataset.${key.substring("-src.".length)}`] = value;
			}
		}
		return prefill;
	};
	// scaffold実行結果(txt駆動generateパラメータ)を現在のフォーム値へマージして再描画する
	// 入力欄は初期値のみstateへ取り込むため、keyを更新して再マウントし反映値を表示させる
	const [scaffoldReflectCount, setScaffoldReflectCount] = useState(0);
	const handleScaffoldReflect = async (params: Record<string, string>) => {
		await refreshSelect({ ...prop.formData(false).values, ...params });
		setScaffoldReflectCount((count) => count + 1);
	};

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
						key={`generate:${select.name}:${scaffoldReflectCount}`}
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
						key={`run:${select.name}`}
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
