import { useState } from "react";

// 長時間のジョブでも描画コストが増え続けないよう、表示するログは末尾の一定行数に限る
const MAX_LOG_LINES = 1000;

// 蓄積済みのログへ行を足し、末尾の一定行数に切り詰める（setStateの更新関数から使う）
export const appendLogLines = (current: string[], lines: string[]) =>
	current.length + lines.length <= MAX_LOG_LINES
		? [...current, ...lines]
		: [...current, ...lines].slice(-MAX_LOG_LINES);

// ジョブからpushされたログ行を蓄積する（中断後の行はfetchData側で止める）
export const useJobLog = () => {
	const [logs, setLogs] = useState<string[]>([]);
	const reset = () => setLogs([]);
	const append = (line: string) =>
		setLogs((current) => appendLogLines(current, [line]));
	return { logs, reset, append };
};
