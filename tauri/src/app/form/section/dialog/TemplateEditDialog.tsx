import { useState } from "react";
import { DialogBody, SettingDialog } from "../../../../components/dialog";
import { TextArea } from "../../../../components/element/Input";
import {
	CONTENT_PARSER,
	useSaveResourceContent,
} from "../../../../hooks/useResourceFiles";
import type { ResourceLocation } from "../../../../model/ResourceLocation";
import type { EditResourceDialogProps } from "./ResourceEditButton";
import { ResourceLoader } from "./ResourceLoader";

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
			{(content, location) => (
				<Dialog
					content={content}
					location={location}
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
	location,
	fileName,
	handleDialogClose,
	handleSave,
}: EditResourceDialogProps & {
	content: string;
	location: ResourceLocation | null;
}) {
	const [content, setContent] = useState<string>(initialContent);
	const saveContent = useSaveResourceContent("template");

	return (
		<SettingDialog
			location={location}
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
		</SettingDialog>
	);
}
