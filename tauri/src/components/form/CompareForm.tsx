import DatasetSettingsProvider from "../../context/DatasetSettingsProvider";
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
			<fieldset className="border border-gray-200 p-3">
				<legend>compare</legend>
				<DatasetSettingsProvider>
					<CommandFormElements
						handleTypeSelect={prop.handleTypeSelect}
						name={prop.name}
						prefix=""
						elements={prop.compare.elements}
					/>
					{prop.compare.imageOption && (
						<CommandFormElements
							handleTypeSelect={prop.handleTypeSelect}
							name={prop.name}
							prefix={imageOption.prefix}
							elements={imageOption.elements}
						/>
					)}
				</DatasetSettingsProvider>
			</fieldset>
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
			<fieldset className="border border-gray-200 p-3">
				<legend>{convertResult.prefix}</legend>
				<CommandFormElements
					handleTypeSelect={prop.handleTypeSelect}
					name={prop.name}
					prefix={convertResult.prefix}
					elements={convertResult.elements}
				/>
			</fieldset>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={expectData}
			/>
		</>
	);
}
