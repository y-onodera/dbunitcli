import { useEffect, useRef } from "react";

// イベントハンドラ起点の非同期処理をアンマウント後に無効化するためのAbortController ref。
// 判定は `ref.current?.signal.aborted`（cleanupでrefをnullにしないこと）
export function useAbortSignalRef() {
	const abortControllerRef = useRef<AbortController | null>(null);
	useEffect(() => {
		const controller = new AbortController();
		abortControllerRef.current = controller;
		return () => {
			controller.abort();
		};
	}, []);
	return abortControllerRef;
}
