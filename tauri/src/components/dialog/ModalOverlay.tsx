import type { ReactNode } from "react";

export function ModalOverlay({
	children,
	width = "w-96",
	zClass = "z-50",
}: {
	children: ReactNode;
	width?: string;
	zClass?: string;
}) {
	return (
		<div
			className={`fixed inset-0 bg-black/50 flex items-center justify-center ${zClass}`}
		>
			<div className={`bg-surface rounded-lg shadow-lg p-6 max-w-full ${width}`}>
				{children}
			</div>
		</div>
	);
}

export function DialogTitle({ children }: { children: ReactNode }) {
	return <h2 className="text-lg font-semibold mb-4">{children}</h2>;
}

export function DialogActions({ children }: { children: ReactNode }) {
	return <div className="flex gap-2 justify-end">{children}</div>;
}
