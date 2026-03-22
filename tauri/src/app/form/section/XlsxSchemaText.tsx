import { useState } from "react";
import {
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
} from "../../../components/element/Input";
import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import { useResourcesSettings } from "../../../context/WorkspaceResourcesProvider";
import type { DatasetSrcInfo } from "../../../model/CommandParam";
import XlsxSchemaDropDownMenu from "./XlsxSchemaDropDownMenu";
import type { Prop } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

export default function XlsxSchemaText({
	prefix,
	element,
	hidden,
	srcType,
}: Prop) {
	const [path, setPath] = useState(element.value);
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();
	const settings = useResourcesSettings();
	const resourceFiles = settings.xlsxSchemas;
	const isValueInDatalist = resourceFiles.includes(path);
	const id = getId(prefix, element.name);
	const fieldName = getName(prefix, element.name);

	const handleChange = (ev: React.ChangeEvent<HTMLInputElement>) => {
		const newValue = ev.target.value;
		setPath(newValue);
		if (datasetSrcInfo) {
			setDatasetSrcInfo({ ...datasetSrcInfo, xlsxSchema: newValue } as DatasetSrcInfo);
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
				<div className="flex-1">
					<ControllTextBox
						name={fieldName}
						id={id}
						list={`${id}_list`}
						hidden={hidden}
						required={element.attribute.required}
						value={path}
						handleChange={handleChange}
					/>
					{!hidden && (
						<ResourceDatalist
							id={id}
							resources={resourceFiles}
						/>
					)}
				</div>
				{!hidden && (
					<XlsxSchemaDropDownMenu
						path={path}
						setPath={setPath}
						prefix={prefix}
						element={element}
						srcType={srcType}
						isValueInDatalist={isValueInDatalist}
					/>
				)}
			</div>
		</div>
	);
}
