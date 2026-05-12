import { type ReactNode, useEffect, useRef } from "react";

export function FullDialog({
	onClose,
	children,
	className,
}: {
	onClose: () => void;
	children: ReactNode;
	className?: string;
}) {
	const dialogRef = useRef<HTMLDialogElement>(null);
	useEffect(() => {
		dialogRef.current?.showModal();
	}, []);
	const base =
		"overflow-y-auto fixed top-0 right-0 left-0 z-modal bg-surface border border-border-subtle";
	return (
		<dialog
			ref={dialogRef}
			onClose={onClose}
			className={className ? `${base} ${className}` : base}
		>
			{children}
		</dialog>
	);
}
