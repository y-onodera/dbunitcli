import { type ReactNode, useEffect, useRef } from "react";
import {
	DialogMaximizeContext,
	useDialogMaximizeState,
} from "./DialogMaximize";

export function FullDialog({
	onClose,
	maximizable = false,
	children,
}: {
	onClose: () => void;
	maximizable?: boolean;
	children: ReactNode;
}) {
	const dialogRef = useRef<HTMLDialogElement>(null);
	const maximize = useDialogMaximizeState(maximizable);
	useEffect(() => {
		dialogRef.current?.showModal();
	}, []);
	return (
		<dialog
			ref={dialogRef}
			onClose={onClose}
			// 最大化中の大きさはブラウザ既定の max-width / max-height で止まる。子は DialogBody が残りの高さを埋める
			className={`overflow-y-auto fixed top-0 right-0 left-0 z-modal bg-surface border border-border-subtle${maximize.maximized ? " open:flex flex-col w-full h-full" : ""}`}
		>
			<DialogMaximizeContext value={maximize}>{children}</DialogMaximizeContext>
		</dialog>
	);
}
