import { useState } from "react";

// resetKeyが変わったら、子を再マウントせずにstateだけinitialへ作り直す。initialは初回とリセット時だけ読む
export function useResettableState<T>(initial: T, resetKey?: string) {
	const [state, setState] = useState<T>(initial);
	const [currentResetKey, setCurrentResetKey] = useState(resetKey);
	if (resetKey !== currentResetKey) {
		setCurrentResetKey(resetKey);
		setState(initial);
	}
	return [state, setState] as const;
}
