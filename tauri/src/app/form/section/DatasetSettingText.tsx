import { useState } from "react";
import {
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
} from "../../../components/element/Input";
import { useDatasetSrcInfo } from "../../../context/DatasetSrcInfoProvider";
import { useResourcesSettings } from "../../../context/WorkspaceResourcesProvider";
import DatasetTableNamesPreviewButton from "../../settings/DatasetTableNamesPreviewButton";
import DatasetSettingDropDownMenu from "./DatasetSettingDropDownMenu";
import type { Prop } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

export default function DatasetSettingText({
	prefix,
	element,
	hidden,
	srcType,
	hideDatasetSettingEdit,
}: Prop) {
	const [path, setPath] = useState(element.value);
	const datasetSrcInfo = useDatasetSrcInfo();
	const settings = useResourcesSettings();
	const resourceFiles = settings.datasetSettings;
	const isValueInDatalist = resourceFiles.includes(path);
	const id = getId(prefix, element.name);
	const fieldName = getName(prefix, element.name);

	const handleChange = (ev: React.ChangeEvent<HTMLInputElement>) => {
		setPath(ev.target.value);
	};

	return (
		<>
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
						<DatasetSettingDropDownMenu
							path={path}
							setPath={setPath}
							prefix={prefix}
							element={element}
							srcType={srcType}
							isValueInDatalist={isValueInDatalist}
							hideDatasetSettingEdit={hideDatasetSettingEdit}
						/>
					)}
				</div>
			</div>
			{datasetSrcInfo?.srcType && !hideDatasetSettingEdit && (
				<div className="mt-2 flex items-center gap-3">
					<DatasetTableNamesPreviewButton title="Preview Before Settings" />
					{path && (
						<DatasetTableNamesPreviewButton
							title="Preview Aply Settings"
							setting={path}
						/>
					)}
				</div>
			)}
		</>
	);
}
