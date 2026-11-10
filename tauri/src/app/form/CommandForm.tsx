import { useState } from "react";
import { useSelectParameter } from "../../context/SelectParameterProvider";
import { useRefreshSelectParameter } from "../../hooks/useSelectParameter";
import type { SelectParameter } from "../../model/SelectParameter";
import { CompareForm } from "./CompareForm";
import { ConvertForm } from "./ConvertForm";
import { GenerateForm } from "./GenerateForm";
import { ParameterizeForm } from "./ParameterizeForm";
import { RunForm } from "./RunForm";
import type { TxtTemplateGenerateFormValues } from "./section/dialog/TxtTemplateGenerateDialog";

type FormData = (validate: boolean) => {
	values: { [k: string]: FormDataEntryValue };
	validationError: boolean;
};

export default function CommandForm(prop: { formData: FormData }) {
	const select = useSelectParameter();
	if (!select) {
		return null;
	}
	return <SelectedCommandForm select={select} formData={prop.formData} />;
}

function SelectedCommandForm(prop: {
	select: SelectParameter;
	formData: FormData;
}) {
	const { select } = prop;
	const refreshSelect = useRefreshSelectParameter(select.command);
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
	// srcの入力値（DatasetSrcInfo）は初期値から作るため、keyを更新して再マウントし反映値から作り直す
	const [scaffoldReflectCount, setScaffoldReflectCount] = useState(0);
	const handleScaffoldReflect = async (params: Record<string, string>) => {
		await refreshSelect({ ...prop.formData(false).values, ...params });
		setScaffoldReflectCount((count) => count + 1);
	};

	const renderCommandForm = () => {
		switch (select.options.command) {
			case "convert":
				return (
					<ConvertForm
						key={`convert:${select.name}`}
						handleTypeSelect={handleTypeSelect}
						name={select.name}
						convert={select.options}
					/>
				);
			case "compare":
				return (
					<CompareForm
						key={`compare:${select.name}`}
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
						key={`parameterize:${select.name}`}
						handleTypeSelect={handleTypeSelect}
						name={select.name}
						parameterize={select.options}
					/>
				);
			default:
				return null;
		}
	};

	return renderCommandForm();
}
