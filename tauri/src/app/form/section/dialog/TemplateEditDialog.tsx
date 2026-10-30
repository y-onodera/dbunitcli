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
	handleSave,
}: EditResourceDialogProps & { content: string }) {
	const [content, setContent] = useState<string>(initialContent);
	const saveContent = useSaveResourceContent("template");

	return (
		<SettingDialog
			title="Template File Edit"
			helpCommand="dialogs/template-edit"
			helpLabel="Template File Edit"
			fileName={fileName}
			handleDialogClose={handleDialogClose}
			save={(path) => saveContent(path, content)}
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
