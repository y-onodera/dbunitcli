import type { ReactNode } from "react";
import { SectionHelpButton } from "./DialogHelpButton";
import {
	DialogMaximizeProvider,
	MaximizeToggleButton,
	useDialogMaximizeState,
} from "./DialogMaximize";

export function ModalOverlay({
	children,
	width = "w-96",
	zClass = "z-modal",
	maximizable = false,
}: {
	children: ReactNode;
	width?: string;
	zClass?: string;
	maximizable?: boolean;
}) {
	const maximize = useDialogMaximizeState(maximizable);
	const size = maximize.maximized
		? "flex flex-col w-full h-[calc(100vh-2rem)]"
		: `max-h-[80vh] ${width}`;
	return (
		<div
			className={`fixed inset-0 bg-overlay flex items-center justify-center ${zClass}`}
		>
			<div
				className={`bg-surface rounded-lg shadow-modal p-6 max-w-[calc(100vw-2rem)] overflow-y-auto ${size}`}
			>
				<DialogMaximizeProvider value={maximize}>
					{children}
				</DialogMaximizeProvider>
			</div>
		</div>
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
