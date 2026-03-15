import { useDeleteXlsxSchema } from "../../hooks/useXlsxSchema";
import type { SrcInfo } from "../form/FormElementProp";
import ResourceEditButton, {
	RemoveResource,
	type ResourceEditButtonProp,
} from "./ResourceEditButton";
import XlsxSchemaDialog from "./XlsxSchemaDialog";

type XlsxSchemaEditButtonProp = ResourceEditButtonProp & {
	srcInfo?: SrcInfo;
};

export default function XlsxSchemaEditButton({
	path,
	setPath,
	srcInfo,
}: XlsxSchemaEditButtonProp) {
	const renderDialog = (open: boolean, closeDialog: () => void) => {
		if (!open) {
			return null;
		}
		return (
			<XlsxSchemaDialog
				fileName={path}
				srcInfo={srcInfo}
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
