import { useState } from "react";
import { FullDialog } from "../../components/dialog";
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
	return (
		<FullDialog onClose={onClose}>
			<div className="p-4 rounded-lg mt-2 w-full max-w-4xl">
				<StartupForm onSelect={onClose} onClose={onClose} />
			</div>
		</FullDialog>
	);
}
