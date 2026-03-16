import { useDeleteDatasetSettings } from "../../hooks/useDatasetSettings";
import type { DatasetSrcInfo } from "../../model/CommandParam";
import DatasetSettingsDialog from "./DatasetSettingsDialog";
import ResourceEditButton, {
	RemoveResource,
	type ResourceEditButtonProp,
} from "./ResourceEditButton";

type DatasetSettingEditButtonProp = ResourceEditButtonProp & {
	datasetSrcInfo?: DatasetSrcInfo;
};

export default function DatasetSettingEditButton({
	path,
	setPath,
	datasetSrcInfo,
}: DatasetSettingEditButtonProp) {
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
				datasetSrcInfo={datasetSrcInfo}
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
