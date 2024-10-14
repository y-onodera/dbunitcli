import type { ConvertParams } from "../../model/CommandParam";
import CommandFormElements from "./CommandFormElement";
import { DatasetLoadForm } from "./DatasetLoadForm";

export function ConvertForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	convert: ConvertParams;
}) {
	const srcData = prop.convert.srcData;
	const convertResult = prop.convert.convertResult;
	return (
		<>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={srcData}
			/>
			<CommandFormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix={convertResult.prefix}
				elements={convertResult.elements}
			/>
			<CommandFormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix={convertResult.prefix}
				elements={convertResult.jdbc ? convertResult.jdbc.elements : []}
			/>
		</>
	);
}
