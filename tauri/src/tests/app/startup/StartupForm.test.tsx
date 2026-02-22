import {
	act,
	fireEvent,
	render,
	screen,
	waitFor,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import StartupForm from "../../../app/startup/StartupForm";
import {
	type Enviroment,
	enviromentContext,
} from "../../../context/EnviromentProvider";
import WorkspaceResourcesProvider from "../../../context/WorkspaceResourcesProvider";
import type { FetchParams } from "../../../utils/fetchUtils";
import { enviromentFixture, workspaceResourcesFixture } from "../../setup";

const mockEnviroment: Enviroment = { ...enviromentFixture };

function MockProvider({ children }: { children: React.ReactNode }) {
	return (
		<enviromentContext.Provider value={mockEnviroment}>
			<WorkspaceResourcesProvider>{children}</WorkspaceResourcesProvider>
		</enviromentContext.Provider>
	);
}
const wrapper = ({ children }: { children: React.ReactNode }) => (
	<MockProvider>{children}</MockProvider>
);

const { mockFetchData } = vi.hoisted(() => {
	return {
		mockFetchData: vi.fn((_params: FetchParams) => {
			return Promise.resolve({
				json: () =>
					Promise.resolve({
						...workspaceResourcesFixture,
						context: {
							...workspaceResourcesFixture.context,
							workspace: "C:/project/old",
							datasetBase: "C:/project/old/dataset",
							resultBase: "C:/project/old/result",
						},
					}),
			} as Response);
		}),
	};
});
vi.mock("../../../utils/fetchUtils", () => ({
	fetchData: mockFetchData,
}));

const { mockOpen } = vi.hoisted(() => {
	return { mockOpen: vi.fn() };
});
vi.mock("@tauri-apps/plugin-dialog", () => ({
	open: (options?: { defaultPath?: string; directory?: boolean }) =>
		mockOpen(options),
}));

describe("StartupFormのテスト", () => {
	it("初期表示時にworkspace contextの値が入力フィールドに表示される", async () => {
		const { rerender } = render(<StartupForm onSelect={vi.fn()} />, {
			wrapper,
		});
		await act(async () => {
			rerender(<StartupForm onSelect={vi.fn()} />);
		});
		expect(screen.getByDisplayValue("C:/project/old")).toBeInTheDocument();
		expect(
			screen.getByDisplayValue("C:/project/old/dataset"),
		).toBeInTheDocument();
		expect(
			screen.getByDisplayValue("C:/project/old/result"),
		).toBeInTheDocument();
	});

	it("workspaceが空の場合にエラーメッセージが表示される", async () => {
		const onSelect = vi.fn();
		const { rerender } = render(<StartupForm onSelect={onSelect} />, {
			wrapper,
		});
		await act(async () => {
			rerender(<StartupForm onSelect={onSelect} />);
		});
		const workspaceInput = screen.getByDisplayValue("C:/project/old");
		await userEvent.clear(workspaceInput);
		await userEvent.click(screen.getByText("confirm"));
		expect(screen.getByText("Workspace path is required.")).toBeInTheDocument();
		expect(onSelect).not.toHaveBeenCalled();
	});

	it("confirmボタンでonSelectが呼ばれworkspaceUpdateが実行される", async () => {
		const onSelect = vi.fn();
		const { rerender } = render(<StartupForm onSelect={onSelect} />, {
			wrapper,
		});
		await act(async () => {
			rerender(<StartupForm onSelect={onSelect} />);
		});
		await userEvent.click(screen.getByText("confirm"));
		expect(onSelect).toHaveBeenCalled();
		expect(mockFetchData).toHaveBeenCalledWith(
			expect.objectContaining({
				endpoint: "http://localhost:8080/workspace/update",
			}),
		);
	});

	it("workspace変更時にdatasetBaseとresultBaseが前方一致で自動更新される", async () => {
		const { rerender } = render(<StartupForm onSelect={vi.fn()} />, {
			wrapper,
		});
		await act(async () => {
			rerender(<StartupForm onSelect={vi.fn()} />);
		});
		const workspaceInput = screen.getByDisplayValue("C:/project/old");
		fireEvent.change(workspaceInput, {
			target: { value: "C:/project/new" },
		});
		expect(screen.getByDisplayValue("C:/project/new")).toBeInTheDocument();
		expect(
			screen.getByDisplayValue("C:/project/new/dataset"),
		).toBeInTheDocument();
		expect(
			screen.getByDisplayValue("C:/project/new/result"),
		).toBeInTheDocument();
	});

	it("datasetBase/resultBaseがworkspaceと前方一致しない場合は更新されない", async () => {
		mockFetchData.mockImplementation(() =>
			Promise.resolve({
				json: () =>
					Promise.resolve({
						...workspaceResourcesFixture,
						context: {
							...workspaceResourcesFixture.context,
							workspace: "C:/project/old",
							datasetBase: "D:/other/dataset",
							resultBase: "D:/other/result",
						},
					}),
			} as Response),
		);
		const { rerender } = render(<StartupForm onSelect={vi.fn()} />, {
			wrapper,
		});
		await act(async () => {
			rerender(<StartupForm onSelect={vi.fn()} />);
		});
		const workspaceInput = screen.getByDisplayValue("C:/project/old");
		await userEvent.clear(workspaceInput);
		await userEvent.type(workspaceInput, "C:/project/new");
		expect(screen.getByDisplayValue("D:/other/dataset")).toBeInTheDocument();
		expect(screen.getByDisplayValue("D:/other/result")).toBeInTheDocument();
	});

	it("DirectoryChooserでディレクトリ選択するとworkspaceが更新される", async () => {
		mockOpen.mockResolvedValue("C:/selected/path");
		const { rerender } = render(<StartupForm onSelect={vi.fn()} />, {
			wrapper,
		});
		await act(async () => {
			rerender(<StartupForm onSelect={vi.fn()} />);
		});
		const dirButtons = screen.getAllByTitle("DirectoryChooser");
		await userEvent.click(dirButtons[0]);
		await waitFor(() => {
			expect(mockOpen).toHaveBeenCalledWith({
				defaultPath: "C:/project/old",
				directory: true,
			});
		});
		await waitFor(() => {
			expect(screen.getByDisplayValue("C:/selected/path")).toBeInTheDocument();
		});
	});
});
