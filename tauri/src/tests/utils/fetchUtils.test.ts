import { fetch } from "@tauri-apps/plugin-http";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
	fetchData,
	isAbortError,
	OptionValidationError,
} from "../../utils/fetchUtils";

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

const sseLog = (line: string) => `event: log\ndata: ${line}\n\n`;
const sseHeartbeat = ": heartbeat\n\n";
const sseResult = (status: number, body: string, reason = "OK") =>
	`event: result\ndata: ${JSON.stringify({ status, reason, body })}\n\n`;

const sseResponse = (body: ReadableStream<Uint8Array>) =>
	new Response(body, {
		status: 200,
		headers: { "Content-Type": "text/event-stream" },
	});

const execParams = {
	endpoint: `${API_URL}compare/exec`,
	options: {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: "{}",
	},
};

const requestHeaders = () => new Headers(mockFetch.mock.calls[0]?.[1]?.headers);

describe("fetchData（ジョブのSSE）", () => {
	beforeEach(() => {
		mockFetch.mockReset();
	});

	it("signal付きならAcceptにSSEを加えて1回だけ呼び、resultから通常の応答を組み立てる", async () => {
		mockFetch.mockResolvedValue(
			sseResponse(sseStream([sseResult(200, "/result/dir")])),
		);

		const response = await fetchData({
			...execParams,
			signal: new AbortController().signal,
		});

		expect(response.status).toBe(200);
		expect(await response.text()).toBe("/result/dir");
		expect(requestHeaders().get("Accept")).toBe("text/event-stream, */*;q=0.1");
		expect(requestHeaders().get("Content-Type")).toBe("application/json");
		expect(requestHeaders().has("X-Job-Log")).toBe(false);
		expect(mockFetch).toHaveBeenCalledTimes(1);
	});

	it("onLogへログ行（チャンク境界をまたぐものを含む）を順に渡し、ハートビートは読み飛ばす", async () => {
		const log1 = sseLog("line 1");
		const result = sseResult(200, "ok");
		mockFetch.mockResolvedValue(
			sseResponse(
				sseStream([
					log1.slice(0, 10),
					log1.slice(10),
					sseHeartbeat,
					sseLog("line 2"),
					result.slice(0, 20),
					result.slice(20),
				]),
			),
		);
		const onLog = vi.fn();

		const response = await fetchData({ ...execParams, onLog });

		expect(onLog.mock.calls).toEqual([["line 1"], ["line 2"]]);
		expect(requestHeaders().get("X-Job-Log")).toBe("true");
		expect(await response.text()).toBe("ok");
	});

	it("resultのオプション検証エラーはOptionValidationErrorにして違反を渡す", async () => {
		const violations = [
			{ options: ["-src.src"], message: "file not found" },
			{ options: ["-cmdParam", "-template"], message: "mutually exclusive" },
		];
		mockFetch.mockResolvedValue(
			sseResponse(
				sseStream([
					sseResult(
						400,
						JSON.stringify({ message: "invalid", violations }),
						"Invalid Option",
					),
				]),
			),
		);

		const error = await fetchData({
			...execParams,
			signal: new AbortController().signal,
		}).catch((ex: unknown) => ex);

		expect(error).toBeInstanceOf(OptionValidationError);
		expect((error as OptionValidationError).message).toBe("invalid");
		expect((error as OptionValidationError).violations).toStrictEqual(
			violations,
		);
	});

	it("resultのエラー応答は従来と同じ形式のエラーにする", async () => {
		mockFetch.mockResolvedValue(
			sseResponse(
				sseStream([sseResult(400, '{"message":"bad"}', "Fix Input Parameter")]),
			),
		);

		await expect(
			fetchData({ ...execParams, signal: new AbortController().signal }),
		).rejects.toThrow("Status: 400\nDetails: Fix Input Parameter");
	});

	it("SSEでない応答（ジョブ対象外のエンドポイント）はそのまま返す", async () => {
		mockFetch.mockResolvedValue(
			new Response('{"saved":true}', {
				status: 200,
				headers: { "Content-Type": "application/json" },
			}),
		);

		const response = await fetchData({
			...execParams,
			signal: new AbortController().signal,
		});

		expect(await response.json()).toEqual({ saved: true });
	});

	it("SSE受信中に中断するとAbortErrorにし、以降のログは渡さない", async () => {
		const controller = new AbortController();
		mockFetch.mockResolvedValue(
			sseResponse(sseStream([sseLog("before abort")], false)),
		);
		const onLog = vi.fn();
		const pending = fetchData({
			...execParams,
			signal: controller.signal,
			onLog,
		}).catch((ex) => ex);
		await vi.waitFor(() => expect(onLog).toHaveBeenCalledTimes(1));

		controller.abort();

		expect(isAbortError(await pending)).toBe(true);
		expect(onLog.mock.calls).toEqual([["before abort"]]);
		expect(mockFetch).toHaveBeenCalledTimes(1);
	});

	it("signalとtimeoutMsの両方があれば、タイムアウトでも中断する", async () => {
		mockFetch.mockResolvedValue(sseResponse(sseStream([], false)));

		const error = await fetchData({
			...execParams,
			signal: new AbortController().signal,
			timeoutMs: 10,
		}).catch((ex) => ex);

		expect(isAbortError(error)).toBe(true);
	});

	it("resultが届かずに閉じた場合はエラーにする", async () => {
		mockFetch.mockResolvedValue(sseResponse(sseStream([sseLog("only log")])));

		await expect(
			fetchData({ ...execParams, signal: new AbortController().signal }),
		).rejects.toThrow("Job stream closed without result");
	});

	it("signalもonLogも無ければAcceptを付けず従来どおり呼ぶ", async () => {
		mockFetch.mockResolvedValue(new Response("ok", { status: 200 }));

		const response = await fetchData(execParams);

		expect(await response.text()).toBe("ok");
		expect(mockFetch).toHaveBeenCalledTimes(1);
		expect(requestHeaders().has("Accept")).toBe(false);
	});
});
