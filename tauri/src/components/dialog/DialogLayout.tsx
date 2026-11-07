import type { ReactNode } from "react";
import { useDialogMaximize } from "./DialogMaximize";

const BODY_SIZE = {
	sm: "w-dialog-sm max-w-full p-4",
	md: "w-dialog-md max-w-full p-4",
	lg: "w-dialog-lg max-w-full p-4",
	// 幅固定ではなく最小幅のみ指定し、内容に応じて広がる
	auto: "min-w-dialog-min p-4",
	// 幅は親に任せる
	fluid: "p-4 rounded-lg mt-2",
} as const;

// ダイアログ本体の幅・パディング。高さの上限とスクロールはダイアログの枠（FullDialog / ModalOverlay）が持つ。
// 最大化中は枠の幅と残りの高さを埋める
export function DialogBody({
	size,
	children,
}: {
	size: keyof typeof BODY_SIZE;
	children: ReactNode;
}) {
	const { maximized } = useDialogMaximize();
	const className = maximized
		? "grow flex flex-col w-full p-4"
		: BODY_SIZE[size];
	return <div className={className}>{children}</div>;
}

// ダイアログ内の入力項目1つ分の縦余白。最後の項目は last を指定して下余白を広げる。
// grow を指定すると、最大化中は残りの高さを埋める（DialogTextArea を入れる項目に使う）
export function DialogField({
	last = false,
	grow = false,
	children,
}: {
	last?: boolean;
	grow?: boolean;
	children: ReactNode;
}) {
	const { maximized } = useDialogMaximize();
	const margin = last ? "mb-4" : "mb-3";
	if (grow && maximized) {
		return <div className={`${margin} grow flex flex-col`}>{children}</div>;
	}
	return <div className={margin}>{children}</div>;
}

// ボタンを横並びにする行
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
