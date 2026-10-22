import {
	act,
	fireEvent,
	render,
	screen,
	waitFor,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import StartupForm, {
	toWorkspaceRelative,
} from "../../../app/startup/StartupForm";
import {
	type Environment,
	environmentContext,
} from "../../../context/EnvironmentProvider";
import WorkspaceResourcesProvider from "../../../context/WorkspaceResourcesProvider";
import type { FetchParams } from "../../../utils/fetchUtils";
import { environmentFixture, workspaceResourcesFixture } from "../../setup";

const mockEnvironment: Environment = { ...environmentFixture };

function MockProvider({ children }: { children: React.ReactNode }) {
	return (
		<environmentContext.Provider value={mockEnvironment}>
			<WorkspaceResourcesProvider>{children}</WorkspaceResourcesProvider>
		</environmentContext.Provider>
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

	it("EnvのFileChooserでworkspace配下のファイルを選ぶと相対パスになる", async () => {
		mockOpen.mockResolvedValue("C:/project/old/env/staging.properties");
		const { rerender } = render(<StartupForm onSelect={vi.fn()} />, {
			wrapper,
		});
		await act(async () => {
			rerender(<StartupForm onSelect={vi.fn()} />);
		});
		await userEvent.click(screen.getByTitle("FileChooser"));
		await waitFor(() => {
			expect(mockOpen).toHaveBeenCalledWith({
				defaultPath: "C:/project/old",
				directory: false,
				filters: [{ name: "properties", extensions: ["properties"] }],
			});
		});
		await waitFor(() => {
			expect(
				screen.getByDisplayValue("env/staging.properties"),
			).toBeInTheDocument();
		});
	});

	it("confirmでenvがworkspace/updateに送られる", async () => {
		const { rerender } = render(<StartupForm onSelect={vi.fn()} />, {
			wrapper,
		});
		await act(async () => {
			rerender(<StartupForm onSelect={vi.fn()} />);
		});
		fireEvent.change(document.getElementById("env") as HTMLInputElement, {
			target: { value: "env/staging.properties" },
		});
		mockFetchData.mockClear();
		await userEvent.click(screen.getByText("confirm"));
		const body = JSON.parse(
			mockFetchData.mock.calls[0][0].options?.body as string,
		);
		expect(body.env).toBe("env/staging.properties");
	});

	it("toWorkspaceRelativeはworkspace配下だけ相対パスに変換する", () => {
		expect(
			toWorkspaceRelative(
				"C:\\project\\old\\env.properties",
				"C:/Project/old/",
			),
		).toBe("env.properties");
		expect(
			toWorkspaceRelative("D:/other/env.properties", "C:/project/old"),
		).toBe("D:/other/env.properties");
		expect(
			toWorkspaceRelative("C:/project/older/env.properties", "C:/project/old"),
		).toBe("C:/project/older/env.properties");
	});
});
