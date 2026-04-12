import { useDeleteXlsxSchema } from "../../../../hooks/useXlsxSchema";
import ResourceEditButton, {
	RemoveResource,
	type ResourceEditButtonProp,
} from "./ResourceEditButton";
import XlsxSchemaDialog from "./XlsxSchemaDialog";

export default function XlsxSchemaEditButton({
	path,
	setPath,
}: ResourceEditButtonProp) {
	const renderDialog = (open: boolean, closeDialog: () => void) => {
		if (!open) {
			return null;
		}
		return (
			<XlsxSchemaDialog
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
export function RemoveXlsxSchemaButton({
	path,
	setPath,
}: ResourceEditButtonProp) {
	const deleteSchema = useDeleteXlsxSchema();

	return (
		<RemoveResource
			path={path}
			setPath={setPath}
			deleteResource={deleteSchema}
		/>
	);
}
