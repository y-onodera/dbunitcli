import { SectionFieldset } from "../../components/dialog";
import { SectionLegend } from "../../components/dialog/SectionFieldset";
import type { ConvertOptions } from "../../model/SelectParameter";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import ResultFormSection from "./section/ResultFormSection";

export function ConvertForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	convert: ConvertOptions;
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
			<SectionFieldset>
				<SectionLegend
					title={convertResult.prefix}
					command="convert"
					label="Convert"
				/>
				<ResultFormSection
					resultOption={convertResult}
					handleTypeSelect={prop.handleTypeSelect}
				/>
			</SectionFieldset>
		</>
	);
}
