import { useState } from "react";
import {
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
} from "../../../components/element/Input";
import { useResourcesSettings } from "../../../context/WorkspaceResourcesProvider";
import TemplateDropDownMenu from "./TemplateDropDownMenu";
import type { Prop } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

export default function TemplateText({
	prefix,
	element,
	hidden,
	srcType,
}: Prop) {
	const [path, setPath] = useState(element.value);
	const settings = useResourcesSettings();
	const resourceFiles = settings.templateFiles;
	const isValueInDatalist = resourceFiles.includes(path);
	const id = getId(prefix, element.name);
	const fieldName = getName(prefix, element.name);

	const handleChange = (ev: React.ChangeEvent<HTMLInputElement>) => {
		setPath(ev.target.value);
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
					<TemplateDropDownMenu
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
