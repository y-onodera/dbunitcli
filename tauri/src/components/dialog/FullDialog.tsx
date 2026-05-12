import { type ReactNode, useEffect, useRef } from "react";

export function FullDialog({
	onClose,
	children,
}: {
	onClose: () => void;
	children: ReactNode;
}) {
	const dialogRef = useRef<HTMLDialogElement>(null);
	useEffect(() => {
		dialogRef.current?.showModal();
	}, []);
	return (
		<dialog
			ref={dialogRef}
			onClose={onClose}
			className="overflow-y-auto fixed top-0 right-0 left-0 z-modal bg-surface border border-border-subtle"
		>
			{children}
		</dialog>
	);
}
