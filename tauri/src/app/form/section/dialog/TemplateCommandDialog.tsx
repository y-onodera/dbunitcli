import { useState } from "react";
import {
	DialogBody,
	SettingDialog,
	StatusMessage,
} from "../../../../components/dialog";
import { EditButton } from "../../../../components/element/ButtonIcon";
import { TextArea } from "../../../../components/element/Input";
import {
	useParameterizeTemplateData,
	useSaveParameterizeTemplate,
} from "../../../../hooks/useTemplate";
import { saveOnSuccess } from "../../../../utils/fetchUtils";

function extractFileNameFromTemplatePath(templatePath: string): string {
	return templatePath.replace(/\\/g, "/").split("/").pop() ?? templatePath;
}

function ParameterizeTemplateDialog({
	name,
	loadName,
	setPath,
	handleDialogClose,
}: {
	name: string;
	loadName: string;
	setPath: (path: string) => void;
	handleDialogClose: () => void;
}) {
	const { content, loading } = useParameterizeTemplateData(loadName);
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
	setPath: (path: string) => void;
	handleDialogClose: () => void;
}) {
	const [content, setContent] = useState<string>(initialContent);
	const saveContent = useSaveParameterizeTemplate();

	const handleSave = (path: string) =>
		saveOnSuccess(
			() => saveContent(path, content),
			() => {
				setPath(path);
				handleDialogClose();
			},
		);

	return (
		<SettingDialog
			title="Edit Parameterize Template"
			helpCommand="dialogs/parameterize-template-edit"
			helpLabel="Edit Parameterize Template"
			fileName={name}
			handleDialogClose={handleDialogClose}
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

export function TemplateCommandButton({
	name,
	setPath,
}: {
	name: string;
	setPath: (path: string) => void;
}) {
	const [showDialog, setShowDialog] = useState(false);
	const fileName = extractFileNameFromTemplatePath(name);
	return (
		<>
			<EditButton handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<ParameterizeTemplateDialog
					name={fileName}
					loadName={fileName}
					setPath={setPath}
					handleDialogClose={() => setShowDialog(false)}
				/>
			)}
		</>
	);
}
