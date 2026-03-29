import { JdbcConnectionProvider } from "../../context/JdbcConnectionProvider";
import type { RunParams } from "../../model/SelectParameter";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import Select from "./section/element/Select";
import JdbcFormSection from "./section/JdbcFormSection";
import TemplateFormSection from "./section/TemplateFormSection";

export function RunForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	run: RunParams;
}) {
	const srcData = prop.run.srcData;
	const templateOption = prop.run.templateOption;
	const jdbcOption = prop.run.jdbcOption;
	const ce = prop.run.commandElements;
	return (
		<>
			<fieldset className="border border-gray-200 p-3">
				<legend>run</legend>
				<Select
					handleTypeSelect={prop.handleTypeSelect}
					prefix=""
					element={ce.scriptType}
				/>
			</fieldset>
			{templateOption && (
				<fieldset className="border border-gray-200 p-3">
					<legend>template</legend>
					<TemplateFormSection templateOption={templateOption} />
				</fieldset>
			)}
			{jdbcOption && (
				<JdbcConnectionProvider>
					<fieldset className="border border-gray-200 p-3">
						<legend>jdbc</legend>
						<JdbcFormSection jdbcOption={jdbcOption} />
					</fieldset>
				</JdbcConnectionProvider>
			)}
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={srcData}
			/>
		</>
	);
}
