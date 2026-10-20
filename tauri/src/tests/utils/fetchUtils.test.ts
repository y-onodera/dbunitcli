import { fetch } from "@tauri-apps/plugin-http";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { fetchData, isAbortError } from "../../utils/fetchUtils";

vi.mock("@tauri-apps/plugin-http", () => ({ fetch: vi.fn() }));

const mockFetch = vi.mocked(fetch);
const API_URL = "http://localhost:8080/dbunit-cli/";

// SSEの本文を任意の位置で分割したチャンクとして流すストリーム（close=falseなら閉じずに待ち続ける）
const sseStream = (chunks: string[], close = true) =>
	new ReadableStream<Uint8Array>({
		start(controller) {
			const encoder = new TextEncoder();
			for (const chunk of chunks) {
				controller.enqueue(encoder.encode(chunk));
			}
			if (close) {
				controller.close();
			}
		},
	});

const sseEvent = (type: string, data: Record<string, unknown>) =>
	`event: ${type}\ndata: ${JSON.stringify({ type, ...data })}\n\n`;

const mockJob = (events: ReadableStream<Uint8Array>) => {
	mockFetch.mockImplementation(async (input) => {
		const url = String(input);
		if (url.endsWith("/exec/job")) {
			return new Response(
				JSON.stringify({
					jobId: "job-1",
					events: "/dbunit-cli/job/job-1/events",
					cancel: "/dbunit-cli/job/job-1/cancel",
				}),
				{ status: 200 },
			);
		}
		if (url.endsWith("job/job-1/events")) {
			return new Response(events, { status: 200 });
		}
		return new Response("success", { status: 200 });
	});
};

const execParams = {
	endpoint: `${API_URL}compare/exec`,
	options: { method: "POST", body: "{}" },
};

describe("fetchData（job指定）", () => {
	beforeEach(() => {
		mockFetch.mockReset();
	});

	it("<endpoint>/jobで開始し、SSEのログをonLogへ渡して完了結果をレスポンスとして返す", async () => {
		const done = sseEvent("done", {
			status: "SUCCEEDED",
			httpStatus: 200,
			result: "/result/dir",
		});
		mockJob(
			sseStream([
				sseEvent("log", { message: "line 1" }).slice(0, 10),
				sseEvent("log", { message: "line 1" }).slice(10),
				sseEvent("log", { message: "line 2" }) + done.slice(0, 20),
				done.slice(20),
			]),
		);
		const onLog = vi.fn();

		const response = await fetchData({
			...execParams,
			job: { onLog },
		});

		expect(await response.text()).toBe("/result/dir");
		expect(onLog.mock.calls).toEqual([["line 1"], ["line 2"]]);
		expect(mockFetch).toHaveBeenCalledWith(
			`${API_URL}compare/exec/job`,
			expect.objectContaining({ method: "POST", body: "{}" }),
		);
		expect(mockFetch).toHaveBeenCalledWith(
			`${API_URL}job/job-1/events`,
			expect.objectContaining({ method: "GET" }),
		);
	});

	it("失敗の完了イベントは同期APIと同じ形式のエラーにする", async () => {
		mockJob(
			sseStream([
				sseEvent("done", {
					status: "FAILED",
					httpStatus: 400,
					reason: "Fix Input Parameter",
				}),
			]),
		);

		await expect(fetchData({ ...execParams, job: {} })).rejects.toThrow(
			"Status: 400\nDetails: Fix Input Parameter",
		);
	});

	it("完了イベントの前にストリームが閉じたらエラーにする", async () => {
		mockJob(sseStream([sseEvent("log", { message: "line 1" })]));

		await expect(fetchData({ ...execParams, job: {} })).rejects.toThrow(
			"Job event stream closed before completion",
		);
	});

	it("中断するとsidecarへcancelを送りAbortErrorにする", async () => {
		mockJob(sseStream([sseEvent("log", { message: "line 1" })], false));
		const controller = new AbortController();
		const onLog = vi.fn(() => controller.abort());

		const error = await fetchData({
			...execParams,
			signal: controller.signal,
			job: { onLog },
		}).catch((ex) => ex);

		expect(isAbortError(error)).toBe(true);
		expect(mockFetch).toHaveBeenCalledWith(`${API_URL}job/job-1/cancel`, {
			method: "POST",
		});
	});

	it("job未指定なら従来どおりendpointへ直接リクエストする", async () => {
		mockFetch.mockResolvedValue(new Response("ok", { status: 200 }));

		const response = await fetchData(execParams);

		expect(await response.text()).toBe("ok");
		expect(mockFetch).toHaveBeenCalledTimes(1);
		expect(mockFetch).toHaveBeenCalledWith(
			`${API_URL}compare/exec`,
			expect.anything(),
		);
	});
});
