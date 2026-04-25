import { act, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import type { ReactNode } from "react";
import { describe, expect, it, vi } from "vitest";
import Header from "../../../app/main/Header";
import {
	type Environment,
	environmentContext,
} from "../../../context/EnvironmentProvider";
import SelectParameterProvider from "../../../context/SelectParameterProvider";
import WorkspaceResourcesProvider from "../../../context/WorkspaceResourcesProvider";
import type { FetchParams } from "../../../utils/fetchUtils";
import { environmentFixture, workspaceResourcesFixture } from "../../setup";

const mockEnvironment: Environment = { ...environmentFixture };

function MockProvider({ children }: { children: ReactNode }) {
	return (
		<environmentContext.Provider value={mockEnvironment}>
			<WorkspaceResourcesProvider>
				<SelectParameterProvider>{children}</SelectParameterProvider>
			</WorkspaceResourcesProvider>
		</environmentContext.Provider>
	);
}
const wrapper = ({ children }: { children: ReactNode }) => (
	<MockProvider>{children}</MockProvider>
);

const { mockFetchData } = vi.hoisted(() => {
	return {
		mockFetchData: vi.fn((_params: FetchParams) => {
			return Promise.resolve({
				json: () => Promise.resolve(workspaceResourcesFixture),
			} as Response);
		}),
	};
});
vi.mock("../../../utils/fetchUtils", () => ({
	fetchData: mockFetchData,
	handleFetchError: vi.fn(),
}));

vi.mock("@tauri-apps/plugin-dialog", () => ({
	open: vi.fn(),
}));

// jsdomにはshowModal/closeが未実装のためモック
HTMLDialogElement.prototype.showModal = vi.fn(function (
	this: HTMLDialogElement,
) {
	this.setAttribute("open", "");
});
HTMLDialogElement.prototype.close = vi.fn(function (this: HTMLDialogElement) {
	this.removeAttribute("open");
});

describe("Headerのテスト", () => {
	it("タイトル「DBunit CLI」が表示される", async () => {
		const { rerender } = render(<Header />, { wrapper });
		await act(async () => {
			rerender(<Header />);
		});
		expect(screen.getByText("DBunit CLI")).toBeInTheDocument();
	});

	it("ChangeWorkspaceボタンが表示される", async () => {
		const { rerender } = render(<Header />, { wrapper });
		await act(async () => {
			rerender(<Header />);
		});
		expect(
			screen.getByRole("button", { name: "ChangeWorkspace" }),
		).toBeInTheDocument();
	});

	it("ChangeWorkspaceボタンをクリックするとダイアログが表示される", async () => {
		const { rerender } = render(<Header />, { wrapper });
		await act(async () => {
			rerender(<Header />);
		});
		await userEvent.click(
			screen.getByRole("button", { name: "ChangeWorkspace" }),
		);
		expect(screen.getByRole("dialog")).toBeInTheDocument();
		expect(HTMLDialogElement.prototype.showModal).toHaveBeenCalled();
	});

	it("ダイアログ内にStartupFormのフィールドが表示される", async () => {
		const { rerender } = render(<Header />, { wrapper });
		await act(async () => {
			rerender(<Header />);
		});
		await userEvent.click(
			screen.getByRole("button", { name: "ChangeWorkspace" }),
		);
		expect(screen.getByDisplayValue("test-workspace")).toBeInTheDocument();
		expect(screen.getByDisplayValue("dataset")).toBeInTheDocument();
		expect(screen.getByDisplayValue("result")).toBeInTheDocument();
	});

	it("Closeボタンをクリックするとダイアログが閉じる", async () => {
		const { rerender } = render(<Header />, { wrapper });
		await act(async () => {
			rerender(<Header />);
		});
		await userEvent.click(
			screen.getByRole("button", { name: "ChangeWorkspace" }),
		);
		expect(screen.getByRole("dialog")).toBeInTheDocument();
		await userEvent.click(screen.getByText("Close"));
		expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
	});

	it("confirmボタンをクリックするとダイアログが閉じる", async () => {
		const { rerender } = render(<Header />, { wrapper });
		await act(async () => {
			rerender(<Header />);
		});
		await userEvent.click(
			screen.getByRole("button", { name: "ChangeWorkspace" }),
		);
		expect(screen.getByRole("dialog")).toBeInTheDocument();
		await userEvent.click(screen.getByText("confirm"));
		expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
	});
});
