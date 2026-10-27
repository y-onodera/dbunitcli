import { core } from "@tauri-apps/api";
import { useEffect, useState } from "react";
import { appendLogLines } from "./useJobLog";

const POLL_INTERVAL_MS = 500;

// Tauri本体が溜めたsidecarの出力を、通し番号sinceの続きから定期的に読む。
// Tauriの外（テスト等）で読めなければ止める
async function pollSidecarLogs(
	signal: AbortSignal,
	onLines: (lines: string[]) => void,
): Promise<void> {
	let since = 0;
	while (!signal.aborted) {
		try {
			const [next, lines] = await core.invoke<[number, string[]]>(
				"sidecar_logs",
				{ since },
			);
			if (signal.aborted) {
				return;
			}
			since = next;
			if (lines.length > 0) {
				onLines(lines);
			}
		} catch {
			return;
		}
		await new Promise((resolve) => setTimeout(resolve, POLL_INTERVAL_MS));
	}
}

// sidecarの標準出力・標準エラー（起動中の表示用）。表示している間だけ読む
export const useSidecarLog = () => {
	const [logs, setLogs] = useState<string[]>([]);
	useEffect(() => {
		const controller = new AbortController();
		pollSidecarLogs(controller.signal, (lines) =>
			setLogs((current) => appendLogLines(current, lines)),
		);
		return () => {
			controller.abort();
		};
	}, []);
	return logs;
};
