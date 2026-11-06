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
import type {
	EditResourceDialogProps,
	EncodingProp,
} from "./ResourceEditButton";

export default function TemplateEditDialog({
	fileName,
	handleDialogClose,
	handleSave,
	encoding,
}: EditResourceDialogProps & EncodingProp) {
	const { content, loading } = useResourceContent(
		"template",
		fileName,
		encoding,
	);
	if (loading) {
		return <StatusMessage kind="loading">Loading...</StatusMessage>;
	}
	return (
		<Dialog
			content={content}
			fileName={fileName}
			handleDialogClose={handleDialogClose}
			handleSave={handleSave}
			encoding={encoding}
		/>
	);
}

function Dialog({
	content: initialContent,
	fileName,
	handleDialogClose,
	handleSave,
	encoding,
}: EditResourceDialogProps & EncodingProp & { content: string }) {
	const [content, setContent] = useState<string>(initialContent);
	const saveContent = useSaveResourceContent("template", encoding);

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
