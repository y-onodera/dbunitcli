import { useState } from "react";
import {
	DialogBody,
	SettingDialog,
	StatusMessage,
} from "../../../../components/dialog";
import { EditButton } from "../../../../components/element/ButtonIcon";
import { TextArea } from "../../../../components/element/Input";
import {
	useTemplateData,
	useTemplateSaveContent,
} from "../../../../hooks/useTemplate";
import { saveOnSuccess } from "../../../../utils/fetchUtils";

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
		return <StatusMessage kind="loading">Loading...</StatusMessage>;
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
			title="Template File Edit"
			helpCommand="dialogs/template-edit"
			helpLabel="Template File Edit"
			fileName={name}
			handleDialogClose={handleDialogClose}
			handleSave={handleSave}
		>
			<DialogBody size="md">
				<TextArea
					value={content}
					onChange={(e) => setContent(e.target.value)}
				/>
			</DialogBody>
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
