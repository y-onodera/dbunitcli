import type { Dispatch, SetStateAction } from "react";
import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import { useDeleteJdbcProperties } from "../../../../hooks/useJdbc";
import type { CommandParam } from "../../../../model/CommandParam";
import { RemoveResource } from "../../../settings/ResourceEditButton";
import ResourceDropDownMenu from "./ResourceDropDownMenu";
import ResourceText from "./ResourceText";

export default function JdbcPropertiesTextField({
	prefix,
	element,
	onValueChange,
}: {
	prefix: string;
	element: CommandParam;
	onValueChange: (name: string, value: string) => void;
}) {
	const settings = useResourcesSettings();
	return (
		<ResourceText
			prefix={prefix}
			element={element}
			resourceFiles={settings.jdbcFiles}
			handleValueChange={(value) => onValueChange(element.name, value)}
		>
			{({ path, setPath, isValueInDatalist }) => {
				const wrappedSetPath: Dispatch<SetStateAction<string>> = (action) => {
					const newPath = typeof action === "function" ? action(path) : action;
					setPath(newPath);
					onValueChange(element.name, newPath);
				};
				return (
					<ResourceDropDownMenu
						prefix={prefix}
						element={element}
						path={path}
						setPath={wrappedSetPath}
						isValueInDatalist={isValueInDatalist}
						removeButton={(closeMenu) => (
							<RemoveJdbcPropertiesButton
								path={path}
								setPath={(value) => {
									wrappedSetPath(value);
									closeMenu();
								}}
							/>
						)}
						className="mr-24"
					/>
				);
			}}
		</ResourceText>
	);
}

function RemoveJdbcPropertiesButton({
	path,
	setPath,
}: {
	path: string;
	setPath: (value: string) => void;
}) {
	const deleteJdbcProperties = useDeleteJdbcProperties();
	return (
		<RemoveResource
			path={path}
			setPath={setPath}
			deleteResource={deleteJdbcProperties}
		/>
	);
}
