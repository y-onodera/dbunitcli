import { useEffect, useState } from "react";
import { SettingDialog } from "../../components/dialog";
import { useTemplateLoadContent, useTemplateSaveContent } from "../../hooks/useTemplate";
import { saveOnSuccess } from "../../utils/fetchUtils";

export default function TemplateEditDialog({
	name,
	setPath,
	handleDialogClose,
}: {
	name: string;
	setPath?: (path: string) => void;
	handleDialogClose: () => void;
}) {
	const [content, setContent] = useState<string | null>(null);
	const loadContent = useTemplateLoadContent();
	const saveContent = useTemplateSaveContent();

	// biome-ignore lint/correctness/useExhaustiveDependencies: loadContent is recreated each render but functionally stable
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
	}, [name]);

	const handleSave = (path: string) =>
		saveOnSuccess(
			() => saveContent(path, content ?? ""),
			() => {
				setPath?.(path);
				handleDialogClose();
			},
		);

	return (
		<SettingDialog
			fileName={name}
			handleDialogClose={handleDialogClose}
			handleSave={handleSave}
			commitDisabled={content === null}
		>
			<div className="w-[800px] p-4">
				<h2 className="text-lg font-bold mb-2">Template File Edit</h2>
				{content === null ? (
					<p className="text-sm text-gray-400 p-3">Loading...</p>
				) : (
					<textarea
						className="text-sm bg-gray-50 border border-gray-300 rounded-lg p-3 w-full h-96 font-mono focus-visible:ring-3 ring-indigo-300"
						value={content}
						onChange={(e) => setContent(e.target.value)}
					/>
				)}
			</div>
		</SettingDialog>
	);
}
