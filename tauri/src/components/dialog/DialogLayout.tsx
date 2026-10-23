import type { ReactNode } from "react";

const BODY_SIZE = {
	sm: "w-dialog-sm p-4",
	md: "w-dialog-md p-4",
	lg: "w-dialog-lg p-4",
	// 幅固定ではなく最小幅のみ指定し、内容に応じて広がる
	auto: "min-w-dialog-min p-4",
	// 幅は親に任せる
	fluid: "p-4 rounded-lg mt-2",
} as const;

// ダイアログ本体の幅・パディング。scrollable 指定時は高さ上限を設けてスクロールさせる
export function DialogBody({
	size,
	scrollable = false,
	children,
}: {
	size: keyof typeof BODY_SIZE;
	scrollable?: boolean;
	children: ReactNode;
}) {
	const scroll = scrollable ? " overflow-y-auto max-h-[80vh]" : "";
	return <div className={`${BODY_SIZE[size]}${scroll}`}>{children}</div>;
}

// ダイアログ内の入力項目1つ分の縦余白。最後の項目は last を指定して下余白を広げる
export function DialogField({
	last = false,
	children,
}: {
	last?: boolean;
	children: ReactNode;
}) {
	return <div className={last ? "mb-4" : "mb-3"}>{children}</div>;
}

// ボタンを横並びにする行（gap-3 に統一）
export function InlineActions({ children }: { children: ReactNode }) {
	return <div className="mt-2 flex items-center gap-3">{children}</div>;
}

const STATUS_COLOR = {
	loading: "text-content-muted",
	error: "text-error",
	empty: "text-content-disabled",
} as const;

export function StatusMessage({
	kind,
	children,
}: {
	kind: keyof typeof STATUS_COLOR;
	children: ReactNode;
}) {
	return (
		<p className={`text-sm ${STATUS_COLOR[kind]} px-3 py-2`}>{children}</p>
	);
}
