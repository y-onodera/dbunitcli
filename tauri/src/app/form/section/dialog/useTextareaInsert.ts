import { useLayoutEffect, useRef, useState } from "react";
import type { InsertResult } from "../../../../utils/textInsert";

export function useTextareaInsert<
	E extends HTMLTextAreaElement | HTMLInputElement = HTMLTextAreaElement,
>(initialValue: string) {
	const [value, setValue] = useState(initialValue);
	const textareaRef = useRef<E>(null);
	const pendingCursorRef = useRef<number | null>(null);

	const insert = (
		compute: (value: string, start: number, end: number) => InsertResult,
	) => {
		const textarea = textareaRef.current;
		const start = textarea?.selectionStart ?? value.length;
		const end = textarea?.selectionEnd ?? value.length;
		const result = compute(value, start, end);
		pendingCursorRef.current = result.cursor;
		setValue(result.value);
	};

	// 続けて挿入したときに直前の挿入の後ろへ入るよう、描画後にカーソルを移す
	useLayoutEffect(() => {
		const cursor = pendingCursorRef.current;
		if (cursor === null) {
			return;
		}
		pendingCursorRef.current = null;
		textareaRef.current?.setSelectionRange(cursor, cursor);
	});

	return { value, setValue, textareaRef, insert };
}
