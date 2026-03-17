import { useState } from "react";
import { EditButton } from "../../components/element/ButtonIcon";
import { useDeleteTemplate } from "../../hooks/useTemplate";
import {
	RemoveResource,
	type ResourceEditButtonProp,
} from "./ResourceEditButton";
import TemplateEditDialog from "./TemplateEditDialog";

export default function TemplateEditButton({
	path,
	setPath,
}: {
	path: string;
	setPath?: (path: string) => void;
}) {
	const [showDialog, setShowDialog] = useState(false);
	return (
		<>
			<EditButton handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<TemplateEditDialog
					name={path}
					setPath={setPath}
					handleDialogClose={() => setShowDialog(false)}
				/>
			)}
		</>
	);
}

export function RemoveTemplateButton({
	path,
	setPath,
}: ResourceEditButtonProp) {
	const deleteTemplate = useDeleteTemplate();
	return (
		<RemoveResource
			path={path}
			setPath={setPath}
			deleteResource={deleteTemplate}
		/>
	);
}
