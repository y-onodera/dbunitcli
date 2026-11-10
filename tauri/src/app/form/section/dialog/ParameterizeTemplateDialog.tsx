import { useState } from "react";
import {
	DialogBody,
	SettingDialog,
	StatusMessage,
} from "../../../../components/dialog";
import { TextArea } from "../../../../components/element/Input";
import {
	useResourceContent,
	useResourceLocation,
	useSaveResourceContent,
} from "../../../../hooks/useResourceFiles";
import type { EditResourceDialogProps } from "./ResourceEditButton";

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
	const { location, loading: locationLoading } = useResourceLocation(
		"parameterizeTemplate",
		fileName,
	);

	return (
		<SettingDialog
			maximizable
			title="Edit Parameterize Template"
			helpCommand="dialogs/parameterize-template-edit"
			helpLabel="Edit Parameterize Template"
			fileName={fileName}
			handleDialogClose={handleDialogClose}
			save={(path, overwrite) => saveContent(path, content, overwrite)}
			location={location}
			locationLoading={locationLoading}
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
