import { useEffect, useState } from "react";
import { EditButton } from "../../components/element/ButtonIcon";
import { SettingDialog } from "../../components/dialog/SettingDialog";
import { useTemplateLoadContent, useTemplateSaveContent, useDeleteTemplate } from "../../hooks/useTemplate";
import { RemoveResource, type ResourceEditButtonProp } from "./ResourceEditButton";

function TemplatePreviewDialog({
	name,
	setPath,
	handleDialogClose,
}: {
	name: string;
	setPath?: (path: string) => void;
	handleDialogClose: () => void;
}) {
	const [fileNameInput, setFileNameInput] = useState("");
	const [content, setContent] = useState<string | null>(null);
	const loadContent = useTemplateLoadContent();
	const saveContent = useTemplateSaveContent();

	useEffect(() => {
		if (!name) {
			setContent("");
			return;
		}
		let cancelled = false;
		loadContent(name).then((result) => {
			if (!cancelled) {
				setContent(result);
			}
		});
		return () => {
			cancelled = true;
		};
	}, [name, loadContent]);

	const handleCommit = async (newContent: string) => {
		const effectiveName = name || fileNameInput;
		await saveContent(effectiveName, newContent);
		setPath?.(effectiveName);
		handleDialogClose();
	};

	return (
		<SettingDialog
			setting={content ?? ""}
			handleDialogClose={handleDialogClose}
			handleCommit={handleCommit}
			commitLabel="Save"
		>
			<div className="w-[800px]">
				<h2 className="text-lg font-bold mb-2">Template File Edit</h2>
				{name ? (
					<p className="text-sm text-gray-500 mb-3 break-all">{name}</p>
				) : (
					<input
						className="text-sm bg-gray-50 border border-gray-300 rounded-lg p-2 w-full mb-3 focus-visible:ring-3 ring-indigo-300"
						placeholder="File name"
						value={fileNameInput}
						onChange={(e) => setFileNameInput(e.target.value)}
					/>
				)}
				{content === null ? (
					<p className="text-sm text-gray-400 p-3">Loading...</p>
				) : (
					<textarea
						className="text-sm bg-gray-50 border border-gray-300 rounded-lg p-3 w-full h-96 font-mono"
						value={content}
						onChange={(e) => setContent(e.target.value)}
					/>
				)}
			</div>
		</SettingDialog>
	);
}

export default function TemplatePreviewButton({
	path,
	setPath,
}: {
	path: string;
	setPath?: (path: string) => void;
}) {
	const [showDialog, setShowDialog] = useState(false);
	return (
		<>
			<EditButton handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<TemplatePreviewDialog
					name={path}
					setPath={setPath}
					handleDialogClose={() => setShowDialog(false)}
				/>
			)}
		</>
	);
}

export function RemoveTemplateButton({ path, setPath }: ResourceEditButtonProp) {
	const deleteTemplate = useDeleteTemplate();
	return <RemoveResource path={path} setPath={setPath} deleteResource={deleteTemplate} />;
}
