import { useState } from "react";

// 長時間のジョブでも描画コストが増え続けないよう、表示するログは末尾の一定行数に限る
const MAX_LOG_LINES = 1000;

// ジョブからpushされたログ行を蓄積する。中断（abort）後に届いた行は捨てる
export const useJobLog = () => {
	const [logs, setLogs] = useState<string[]>([]);
	const reset = () => setLogs([]);
	const onLog = (signal?: AbortSignal) => (line: string) => {
		if (!signal?.aborted) {
			setLogs((current) => [...current, line].slice(-MAX_LOG_LINES));
		}
	};
	return { logs, reset, onLog };
};
