import { useMemo } from "react";
import { DatasetSrcInfoProvider } from "../../context/DatasetSrcInfoProvider";
import type { CompareParams } from "../../model/CommandParam";
import CommandFormElements, { buildDatasetSrcInfo } from "./CommandFormElement";
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

	const targetType =
		prop.compare.elements.find((e) => e.name === "targetType")?.value ?? "data";
	const settingIndex = prop.compare.elements.findIndex(
		(e) => e.name === "setting",
	);
	const settingElement =
		settingIndex >= 0 ? prop.compare.elements[settingIndex] : null;
	const elementsBeforeSetting =
		settingIndex >= 0
			? prop.compare.elements.slice(0, settingIndex)
			: prop.compare.elements;
	const elementsAfterSetting =
		settingIndex >= 0 ? prop.compare.elements.slice(settingIndex + 1) : [];

	const oldDataInitialInfo = useMemo(
		() => buildDatasetSrcInfo(oldData.elements),
		[oldData.elements],
	);

	return (
		<>
			<fieldset className="border border-gray-200 p-3">
				<legend>compare</legend>
				<CommandFormElements
					handleTypeSelect={prop.handleTypeSelect}
					name={prop.name}
					prefix=""
					elements={elementsBeforeSetting}
				/>
				{settingElement &&
					(targetType === "data" ? (
						<DatasetSrcInfoProvider
							key={prop.name + "compare-setting"}
							initialValue={oldDataInitialInfo}
						>
							<CommandFormElements
								handleTypeSelect={prop.handleTypeSelect}
								name={prop.name}
								prefix=""
								elements={[settingElement]}
							/>
						</DatasetSrcInfoProvider>
					) : (
						<CommandFormElements
							handleTypeSelect={prop.handleTypeSelect}
							name={prop.name}
							prefix=""
							elements={[settingElement]}
							hideDatasetSettingEdit={true}
						/>
					))}
				<CommandFormElements
					handleTypeSelect={prop.handleTypeSelect}
					name={prop.name}
					prefix=""
					elements={elementsAfterSetting}
				/>
				{prop.compare.imageOption && (
					<CommandFormElements
						handleTypeSelect={prop.handleTypeSelect}
						name={prop.name}
						prefix={imageOption.prefix}
						elements={imageOption.elements}
					/>
				)}
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
