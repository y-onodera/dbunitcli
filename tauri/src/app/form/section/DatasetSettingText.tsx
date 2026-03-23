import { useState } from "react";
import {
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
} from "../../../components/element/Input";
import { useDatasetSrcInfo } from "../../../context/DatasetSrcInfoProvider";
import { useResourcesSettings } from "../../../context/WorkspaceResourcesProvider";
import DatasetSettingEditButton, {
	RemoveDatasetSettingButton,
} from "../../settings/DatasetSettingEditButton";
import DatasetTableNamesPreviewButton from "../../settings/DatasetTableNamesPreviewButton";
import type { Prop } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";
import ResourceDropDownMenu from "./ResourceDropDownMenu";

export default function DatasetSettingText({
	prefix,
	element,
	hidden,
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
						{!hidden && <ResourceDatalist id={id} resources={resourceFiles} />}
					</div>
					{!hidden && (
						<ResourceDropDownMenu
							path={path}
							setPath={setPath}
							prefix={prefix}
							element={element}
							isValueInDatalist={isValueInDatalist}
							editButtons={
								!hideDatasetSettingEdit
									? [
											<DatasetSettingEditButton
												key="edit"
												path={path}
												setPath={setPath}
											/>,
										]
									: undefined
							}
							removeButton={() => (
								<RemoveDatasetSettingButton path={path} setPath={setPath} />
							)}
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
