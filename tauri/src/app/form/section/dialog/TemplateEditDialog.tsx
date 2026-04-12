import { Suspense, use, useState } from "react";
import { SettingDialog } from "../../../../components/dialog";
import { EditButton } from "../../../../components/element/ButtonIcon";
import {
	useDeleteTemplate,
	useTemplateLoadContent,
	useTemplateSaveContent,
} from "../../../../hooks/useTemplate";
import { saveOnSuccess } from "../../../../utils/fetchUtils";
import {
	RemoveResource,
	type ResourceEditButtonProp,
} from "./ResourceEditButton";

export default function TemplateEditDialog({
	name,
	setPath,
	handleDialogClose,
}: {
	name: string;
	setPath?: (path: string) => void;
	handleDialogClose: () => void;
}) {
	const loadContent = useTemplateLoadContent();
	const promise = name ? loadContent(name) : Promise.resolve("");
	return (
		<Suspense fallback={<div>Loading...</div>}>
			<Dialog
				promise={promise}
				name={name}
				setPath={setPath}
				handleDialogClose={handleDialogClose}
			/>
		</Suspense>
	);
}

function Dialog({
	promise,
	name,
	setPath,
	handleDialogClose,
}: {
	promise: Promise<string>;
	name: string;
	setPath?: (path: string) => void;
	handleDialogClose: () => void;
}) {
	const initialContent = use(promise);
	const [content, setContent] = useState<string>(initialContent);
	const saveContent = useTemplateSaveContent();

	const handleSave = (path: string) =>
		saveOnSuccess(
			() => saveContent(path, content),
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
		>
			<div className="w-[800px] p-4">
				<h2 className="text-lg font-bold mb-2">Template File Edit</h2>
				<textarea
					className="text-sm bg-gray-50 border border-gray-300 rounded-lg p-3 w-full h-96 font-mono focus-visible:ring-3 ring-indigo-300"
					value={content}
					onChange={(e) => setContent(e.target.value)}
				/>
			</div>
		</SettingDialog>
	);
}
export function TemplateEditButton({
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
				<TemplateEditDialog
					name={path}
					setPath={setPath}
					handleDialogClose={() => setShowDialog(false)}
				/>
			)}
		</>
	);
}
export function RemoveTemplateButton({
	path,
	setPath,
}: ResourceEditButtonProp) {
	const deleteTemplate = useDeleteTemplate();
	return (
		<RemoveResource
			path={path}
			setPath={setPath}
			deleteResource={deleteTemplate}
		/>
	);
}
