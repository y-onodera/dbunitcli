import { type RefObject, type SyntheticEvent, useEffect } from "react";

// <dialog> を showModal() で開き、Esc は閉じずに onClose へ任せる（閉じるかは親がアンマウントで決める）。
// Chrome は利用者操作の無い連続 Esc の cancel を取り消させず閉じてしまうため、まだ表示中なら開き直す。
// React の onCancel / onClose は入れ子の dialog から親コンポーネントへ伝わるため、自身の dialog の分だけ扱う
export function useModalDialog(
	ref: RefObject<HTMLDialogElement | null>,
	onClose: () => void,
) {
	useEffect(() => {
		if (ref.current) {
			showAboveChildren(ref.current);
		}
	}, [ref]);
	return {
		onCancel: (ev: SyntheticEvent<HTMLDialogElement>) => {
			if (ev.target !== ev.currentTarget) {
				return;
			}
			ev.preventDefault();
			onClose();
		},
		onClose: (ev: SyntheticEvent<HTMLDialogElement>) => {
			if (ev.target === ev.currentTarget) {
				reopenIfClosed(ev.currentTarget);
			}
		},
	};
}

// effect は子から先に走り、同時にマウントした入れ子の dialog が親より下に積まれるため、開き直して親の上に載せる
function showAboveChildren(dialog: HTMLDialogElement) {
	dialog.showModal();
	for (const child of dialog.querySelectorAll<HTMLDialogElement>(
		"dialog[open]",
	)) {
		child.close();
		child.showModal();
	}
}

function reopenIfClosed(dialog: HTMLDialogElement) {
	if (dialog.isConnected && !dialog.open) {
		dialog.showModal();
	}
}
