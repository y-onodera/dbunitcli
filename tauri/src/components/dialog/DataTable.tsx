import type { ReactNode } from "react";

// 枠・スクロール・sticky ヘッダを持つテーブルの土台。maxHeightClass は max-h-xx を渡す
export function DataTable({
	maxHeightClass = "max-h-72",
	head,
	children,
}: {
	maxHeightClass?: string;
	head?: ReactNode;
	children: ReactNode;
}) {
	return (
		<div
			className={`relative overflow-auto ${maxHeightClass} border border-border-subtle rounded-lg`}
		>
			<table className="w-full text-sm text-left">
				{head && (
					<thead className="bg-surface-subtle sticky top-0">
						<tr>{head}</tr>
					</thead>
				)}
				<tbody>{children}</tbody>
			</table>
		</div>
	);
}

export function Th({
	narrow = false,
	nowrap = false,
	children,
}: {
	narrow?: boolean;
	nowrap?: boolean;
	children?: ReactNode;
}) {
	return (
		<th
			className={`px-3 py-2 font-medium text-content-secondary${narrow ? " w-8" : ""}${nowrap ? " whitespace-nowrap" : ""}`}
		>
			{children}
		</th>
	);
}

export function Tr({
	onClick,
	muted = false,
	children,
}: {
	onClick?: () => void;
	muted?: boolean;
	children: ReactNode;
}) {
	const base = "border-t border-border-faint";
	const tone = muted ? " bg-surface-subtle" : " hover:bg-surface-subtle";
	return (
		<tr
			className={`${base}${tone}${onClick ? " cursor-pointer" : ""}`}
			onClick={onClick}
		>
			{children}
		</tr>
	);
}

export function Td({
	align,
	nowrap = false,
	children,
}: {
	align?: "right";
	nowrap?: boolean;
	children?: ReactNode;
}) {
	return (
		<td
			className={`px-3 py-1.5 text-content-muted${align === "right" ? " text-right" : ""}${nowrap ? " whitespace-nowrap" : ""}`}
		>
			{children}
		</td>
	);
}
