import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import AppErrorBoundary from "../../../app/startup/AppErrorBoundary";

function Thrower(): never {
	throw new Error("boom");
}

describe("AppErrorBoundaryのテスト", () => {
	it("例外が無ければ子をそのまま表示する", () => {
		render(
			<AppErrorBoundary>
				<div>content</div>
			</AppErrorBoundary>,
		);
		expect(screen.getByText("content")).toBeInTheDocument();
	});

	it("描画中に例外が起きたら、白画面ではなくエラーメッセージを表示する", () => {
		vi.spyOn(console, "error").mockImplementation(() => {});
		render(
			<AppErrorBoundary>
				<Thrower />
			</AppErrorBoundary>,
		);
		expect(screen.getByText("Error: boom")).toBeInTheDocument();
	});
});
