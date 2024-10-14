import type { RunParams } from "../../model/CommandParam";
import CommandFormElements from "./CommandFormElement";
import { DatasetLoadForm } from "./DatasetLoadForm";

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
			<CommandFormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix=""
				elements={prop.run.elements}
			/>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={srcData}
			/>
			{prop.run.jdbcOption && (
				<CommandFormElements
					handleTypeSelect={prop.handleTypeSelect}
					name={prop.name}
					prefix={templateOption.prefix}
					elements={templateOption.elements}
				/>
			)}
			{prop.run.jdbcOption && (
				<CommandFormElements
					handleTypeSelect={prop.handleTypeSelect}
					name={prop.name}
					prefix={jdbcOption.prefix}
					elements={jdbcOption.elements}
				/>
			)}
		</>
	);
}
