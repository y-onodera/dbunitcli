import { useState } from "react";
import {
	DialogBody,
	SettingDialog,
	StatusMessage,
} from "../../../../components/dialog";
import { TextArea } from "../../../../components/element/Input";
import {
	useResourceContent,
	useSaveResourceContent,
} from "../../../../hooks/useResourceFiles";
import { saveOnSuccess } from "../../../../utils/fetchUtils";
import type { EditResourceDialogProps } from "./ResourceEditButton";

export default function TemplateEditDialog({
	fileName,
	handleDialogClose,
	handleSave,
}: EditResourceDialogProps) {
	const { content, loading } = useResourceContent("template", fileName);
	if (loading) {
		return <StatusMessage kind="loading">Loading...</StatusMessage>;
	}
	return (
		<Dialog
			content={content}
			fileName={fileName}
			handleDialogClose={handleDialogClose}
			handleSave={handleSave}
		/>
	);
}

function Dialog({
	content: initialContent,
	fileName,
	handleDialogClose,
	handleSave: onSaved,
}: EditResourceDialogProps & { content: string }) {
	const [content, setContent] = useState<string>(initialContent);
	const saveContent = useSaveResourceContent("template");

	const handleSave = (path: string) =>
		saveOnSuccess(
			() => saveContent(path, content),
			() => onSaved(path),
		);

	return (
		<SettingDialog
			title="Template File Edit"
			helpCommand="dialogs/template-edit"
			helpLabel="Template File Edit"
			fileName={fileName}
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
