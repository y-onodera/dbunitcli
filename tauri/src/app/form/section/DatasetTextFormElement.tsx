import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import type { DatasetSrcInfo } from "../../../model/CommandParam";
import DatasetFileText from "./DatasetFileText";
import type { Prop } from "./FormElementProp";
import ResourceText from "./ResourceText";
import TemplateText from "./TemplateText";
import XlsxSchemaText from "./XlsxSchemaText";

export default function DatasetText(prop: Prop) {
	if (prop.element.name === "xlsxSchema") {
		return <XlsxSchemaText {...prop} />;
	}
	if (prop.element.name === "templateGroup") {
		return <TemplateText {...prop} />;
	}
	if (
		prop.element.attribute.type.includes("FILE") ||
		prop.element.attribute.type.includes("DIR")
	) {
		return <DatasetFileText {...prop} />;
	}
	return <DatasetPlainText {...prop} />;
}

export function DatasetPlainText({ prefix, element, hidden }: Prop) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();

	const handleValueChange = (newValue: string) => {
		if (datasetSrcInfo && element.name in datasetSrcInfo) {
			setDatasetSrcInfo({
				...datasetSrcInfo,
				[element.name]: newValue,
			} as DatasetSrcInfo);
		}
	};

	return (
		<ResourceText
			prefix={prefix}
			element={element}
			hidden={hidden}
			onValueChange={handleValueChange}
		/>
	);
}
