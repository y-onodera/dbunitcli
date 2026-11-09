import { type ReactNode, useRef } from "react";
import { SectionHelpButton } from "./DialogHelpButton";
import { DIALOG_WIDTH } from "./DialogLayout";
import {
	DialogMaximizeContext,
	MaximizeToggleButton,
	useDialogMaximizeState,
} from "./DialogMaximize";
import { useModalDialog } from "./modalDialog";

// showModal() で top layer に載せ、背後の画面を操作不可にする。入れ子にしても後から開いたものが上に重なる
export function ModalOverlay({
	children,
	onClose,
	size = "sm",
	maximizable = false,
}: {
	children: ReactNode;
	// Esc で呼ぶ
	onClose: () => void;
	size?: keyof typeof DIALOG_WIDTH;
	maximizable?: boolean;
}) {
	const dialogRef = useRef<HTMLDialogElement>(null);
	const dialog = useModalDialog(dialogRef, onClose);
	const maximize = useDialogMaximizeState(maximizable);
	const frameSize = maximize.maximized
		? "open:flex flex-col w-full h-full max-h-[calc(100%-2rem)]"
		: `max-h-[80vh] ${DIALOG_WIDTH[size]}`;
	return (
		<dialog
			ref={dialogRef}
			onCancel={dialog.onCancel}
			onClose={dialog.onClose}
			className={`m-auto bg-surface text-content rounded-lg shadow-modal p-6 max-w-[calc(100%-2rem)] overflow-y-auto backdrop:bg-overlay ${frameSize}`}
		>
			<DialogMaximizeContext value={maximize}>{children}</DialogMaximizeContext>
		</dialog>
	);
}

export function DialogTitle(props: {
	children: ReactNode;
	command?: string;
	label?: string;
}) {
	return (
		<h2 className="flex items-center gap-1 text-lg font-semibold mb-4">
			{props.children}
			{props.command && props.label && (
				<SectionHelpButton command={props.command} label={props.label} />
			)}
			<MaximizeToggleButton />
		</h2>
	);
}

// 枠のスクロール中も見えるよう下端に固定する。影は ModalOverlay の下パディング（p-6）を覆い、ボタンの下に本文が透けるのを防ぐ
export function DialogActions({ children }: { children: ReactNode }) {
	return (
		<div className="sticky bottom-0 bg-surface shadow-[0_1.5rem_0_var(--color-surface)] pt-3 flex items-center gap-2 justify-end">
			{children}
		</div>
	);
}

// FullDialog の直下に置き、枠のスクロール中も見えるよう下端に固定する
export function DialogFooter({ children }: { children: ReactNode }) {
	return (
		<div className="sticky bottom-0 bg-surface flex items-center gap-2 justify-end p-4">
			{children}
		</div>
	);
}
