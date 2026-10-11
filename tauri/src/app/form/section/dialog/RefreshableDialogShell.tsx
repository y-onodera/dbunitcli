import type { ReactNode } from "react";
import { createPortal } from "react-dom";
import {
	DialogFooter,
	DialogTitle,
	FullDialog,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import type { DialogLoadState } from "./refreshableDialogForm";

// refresh系ダイアログ（Generate・Scaffold）共通の枠: タイトル・読込/エラー表示・form・Execute/Closeフッタ
export function RefreshableDialogShell<OPTIONS>({
	command,
	label,
	title,
	formId,
	loadState,
	executing,
	executeError,
	onExecute,
	onClose,
	children,
}: {
	command: string;
	label: string;
	title: string;
	formId: string;
	loadState: DialogLoadState<OPTIONS>;
	executing: boolean;
	executeError: string;
	onExecute: () => void;
	onClose: () => void;
	children: (options: OPTIONS) => ReactNode;
}) {
	return createPortal(
		<FullDialog onClose={onClose}>
			<div className="overflow-y-auto max-h-[80vh] min-w-[600px] p-4">
				<DialogTitle command={command} label={label}>
					{title}
				</DialogTitle>
				{loadState.status === "loading" && (
					<div className="text-content-muted">Loading...</div>
				)}
				{loadState.status === "error" && (
					<div className="text-error">Error: {loadState.message}</div>
				)}
				{loadState.status === "loaded" && (
					<form
						id={formId}
						className="grid gap-6 grid-cols-1"
						noValidate
						onSubmit={(e) => {
							e.preventDefault();
						}}
					>
						{children(loadState.options)}
					</form>
				)}
				{executeError && (
					<div className="text-error">Error: {executeError}</div>
				)}
			</div>
			<DialogFooter>
				<BlueButton
					title="Execute"
					handleClick={onExecute}
					disabled={loadState.status !== "loaded" || executing}
				/>
				<WhiteButton title="Close" handleClick={onClose} />
			</DialogFooter>
		</FullDialog>,
		document.body,
	);
}
