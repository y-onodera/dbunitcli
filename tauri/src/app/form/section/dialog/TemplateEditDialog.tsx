import { useState } from "react";
import { DialogTitle, SettingDialog } from "../../../../components/dialog";
import { EditButton } from "../../../../components/element/ButtonIcon";
import { TextArea } from "../../../../components/element/Input";
import {
	useDeleteTemplate,
	useTemplateData,
	useTemplateSaveContent,
} from "../../../../hooks/useTemplate";
import { saveOnSuccess } from "../../../../utils/fetchUtils";
import {
	RemoveResource,
	type ResourceEditButtonProp,
} from "./ResourceEditButton";

export default function TemplateEditDialog({
	name,
	setPath,
	handleDialogClose,
}: {
	name: string;
	setPath?: (path: string) => void;
	handleDialogClose: () => void;
}) {
	const { content, loading } = useTemplateData(name);
	if (loading) {
		return <div>Loading...</div>;
	}
	return (
		<Dialog
			content={content}
			name={name}
			setPath={setPath}
			handleDialogClose={handleDialogClose}
		/>
	);
}

function Dialog({
	content: initialContent,
	name,
	setPath,
	handleDialogClose,
}: {
	content: string;
	name: string;
	setPath?: (path: string) => void;
	handleDialogClose: () => void;
}) {
	const [content, setContent] = useState<string>(initialContent);
	const saveContent = useTemplateSaveContent();

	const handleSave = (path: string) =>
		saveOnSuccess(
			() => saveContent(path, content),
			() => {
				setPath?.(path);
				handleDialogClose();
			},
		);

	return (
		<SettingDialog
			fileName={name}
			handleDialogClose={handleDialogClose}
			handleSave={handleSave}
		>
			<div className="w-[800px] p-4">
				<DialogTitle>Template File Edit</DialogTitle>
				<TextArea
					value={content}
					onChange={(e) => setContent(e.target.value)}
				/>
			</div>
		</SettingDialog>
	);
}
export function TemplateEditButton({
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
