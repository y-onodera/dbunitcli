import { JdbcConnectionProvider } from "../../context/JdbcConnectionProvider";
import type { RunParams } from "../../model/SelectParameter";
import CommandFormElements from "./section/CommandFormElement";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
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
	return (
		<>
			<fieldset className="border border-gray-200 p-3">
				<legend>run</legend>
				<CommandFormElements
					handleTypeSelect={prop.handleTypeSelect}
					name={prop.name}
					prefix=""
					elements={prop.run.elements}
				/>
			</fieldset>
			{prop.run.templateOption && (
				<fieldset className="border border-gray-200 p-3">
					<legend>template</legend>
					<TemplateFormSection
						commandParams={templateOption}
						handleTypeSelect={prop.handleTypeSelect}
						name={prop.name}
					/>
				</fieldset>
			)}
			{prop.run.jdbcOption && (
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
