import { useState } from "react";
import { ControllTextBox, InputLabel } from "../../../components/element/Input";
import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import { useWorkspaceContext } from "../../../context/WorkspaceResourcesProvider";
import type { DatasetSrcInfo } from "../../../model/CommandParam";
import { getPath } from "./Chooser";
import FileDropDownMenu from "./FileDropDownMenu";
import type { Prop } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

export default function FileText({ prefix, element, hidden, srcType }: Prop) {
	const [path, setPath] = useState(element.value);
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();
	const context = useWorkspaceContext();
	const defaultPath = getPath(context, element.attribute.defaultPath, srcType);
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
				<div className="flex-1">
					<ControllTextBox
						name={fieldName}
						id={id}
						hidden={hidden}
						required={element.attribute.required}
						value={path}
						handleChange={handleChange}
					/>
					{!hidden && defaultPath && (
						<p className="text-xs text-gray-400 truncate">{defaultPath}</p>
					)}
				</div>
				{!hidden && (
					<FileDropDownMenu
						path={path}
						setPath={setPath}
						prefix={prefix}
						element={element}
						srcType={srcType}
					/>
				)}
			</div>
		</div>
	);
}
