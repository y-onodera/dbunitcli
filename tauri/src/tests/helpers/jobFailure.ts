import { screen } from "@testing-library/react";
import { expect } from "vitest";

// sidecarのジョブが途中で失敗したときの、届くログと失敗のメッセージ
export const JOB_LOG_LINES = ["step1", "step2"];
export const JOB_ERROR = "boom";

// 実行中のログを流す（取得処理のモックから呼ぶ）
export function emitJobLogs(onLog: ((line: string) => void) | undefined) {
	for (const line of JOB_LOG_LINES) {
		onLog?.(line);
	}
}

// 実行中のログを流してから失敗する
export async function emitLogsThenFail(
	onLog: ((line: string) => void) | undefined,
): Promise<never> {
	emitJobLogs(onLog);
	throw new Error(JOB_ERROR);
}

// 実行ログ欄に、流したログが表示されている
export function expectJobLog() {
	expect(screen.getByRole("log", { name: "execution log" })).toHaveTextContent(
		JOB_LOG_LINES.join(" "),
	);
}

// 失敗の表示（JobFailure）: エラーメッセージと、実行中に届いたログ
export function expectJobFailure() {
	expect(screen.getByText(`Error: ${JOB_ERROR}`)).toBeInTheDocument();
	expectJobLog();
}
