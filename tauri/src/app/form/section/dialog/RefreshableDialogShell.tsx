import type { ReactNode } from "react";
import { createPortal } from "react-dom";
import {
	DialogBody,
	DialogFooter,
	DialogTitle,
	FullDialog,
	StatusMessage,
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
			<DialogBody size="auto" scrollable>
				<DialogTitle command={command} label={label}>
					{title}
				</DialogTitle>
				{loadState.status === "loading" && (
					<StatusMessage kind="loading">Loading...</StatusMessage>
				)}
				{loadState.status === "error" && (
					<StatusMessage kind="error">Error: {loadState.message}</StatusMessage>
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
					<StatusMessage kind="error">Error: {executeError}</StatusMessage>
				)}
			</DialogBody>
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
