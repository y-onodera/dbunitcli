import type { ReactNode } from "react";
import { SectionHelpButton } from "./DialogHelpButton";

export function ModalOverlay({
	children,
	width = "w-96",
	zClass = "z-modal",
}: {
	children: ReactNode;
	width?: string;
	zClass?: string;
}) {
	return (
		<div
			className={`fixed inset-0 bg-black/50 flex items-center justify-center ${zClass}`}
		>
			<div
				className={`bg-surface rounded-lg shadow-modal p-6 max-w-full ${width}`}
			>
				{children}
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
		</h2>
	);
}

export function DialogActions({ children }: { children: ReactNode }) {
	return <div className="flex items-center gap-2 justify-end">{children}</div>;
}

export function DialogFooter({ children }: { children: ReactNode }) {
	return (
		<div className="flex items-center gap-2 justify-end p-4">{children}</div>
	);
}
