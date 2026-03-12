import { useEffect, useRef, useState } from "react";
import { ButtonWithIcon, WhiteButton } from "../../components/element/Button";
import { FileIcon } from "../../components/element/Icon";
import { useTemplateLoadContent } from "../../hooks/useTemplate";

function TemplatePreviewDialog({
	name,
	handleDialogClose,
}: {
	name: string;
	handleDialogClose: () => void;
}) {
	const [content, setContent] = useState<string | null>(null);
	const loadContent = useTemplateLoadContent();
	const dialogRef = useRef<HTMLDialogElement>(null);

	useEffect(() => {
		dialogRef.current?.showModal();
	}, []);

	useEffect(() => {
		loadContent(name).then(setContent);
	}, [name]);

	return (
		<dialog
			ref={dialogRef}
			onClose={handleDialogClose}
			className="overflow-y-auto fixed top-0 right-0 left-0 z-50 bg-white border border-gray-200"
		>
			<div className="p-4 rounded-lg mt-2">
				<div className="w-[800px]">
					<h2 className="text-lg font-bold mb-2">Template File Preview</h2>
					<p className="text-sm text-gray-500 mb-3 break-all">{name}</p>
					{content === null ? (
						<p className="text-sm text-gray-400 p-3">Loading...</p>
					) : (
						<pre className="text-sm bg-gray-50 border border-gray-300 rounded-lg p-3 overflow-auto max-h-96 whitespace-pre-wrap break-all">
							{content}
						</pre>
					)}
				</div>
			</div>
			<div className="flex items-center justify-end p-4 gap-2">
				<WhiteButton title="Close" handleClick={handleDialogClose} />
			</div>
		</dialog>
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
				<FileIcon title="Preview Template" fill="white" />
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
