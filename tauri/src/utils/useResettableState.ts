import { useState } from "react";

// resetKeyが変わったら（!==で比べる）、子を再マウントせずにstateだけinitialへ作り直す。initialは初回とリセット時だけ読む
export function useResettableState<T, K = string>(initial: T, resetKey?: K) {
	const [state, setState] = useState<T>(initial);
	const [currentResetKey, setCurrentResetKey] = useState(resetKey);
	if (resetKey !== currentResetKey) {
		setCurrentResetKey(resetKey);
		setState(initial);
	}
	return [state, setState] as const;
}

// 外から与えられる値を入力中の値として持つ。refresh等でvalueが変わったら、入力中の値をその値に戻す
export function useSyncedState(value: string) {
	return useResettableState(value, value);
}
