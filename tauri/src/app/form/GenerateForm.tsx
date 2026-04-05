import type { GenerateParams } from "../../model/SelectParameter";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import Select from "./section/element/Select";
import Text from "./section/element/TextFormElement";
import TemplateFormSection from "./section/TemplateFormSection";

export function GenerateForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	generate: GenerateParams;
}) {
	const srcData = prop.generate.srcData;
	const ce = prop.generate;
	return (
		<>
			<fieldset className="border border-gray-200 p-3">
				<legend>generate</legend>
				<Select
					handleTypeSelect={prop.handleTypeSelect}
					prefix=""
					element={ce.generateType}
				/>
				{ce.unit && (
					<Select
						handleTypeSelect={prop.handleTypeSelect}
						prefix=""
						element={ce.unit}
					/>
				)}
				{ce.template && <Text prefix="" element={ce.template} />}
				<Text prefix="" element={ce.result} />
				<Text prefix="" element={ce.resultPath} />
				{ce.outputEncoding && <Text prefix="" element={ce.outputEncoding} />}
			</fieldset>
			{prop.generate.templateOption && (
				<fieldset className="border border-gray-200 p-3">
					<legend>{prop.generate.templateOption.prefix}</legend>
					<TemplateFormSection templateOption={prop.generate.templateOption} />
				</fieldset>
			)}
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={srcData}
			/>
		</>
	);
}
