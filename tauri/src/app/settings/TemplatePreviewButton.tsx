import { useEffect, useState } from "react";
import { ButtonWithIcon } from "../../components/element/Button";
import { FileIcon } from "../../components/element/Icon";
import { SettingDialog } from "../../components/dialog/SettingDialog";
import { useTemplateLoadContent, useTemplateSaveContent } from "../../hooks/useTemplate";

function TemplatePreviewDialog({
	name,
	handleDialogClose,
}: {
	name: string;
	handleDialogClose: () => void;
}) {
	const [content, setContent] = useState<string | null>(null);
	const loadContent = useTemplateLoadContent();
	const saveContent = useTemplateSaveContent();

	useEffect(() => {
		loadContent(name).then(setContent);
	}, [name]);

	const handleCommit = async (newContent: string) => {
		await saveContent(name, newContent);
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
				<p className="text-sm text-gray-500 mb-3 break-all">{name}</p>
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

export default function TemplatePreviewButton({ path }: { path: string }) {
	const [showDialog, setShowDialog] = useState(false);
	return (
		<>
			<ButtonWithIcon
				handleClick={() => setShowDialog(true)}
				id="templatePreviewButton"
			>
				<FileIcon title="Edit Template" fill="white" />
			</ButtonWithIcon>
			{showDialog && (
				<TemplatePreviewDialog
					name={path}
					handleDialogClose={() => setShowDialog(false)}
				/>
			)}
		</>
	);
}
