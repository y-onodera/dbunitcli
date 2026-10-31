import { act, renderHook } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { type JobContext, useJob } from "../../hooks/useJob";
import {
	emitJobLogs,
	emitLogsThenFail,
	JOB_ERROR,
	JOB_LOG_LINES,
} from "../helpers/jobFailure";

// 外から完了させられるジョブ（実行中の状態を確かめる）
function deferredJob() {
	let resolve: (value: string) => void = () => {};
	let context: JobContext | null = null;
	const fn = (ctx: JobContext) => {
		context = ctx;
		return new Promise<string>((r) => {
			resolve = r;
		});
	};
	return {
		fn,
		resolve: (value: string) => resolve(value),
		context: () => context,
	};
}

describe("useJobのテスト", () => {
	it("成功したら結果を返し、実行中・エラーは残らない", async () => {
		const { result } = renderHook(() => useJob());
		let outcome: { value: string } | null = null;
		await act(async () => {
			outcome = await result.current.run(async ({ onLog }) => {
				emitJobLogs(onLog);
				return "done";
			});
		});

		expect(outcome).toStrictEqual({ value: "done" });
		expect(result.current.running).toBe(false);
		expect(result.current.error).toBe("");
		expect(result.current.logs).toStrictEqual(JOB_LOG_LINES);
	});

	it("失敗したらnullを返し、エラーと実行中のログを残す", async () => {
		const { result } = renderHook(() => useJob());
		let outcome: { value: never } | null = { value: undefined as never };
		await act(async () => {
			outcome = await result.current.run(({ onLog }) =>
				emitLogsThenFail(onLog),
			);
		});

		expect(outcome).toBeNull();
		expect(result.current.running).toBe(false);
		expect(result.current.error).toBe(JOB_ERROR);
		expect(result.current.logs).toStrictEqual(JOB_LOG_LINES);
	});

	it("実行し直すとエラーとログを消して実行中になる", async () => {
		const { result } = renderHook(() => useJob());
		await act(async () => {
			await result.current.run(({ onLog }) => emitLogsThenFail(onLog));
		});
		const job = deferredJob();
		act(() => {
			result.current.run(job.fn);
		});

		expect(result.current.running).toBe(true);
		expect(result.current.error).toBe("");
		expect(result.current.logs).toStrictEqual([]);
	});

	it("実行し直すと前回を中断し、前回の結果は捨てる", async () => {
		const { result } = renderHook(() => useJob());
		const first = deferredJob();
		let firstOutcome: Promise<{ value: string } | null> | null = null;
		act(() => {
			firstOutcome = result.current.run(first.fn);
		});
		const second = deferredJob();
		act(() => {
			result.current.run(second.fn);
		});

		expect(first.context()?.signal.aborted).toBe(true);
		await act(async () => {
			first.resolve("stale");
		});
		expect(await firstOutcome).toBeNull();
		expect(result.current.running).toBe(true);
	});

	it("cancelで中断し、実行中の表示を消す", async () => {
		const { result } = renderHook(() => useJob());
		const job = deferredJob();
		let outcome: Promise<{ value: string } | null> | null = null;
		act(() => {
			outcome = result.current.run(job.fn);
		});
		act(() => {
			result.current.cancel();
		});

		expect(job.context()?.signal.aborted).toBe(true);
		expect(result.current.running).toBe(false);
		await act(async () => {
			job.resolve("stale");
		});
		expect(await outcome).toBeNull();
	});

	it("アンマウントで中断する", () => {
		const { result, unmount } = renderHook(() => useJob());
		const job = deferredJob();
		act(() => {
			result.current.run(job.fn);
		});
		unmount();

		expect(job.context()?.signal.aborted).toBe(true);
	});
});
