import type { RunParams } from "../../model/CommandParam";
import { DatasetLoadForm } from "./DatasetLoadForm";
import FormElements from "./FormElement";

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
			<FormElements
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
				<FormElements
					handleTypeSelect={prop.handleTypeSelect}
					name={prop.name}
					prefix={templateOption.prefix}
					elements={templateOption.elements}
				/>
			)}
			{prop.run.jdbcOption && (
				<FormElements
					handleTypeSelect={prop.handleTypeSelect}
					name={prop.name}
					prefix={jdbcOption.prefix}
					elements={jdbcOption.elements}
				/>
			)}
		</>
	);
}
