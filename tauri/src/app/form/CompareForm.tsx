import { useMemo } from "react";
import { DatasetSrcInfoProvider } from "../../context/DatasetSrcInfoProvider";
import { buildDatasetSrcInfo } from "../../model/CommandParam";
import type { CompareParams } from "../../model/SelectParameter";
import ConvertResultFormSection from "./section/ConvertResultFormSection";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import ImageOptionFormSection from "./section/ImageOptionFormSection";
import Select from "./section/Select";
import Text from "./section/TextFormElement";

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

	const targetTypeElement = prop.compare.elements.find(
		(e) => e.name === "targetType",
	);
	const targetType = targetTypeElement?.value ?? "data";
	const settingElement =
		prop.compare.elements.find((e) => e.name === "setting") ?? null;
	const settingEncodingElement = prop.compare.elements.find(
		(e) => e.name === "settingEncoding",
	);

	const oldDataInitialInfo = useMemo(
		() => buildDatasetSrcInfo(oldData.elements),
		[oldData.elements],
	);

	return (
		<>
			<fieldset className="border border-gray-200 p-3">
				<legend>compare</legend>
				{targetTypeElement && (
					<Select
						handleTypeSelect={prop.handleTypeSelect}
						prefix=""
						element={targetTypeElement}
					/>
				)}
				{settingElement &&
					(targetType === "data" ? (
						<DatasetSrcInfoProvider
							key={`${prop.name}compare-setting`}
							initialValue={oldDataInitialInfo}
						>
							<Text prefix="" element={settingElement} />
						</DatasetSrcInfoProvider>
					) : (
						<Text
							prefix=""
							element={settingElement}
							hideDatasetSettingEdit={true}
						/>
					))}
				{settingEncodingElement && (
					<Text prefix="" element={settingEncodingElement} />
				)}
			</fieldset>
			{prop.compare.imageOption && (
				<fieldset className="border border-gray-200 p-3">
					<legend>image</legend>
					<ImageOptionFormSection imageOption={imageOption} />
				</fieldset>
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
			<fieldset className="border border-gray-200 p-3">
				<legend>{convertResult.prefix}</legend>
				<ConvertResultFormSection
					convertResult={convertResult}
					handleTypeSelect={prop.handleTypeSelect}
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
