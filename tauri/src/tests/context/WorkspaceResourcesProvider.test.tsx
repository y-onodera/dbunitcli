import {
	act,
	render,
	renderHook,
	screen,
	waitFor,
} from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import {
	type Environment,
	environmentContext,
} from "../../context/EnvironmentProvider";
import WorkspaceResourcesProvider, {
	useParameterList,
	useResourcesSettings,
	useWorkspaceContext,
} from "../../context/WorkspaceResourcesProvider";
import { useWorkspaceUpdate } from "../../hooks/useWorkspaceResources";
import type { WorkspaceResources } from "../../model/WorkspaceResources";
import {
	ParameterList,
	ResourcesSettings,
	WorkspaceContext,
} from "../../model/WorkspaceResources";
import type { FetchParams } from "../../utils/fetchUtils";
import { environmentFixture, workspaceResourcesFixture } from "../setup";

// モックデータ
const mockWorkspaceResources: WorkspaceResources = {
	...workspaceResourcesFixture,
};
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
		mockFetchData: vi.fn((params: FetchParams) => {
			if (params.endpoint === "http://localhost:8080/workspace/update") {
				const body = JSON.parse(params.options.body as string);
				return Promise.resolve({
					json: () =>
						Promise.resolve({
							context: {
								...mockWorkspaceResources.context,
								workspace: body.workspace,
								datasetBase: body.datasetBase,
								resultBase: body.resultBase,
							},
							parameterList: mockWorkspaceResources.parameterList,
							resources: mockWorkspaceResources.resources,
						}),
				} as Response);
			}
			return Promise.resolve({
				json: () => Promise.resolve(mockWorkspaceResources),
			} as Response);
		}),
	};
});
vi.mock("../../utils/fetchUtils", () => ({
	fetchData: mockFetchData,
}));
describe("WorkspaceResourcesProviderのテスト", () => {
	describe("Workspaceのテスト", () => {
		it("suspend中のloading表示の確認", async () => {
			render(<div>test</div>, { wrapper });
			await waitFor(() => {
				expect(screen.getByText("Loading...")).toBeInTheDocument();
			});
		});
		it("suspendが解決されるとcontextがセットされる", async () => {
			const { result, rerender } = renderHook(
				() => ({
					context: useWorkspaceContext(),
					parameterList: useParameterList(),
					resourcesSettings: useResourcesSettings(),
				}),
				{ wrapper },
			);
			await act(async () => {
				rerender();
			});
			expect(result.current.context).toStrictEqual(
				WorkspaceContext.from(mockWorkspaceResources.context),
			);
			expect(result.current.parameterList).toStrictEqual(
				ParameterList.from(mockWorkspaceResources.parameterList),
			);
			expect(result.current.resourcesSettings).toStrictEqual(
				ResourcesSettings.from(mockWorkspaceResources.resources),
			);
		});
		it("useWorkspaceUpdateが正常に動作することを確認", async () => {
			const { result, rerender } = renderHook(
				() => {
					const workspaceUpdate = useWorkspaceUpdate();
					const context = useWorkspaceContext();
					return { context, workspaceUpdate };
				},
				{ wrapper },
			);
			await act(async () => {
				rerender();
			});
			expect(result.current.context.workspace).toBe("test-workspace");
			expect(result.current.context.datasetBase).toBe("dataset");
			expect(result.current.context.resultBase).toBe("result");
			await act(async () => {
				result.current.workspaceUpdate(
					"new-workspace",
					"new-dataset",
					"new-result",
				);
			});
			expect(result.current.context.workspace).toBe("new-workspace");
			expect(result.current.context.datasetBase).toBe("new-dataset");
			expect(result.current.context.resultBase).toBe("new-result");
		});
	});

});
