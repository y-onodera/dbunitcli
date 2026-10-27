import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import SidebarHelpLink from "../../../app/sidebar/SidebarHelpLink";

const { mockOpenHelpWindow } = vi.hoisted(() => {
	return { mockOpenHelpWindow: vi.fn() };
});
vi.mock("../../../utils/helpWindow", () => ({
	openHelpWindow: mockOpenHelpWindow,
}));

describe("SidebarHelpLink", () => {
	it("Getting Started を押すと基本的な使い方のヘルプを開き、メニューを閉じる", async () => {
		const user = userEvent.setup();
		render(<SidebarHelpLink />);

		await user.click(screen.getByRole("button", { name: "Help" }));
		await user.click(screen.getByText("Getting Started"));

		expect(mockOpenHelpWindow).toHaveBeenCalledWith(
			"guide/getting-started",
			"Getting Started",
		);
		expect(screen.queryByText("Getting Started")).toBeNull();
	});

	it("Getting Started をコマンドより先に表示する", async () => {
		const user = userEvent.setup();
		render(<SidebarHelpLink />);

		await user.click(screen.getByRole("button", { name: "Help" }));

		const labels = screen
			.getAllByRole("listitem")
			.map((item) => item.querySelector("span")?.textContent);
		expect(labels).toEqual([
			"Getting Started",
			"Convert",
			"Compare",
			"Generate",
			"Run",
			"Parameterize",
		]);
	});
});
