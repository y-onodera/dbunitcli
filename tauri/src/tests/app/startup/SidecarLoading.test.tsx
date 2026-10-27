import { core } from "@tauri-apps/api";
import { render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import SidecarLoading from "../../../app/startup/SidecarLoading";

vi.mock("@tauri-apps/api", () => ({ core: { invoke: vi.fn() } }));

const mockInvoke = vi.mocked(core.invoke);

describe("SidecarLoading", () => {
	afterEach(() => {
		mockInvoke.mockReset();
	});

	it("sidecarの出力を通し番号の続きから読み、届いた行を順に表示する", async () => {
		mockInvoke
			.mockResolvedValueOnce([2, ["line1", "line2"]])
			.mockResolvedValueOnce([3, ["line3"]])
			.mockResolvedValue([3, []]);

		render(<SidecarLoading />);

		expect(screen.getByText("Loading...")).toBeInTheDocument();
		await waitFor(() =>
			expect(screen.getByRole("log")).toHaveTextContent("line1 line2 line3"),
		);
		expect(mockInvoke.mock.calls.slice(0, 2)).toEqual([
			["sidecar_logs", { since: 0 }],
			["sidecar_logs", { since: 2 }],
		]);
	});

	it("出力を読めなければ（Tauriの外）ログ無しで表示し、読み直さない", async () => {
		mockInvoke.mockRejectedValue(new Error("not in tauri"));

		render(<SidecarLoading />);

		await waitFor(() => expect(mockInvoke).toHaveBeenCalledTimes(1));
		expect(screen.getByText("Loading...")).toBeInTheDocument();
		expect(screen.queryByRole("log")).not.toBeInTheDocument();
	});
});
