import { useEffect, useRef, useState } from "react";
import { DirectoryButton } from "../../components/element/ButtonIcon";
import { useSelectParameter } from "../../context/SelectParameterProvider";
import StartupForm from "../startup/StartupForm";

export default function Header() {
	const selected = useSelectParameter();
	const [showWorkspaceDialog, setShowWorkspaceDialog] = useState(false);
	return (
		<div className="px-3 py-3 lg:px-5 lg:pl-3">
			<div className="flex items-center justify-between">
				<div className="flex items-center justify-start rtl:justify-end gap-2">
					<h1>DBunit CLI</h1>
					<DirectoryButton
						title="ChangeWorkspace"
						handleClick={() => setShowWorkspaceDialog(true)}
					/>
				</div>
				{selected.name && <h1>{`${selected.command}: ${selected.name}`}</h1>}
			</div>
			{showWorkspaceDialog && (
				<WorkspaceDialog onClose={() => setShowWorkspaceDialog(false)} />
			)}
		</div>
	);
}

function WorkspaceDialog({ onClose }: { onClose: () => void }) {
	const dialogRef = useRef<HTMLDialogElement>(null);
	useEffect(() => {
		dialogRef.current?.showModal();
	}, []);
	return (
		<dialog
			ref={dialogRef}
			onClose={onClose}
			className="overflow-y-auto fixed top-0 right-0 left-0 z-50 bg-white border border-gray-200 w-full max-w-4xl"
		>
			<div className="p-4 rounded-lg mt-2">
				<StartupForm onSelect={onClose} onClose={onClose} />
			</div>
		</dialog>
	);
}
