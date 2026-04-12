import { useDeleteDatasetSettings } from "../../../../hooks/useDatasetSettings";
import DatasetSettingsDialog from "./DatasetSettingsDialog";
import ResourceEditButton, {
	RemoveResource,
	type ResourceEditButtonProp,
} from "./ResourceEditButton";

export default function DatasetSettingEditButton({
	path,
	setPath,
}: ResourceEditButtonProp) {
	const renderDialog = (open: boolean, closeDialog: () => void) => {
		if (!open) {
			return null;
		}
		return (
			<DatasetSettingsDialog
				fileName={path}
				handleDialogClose={closeDialog}
				handleSave={(newPath: string) => {
					setPath(newPath);
					closeDialog();
				}}
			/>
		);
	};

	return <ResourceEditButton renderDialog={renderDialog} />;
}
export function RemoveDatasetSettingButton({
	path,
	setPath,
}: ResourceEditButtonProp) {
	const deleteSettings = useDeleteDatasetSettings();
	return (
		<RemoveResource
			path={path}
			setPath={setPath}
			deleteResource={deleteSettings}
		/>
	);
}
