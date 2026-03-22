import { useState } from "react";
import { ControllTextBox, InputLabel } from "../../../components/element/Input";
import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import type { DatasetSrcInfo } from "../../../model/CommandParam";
import { isSqlRelatedType } from "../../../model/QueryDatasource";
import DatasetFileText from "./DatasetFileText";
import DatasetSettingText from "./DatasetSettingText";
import type { Prop } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";
import SqlSrcText from "./SqlSrcText";
import TemplateText from "./TemplateText";
import XlsxSchemaText from "./XlsxSchemaText";

export default function DatasetText(prop: Prop) {
	if (prop.element.name === "setting") {
		return <DatasetSettingText {...prop} />;
	}
	if (prop.element.name === "xlsxSchema") {
		return <XlsxSchemaText {...prop} />;
	}
	if (prop.element.name === "src" && isSqlRelatedType(prop.srcType ?? "")) {
		return <SqlSrcText {...prop} />;
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

function DatasetPlainText({ prefix, element, hidden }: Prop) {
	const [path, setPath] = useState(element.value);
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();
	const id = getId(prefix, element.name);
	const fieldName = getName(prefix, element.name);

	const handleChange = (ev: React.ChangeEvent<HTMLInputElement>) => {
		const newValue = ev.target.value;
		setPath(newValue);
		if (datasetSrcInfo && element.name in datasetSrcInfo) {
			setDatasetSrcInfo({
				...datasetSrcInfo,
				[element.name]: newValue,
			} as DatasetSrcInfo);
		}
	};

	return (
		<div>
			<InputLabel
				text={fieldName}
				id={id}
				required={element.attribute.required}
				hidden={hidden}
			/>
			<div className="flex">
				<div className="flex-1 mr-36">
					<ControllTextBox
						name={fieldName}
						id={id}
						hidden={hidden}
						required={element.attribute.required}
						value={path}
						handleChange={handleChange}
					/>
				</div>
			</div>
		</div>
	);
}
