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

function extractFileNameFromTemplatePath(templatePath: string): string {
	return templatePath.replace(/\\/g, "/").split("/").pop() ?? templatePath;
}

export default function ParameterizeTemplateDialog({
	fileName,
	handleDialogClose,
	handleSave,
}: EditResourceDialogProps) {
	const name = extractFileNameFromTemplatePath(fileName);
	const { content, loading } = useResourceContent("parameterizeTemplate", name);
	if (loading) {
		return <StatusMessage kind="loading">Loading...</StatusMessage>;
	}
	return (
		<Dialog
			content={content}
			fileName={name}
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
	const saveContent = useSaveResourceContent("parameterizeTemplate");

	return (
		<SettingDialog
			title="Edit Parameterize Template"
			helpCommand="dialogs/parameterize-template-edit"
			helpLabel="Edit Parameterize Template"
			fileName={fileName}
			handleDialogClose={handleDialogClose}
			save={(path) => saveContent(path, content)}
			handleSave={handleSave}
		>
			<DialogBody size="md">
				<TextArea
					id="contents"
					value={content}
					onChange={(e) => setContent(e.target.value)}
					placeholder="Enter template..."
					spellCheck={false}
				/>
			</DialogBody>
		</SettingDialog>
	);
}
