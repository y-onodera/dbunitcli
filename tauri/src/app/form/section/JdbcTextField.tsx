import type { Dispatch, SetStateAction } from "react";
import { useState } from "react";
import { BlueEditButton } from "../../../components/element/ButtonIcon";
import { useResourcesSettings } from "../../../context/WorkspaceResourcesProvider";
import { useDeleteJdbcProperties } from "../../../hooks/useJdbc";
import type { CommandParam } from "../../../model/CommandParam";
import JdbcUrlBuilderDialog from "../../settings/JdbcUrlBuilderDialog";
import { RemoveResource } from "../../settings/ResourceEditButton";
import ResourceDropDownMenu from "./ResourceDropDownMenu";
import ResourceText from "./ResourceText";

export default function JdbcTextField({
	prefix,
	element,
	onValueChange,
}: {
	prefix: string;
	element: CommandParam;
	onValueChange: (name: string, value: string) => void;
}) {
	const settings = useResourcesSettings();
	const isJdbcUrl = element.name === "jdbcUrl";
	const isJdbcProperties = element.name === "jdbcProperties";
	const resourceFiles = isJdbcProperties ? settings.jdbcFiles : [];

	const renderMenu = ({
		path,
		setPath,
		isValueInDatalist,
	}: {
		path: string;
		setPath: Dispatch<SetStateAction<string>>;
		isValueInDatalist: boolean;
	}) => {
		const wrappedSetPath: Dispatch<SetStateAction<string>> = (action) => {
			const newPath = typeof action === "function" ? action(path) : action;
			setPath(newPath);
			onValueChange(element.name, newPath);
		};

		if (isJdbcProperties) {
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
		}
		if (isJdbcUrl) {
			return <JdbcUrlBuilderButton path={path} setPath={wrappedSetPath} />;
		}
		return <div className="w-36" />;
	};

	return (
		<ResourceText
			prefix={prefix}
			element={element}
			resourceFiles={resourceFiles}
			onValueChange={(value) => onValueChange(element.name, value)}
		>
			{renderMenu}
		</ResourceText>
	);
}

function JdbcUrlBuilderButton({
	path,
	setPath,
}: {
	path: string;
	setPath: Dispatch<SetStateAction<string>>;
}) {
	const [showDialog, setShowDialog] = useState(false);
	return (
		<>
			<BlueEditButton handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<JdbcUrlBuilderDialog
					currentUrl={path}
					handleDialogClose={() => setShowDialog(false)}
					handleSave={(url) => {
						setPath(url);
						setShowDialog(false);
					}}
				/>
			)}
		</>
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
