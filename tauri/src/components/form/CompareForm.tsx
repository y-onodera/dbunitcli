import MetadataSettingsProvider from "../../context/MetadataSettingsProvider";
import type { CompareParams } from "../../model/CommandParam";
import CommandFormElements from "./CommandFormElement";
import { DatasetLoadForm } from "./DatasetLoadForm";

export function CompareForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	compare: CompareParams;
}) {
	const imageOption = prop.compare.imageOption;
	const newData = prop.compare.newData;
	const oldData = prop.compare.oldData;
	const expectData = prop.compare.expectData;
	const convertResult = prop.compare.convertResult;
	return (
		<>
			<MetadataSettingsProvider>
				<CommandFormElements
					handleTypeSelect={prop.handleTypeSelect}
					name={prop.name}
					prefix=""
					elements={prop.compare.elements}
				/>
			</MetadataSettingsProvider>
			{prop.compare.imageOption && (
				<CommandFormElements
					handleTypeSelect={prop.handleTypeSelect}
					name={prop.name}
					prefix={imageOption.prefix}
					elements={imageOption.elements}
				/>
			)}
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={newData}
			/>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={oldData}
			/>
			<CommandFormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix={convertResult.prefix}
				elements={convertResult.elements}
			/>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={expectData}
			/>
		</>
	);
}
