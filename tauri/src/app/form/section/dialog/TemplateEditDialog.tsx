import { useState } from "react";
import { DialogBody } from "../../../../components/dialog";
import { TextArea } from "../../../../components/element/Input";
import {
	CONTENT_PARSER,
	useSaveResourceContent,
} from "../../../../hooks/useResourceFiles";
import type { EditResourceDialogProps } from "./ResourceEditButton";
import { ResourceLoader } from "./ResourceLoader";
import { ResourceSettingDialog } from "./ResourceSettingDialog";

export default function TemplateEditDialog({
	fileName,
	handleDialogClose,
	handleSave,
}: EditResourceDialogProps) {
	return (
		<ResourceLoader
			kind={"template"}
			fileName={fileName}
			parser={CONTENT_PARSER}
		>
			{(content) => (
				<Dialog
					content={content}
					fileName={fileName}
					handleDialogClose={handleDialogClose}
					handleSave={handleSave}
				/>
			)}
		</ResourceLoader>
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
		<ResourceSettingDialog
			resourceKind={"template"}
			maximizable
			title="Template File Edit"
			helpCommand="dialogs/template-edit"
			helpLabel="Template File Edit"
			fileName={fileName}
			handleDialogClose={handleDialogClose}
			save={(path, overwrite) => saveContent(path, content, overwrite)}
			handleSave={handleSave}
		>
			<DialogBody size="md">
				<TextArea
					value={content}
					onChange={(e) => setContent(e.target.value)}
				/>
			</DialogBody>
		</ResourceSettingDialog>
	);
}
