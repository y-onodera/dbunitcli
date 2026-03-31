import { useMemo } from "react";
import { DatasetSrcInfoProvider } from "../../context/DatasetSrcInfoProvider";
import type { CompareParams } from "../../model/SelectParameter";
import ConvertResultFormSection from "./section/ConvertResultFormSection";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import Select from "./section/element/Select";
import Text from "./section/element/TextFormElement";
import ImageOptionFormSection from "./section/ImageOptionFormSection";

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

	const targetTypeElement = prop.compare.targetType;
	const targetType = targetTypeElement?.value ?? "data";
	const settingElement = prop.compare.setting ?? null;
	const settingEncodingElement = prop.compare.settingEncoding;

	const oldDataInitialInfo = useMemo(
		() => oldData.buildDatasetSrcInfo(),
		[oldData],
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
