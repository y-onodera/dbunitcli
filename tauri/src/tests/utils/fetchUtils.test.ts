import { fetch } from "@tauri-apps/plugin-http";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { fetchData, isAbortError } from "../../utils/fetchUtils";

vi.mock("@tauri-apps/plugin-http", () => ({ fetch: vi.fn() }));

const mockFetch = vi.mocked(fetch);
const API_URL = "http://localhost:8080/dbunit-cli/";
const JOB_URL = `${API_URL}job/job-1/`;

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

const sseLog = (line: string) => `event: log\ndata: ${line}\n\n`;

const execParams = {
	endpoint: `${API_URL}compare/exec`,
	options: {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: "{}",
	},
};

// 本体の応答とログのSSEを返すfetchのモック
const mockJob = (
	execResponse: () => Promise<Response>,
	events: ReadableStream<Uint8Array> = sseStream([]),
) => {
	mockFetch.mockImplementation(async (input) => {
		const url = String(input);
		if (url === `${JOB_URL}events`) {
			return new Response(events, { status: 200 });
		}
		if (url === `${JOB_URL}cancel`) {
			return new Response("success", { status: 200 });
		}
		return execResponse();
	});
};

const execCall = () =>
	mockFetch.mock.calls.find(([url]) => String(url) === execParams.endpoint);

describe("fetchData（job指定）", () => {
	beforeEach(() => {
		mockFetch.mockReset();
		vi.spyOn(crypto, "randomUUID").mockReturnValue(
			"job-1" as ReturnType<typeof crypto.randomUUID>,
		);
	});

	afterEach(() => {
		vi.restoreAllMocks();
	});

	it("X-Job-Id付きで本体を呼び、結果は本体の応答をそのまま返す", async () => {
		mockJob(async () => new Response("/result/dir", { status: 200 }));

		const response = await fetchData({ ...execParams, job: {} });

		expect(await response.text()).toBe("/result/dir");
		const headers = new Headers(execCall()?.[1]?.headers);
		expect(headers.get("X-Job-Id")).toBe("job-1");
		expect(headers.get("Content-Type")).toBe("application/json");
		expect(mockFetch).toHaveBeenCalledTimes(1);
	});

	it("onLogがあればSSEのログ行（チャンク境界をまたぐものを含む）を渡す", async () => {
		const log1 = sseLog("line 1");
		mockJob(
			async () => new Response("ok", { status: 200 }),
			sseStream([log1.slice(0, 10), log1.slice(10), sseLog("line 2")]),
		);
		const onLog = vi.fn();

		await fetchData({ ...execParams, job: { onLog } });

		await vi.waitFor(() =>
			expect(onLog.mock.calls).toEqual([["line 1"], ["line 2"]]),
		);
		expect(mockFetch).toHaveBeenCalledWith(
			`${JOB_URL}events`,
			expect.objectContaining({ method: "GET" }),
		);
	});

	it("エラー応答は従来と同じ形式のエラーにする", async () => {
		mockJob(
			async () =>
				new Response("", { status: 400, statusText: "Fix Input Parameter" }),
		);

		await expect(fetchData({ ...execParams, job: {} })).rejects.toThrow(
			"Status: 400\nDetails: Fix Input Parameter",
		);
	});

	it("中断するとsidecarへcancelを送りAbortErrorにする", async () => {
		const controller = new AbortController();
		mockJob(
			() =>
				new Promise<Response>((_, reject) => {
					controller.signal.addEventListener("abort", () =>
						reject(new DOMException("Aborted", "AbortError")),
					);
				}),
			sseStream([], false),
		);
		const pending = fetchData({
			...execParams,
			signal: controller.signal,
			job: { onLog: vi.fn() },
		}).catch((ex) => ex);

		controller.abort();

		expect(isAbortError(await pending)).toBe(true);
		expect(mockFetch).toHaveBeenCalledWith(`${JOB_URL}cancel`, {
			method: "POST",
		});
	});

	it("job未指定ならヘッダを付けず従来どおり呼ぶ", async () => {
		mockFetch.mockResolvedValue(new Response("ok", { status: 200 }));

		const response = await fetchData(execParams);

		expect(await response.text()).toBe("ok");
		expect(mockFetch).toHaveBeenCalledTimes(1);
		expect(new Headers(execCall()?.[1]?.headers).has("X-Job-Id")).toBe(false);
	});
});
