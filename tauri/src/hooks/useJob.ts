import { useRef, useState } from "react";
import { type FetchParams, getErrorMessage } from "../utils/fetchUtils";
import { useAbortSignalRef } from "./useAbortSignalRef";
import { useJobLog } from "./useJobLog";

// ジョブへ渡すfetchオプション（signalでsidecar側も中断し、onLogで実行中のログを受け取る）
export type JobContext = Required<Pick<FetchParams, "signal" | "onLog">>;

type JobState =
	| { status: "idle" }
	| { status: "running" }
	| { status: "failed"; error: string };

const IDLE: JobState = { status: "idle" };
const RUNNING: JobState = { status: "running" };

// イベントハンドラから起動するsidecarのジョブ1本分の状態（実行中・失敗時のエラーと実行中のログ）。
// 実行し直すと前回を中断し、アンマウント・cancel・置き換え後の結果は捨てる。
// runは毎回新しいクロージャなので、effectからは使わない（hooks-design.md）
export function useJob() {
	const [state, setState] = useState<JobState>(IDLE);
	const jobLog = useJobLog();
	const mountRef = useAbortSignalRef();
	const runRef = useRef<AbortController | null>(null);

	// 成功したら結果を返す。失敗・中断ならnull（失敗はerrorとlogsに残る）
	const run = async <T>(
		fn: (context: JobContext) => Promise<T>,
	): Promise<{ value: T } | null> => {
		const mount = mountRef.current;
		if (!mount || mount.signal.aborted) {
			return null;
		}
		runRef.current?.abort();
		const controller = new AbortController();
		runRef.current = controller;
		const signal = AbortSignal.any([mount.signal, controller.signal]);
		setState(RUNNING);
		jobLog.reset();
		try {
			const value = await fn({ signal, onLog: jobLog.append });
			if (signal.aborted) {
				return null;
			}
			setState(IDLE);
			return { value };
		} catch (ex) {
			if (!signal.aborted) {
				setState({ status: "failed", error: getErrorMessage(ex) });
			}
			return null;
		}
	};

	// 実行中なら中断し、失敗の表示も消す
	const cancel = () => {
		runRef.current?.abort();
		setState(IDLE);
	};

	return {
		running: state.status === "running",
		error: state.status === "failed" ? state.error : "",
		logs: jobLog.logs,
		run,
		cancel,
	};
}
