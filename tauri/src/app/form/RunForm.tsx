import { JdbcConnectionProvider } from "../../context/JdbcConnectionProvider";
import type { RunParams } from "../../model/CommandParam";
import CommandFormElements from "./CommandFormElement";
import { DatasetLoadForm } from "./DatasetLoadForm";
import JdbcFormSection, { isJdbcField } from "./JdbcFormSection";

export function RunForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	run: RunParams;
}) {
	const srcData = prop.run.srcData;
	const templateOption = prop.run.templateOption;
	const jdbcOption = prop.run.jdbcOption;
	const jdbcOptionElements =
		jdbcOption?.elements.filter((e) => isJdbcField(e.name)) ?? [];
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
				{prop.run.templateOption && (
					<CommandFormElements
						handleTypeSelect={prop.handleTypeSelect}
						name={prop.name}
						prefix={templateOption.prefix}
						elements={templateOption.elements}
					/>
				)}
				{prop.run.jdbcOption && (
					<JdbcConnectionProvider>
						<CommandFormElements
							handleTypeSelect={prop.handleTypeSelect}
							name={prop.name}
							prefix={jdbcOption.prefix}
							elements={jdbcOption.elements}
						/>
						{jdbcOptionElements.length > 0 && (
							<JdbcFormSection
								prefix={jdbcOption.prefix}
								elements={jdbcOptionElements}
							/>
						)}
					</JdbcConnectionProvider>
				)}
			</fieldset>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={srcData}
			/>
		</>
	);
}
