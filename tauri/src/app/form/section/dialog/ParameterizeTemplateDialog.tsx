import { useState } from "react";
import { DialogBody, SettingDialog } from "../../../../components/dialog";
import { TextArea } from "../../../../components/element/Input";
import {
	CONTENT_PARSER,
	useSaveResourceContent,
} from "../../../../hooks/useResourceFiles";
import type {
	EditResourceDialogProps,
	LoadedResourceDialogProps,
} from "./ResourceEditButton";
import { ResourceLoader } from "./ResourceLoader";

export default function ParameterizeTemplateDialog({
	fileName,
	handleDialogClose,
	handleSave,
}: EditResourceDialogProps) {
	return (
		<ResourceLoader
			kind={"parameterizeTemplate"}
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
}: LoadedResourceDialogProps & { content: string }) {
	const [content, setContent] = useState<string>(initialContent);
	const saveContent = useSaveResourceContent("parameterizeTemplate");

	return (
		<SettingDialog
			location={location}
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
		</SettingDialog>
	);
}
