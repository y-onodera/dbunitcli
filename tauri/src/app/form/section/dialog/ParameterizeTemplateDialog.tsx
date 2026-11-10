import { useState } from "react";
import { DialogBody, StatusMessage } from "../../../../components/dialog";
import { TextArea } from "../../../../components/element/Input";
import {
	useResourceContent,
	useSaveResourceContent,
} from "../../../../hooks/useResourceFiles";
import type { EditResourceDialogProps } from "./ResourceEditButton";
import { ResourceSettingDialog } from "./ResourceSettingDialog";

export default function ParameterizeTemplateDialog({
	fileName,
	handleDialogClose,
	handleSave,
}: EditResourceDialogProps) {
	const { content, loading } = useResourceContent(
		"parameterizeTemplate",
		fileName,
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
		<ResourceSettingDialog
			resourceKind={"parameterizeTemplate"}
			maximizable
			title="Edit Parameterize Template"
			helpCommand="dialogs/parameterize-template-edit"
			helpLabel="Edit Parameterize Template"
			fileName={fileName}
			handleDialogClose={handleDialogClose}
			save={(path, overwrite) => saveContent(path, content, overwrite)}
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
		</ResourceSettingDialog>
	);
}
