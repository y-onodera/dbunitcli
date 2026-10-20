import { useState } from "react";

// 長時間のジョブでも描画コストが増え続けないよう、表示するログは末尾の一定行数に限る
const MAX_LOG_LINES = 1000;

// ジョブからpushされたログ行を蓄積する（中断後の行はfetchData側で止める）
export const useJobLog = () => {
	const [logs, setLogs] = useState<string[]>([]);
	const reset = () => setLogs([]);
	const append = (line: string) =>
		setLogs((current) =>
			current.length < MAX_LOG_LINES
				? [...current, line]
				: [...current.slice(1), line],
		);
	return { logs, reset, append };
};
