import {
	act,
	render,
	renderHook,
	screen,
	waitFor,
} from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import {
	type Enviroment,
	enviromentContext,
} from "../../context/EnviromentProvider";
import WorkspaceResourcesProvider, {
	useParameterList,
	useResourcesSettings,
	useWorkspaceContext,
} from "../../context/WorkspaceResourcesProvider";
import {
	useAddParameter,
	useParameterActions,
	useWorkspaceUpdate,
} from "../../hooks/useWorkspaceResources";
import type { WorkspaceResources } from "../../model/WorkspaceResources";
import {
	ParameterList,
	ResourcesSettings,
	WorkspaceContext,
} from "../../model/WorkspaceResources";
import type { FetchParams } from "../../utils/fetchUtils";
import { enviromentFixture, workspaceResourcesFixture } from "../setup";

// モックデータ
const mockWorkspaceResources: WorkspaceResources = {
	...workspaceResourcesFixture,
};
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
			if (params.endpoint === "http://localhost:8080/convert/add") {
				return Promise.resolve({
					json: () => Promise.resolve(["convert1", "convert2", "add"]),
				} as Response);
			}
			if (params.endpoint === "http://localhost:8080/convert/delete") {
				return Promise.resolve({
					json: () => Promise.resolve(["convert1"]),
				} as Response);
			}
			if (params.endpoint === "http://localhost:8080/convert/copy") {
				return Promise.resolve({
					json: () => Promise.resolve(["convert1", "convert2", "copy"]),
				} as Response);
			}
			if (params.endpoint === "http://localhost:8080/convert/rename") {
				return Promise.resolve({
					json: () => Promise.resolve(["newName", "convert2"]),
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

	describe("パラメータ操作のテスト", () => {
		it("useAddParameterが正常に動作することを確認", async () => {
			const { result, rerender } = renderHook(
				() => {
					const addConvert = useAddParameter("convert");
					const parameterList = useParameterList();
					return { parameterList, addConvert };
				},
				{ wrapper },
			);
			await act(async () => {
				rerender();
			});
			expect(result.current.parameterList.convert).toEqual([
				"convert1",
				"convert2",
			]);
			await act(async () => {
				result.current.addConvert();
			});
			expect(result.current.parameterList.convert).toEqual([
				"convert1",
				"convert2",
				"add",
			]);
		});

		it("useParameterActionsが正常に動作することを確認", async () => {
			const { result, rerender } = renderHook(
				() => {
					const deleteActions = useParameterActions("convert", "convert2");
					const copyActions = useParameterActions("convert", "convert1");
					const renameActions = useParameterActions("convert", "convert1");
					const parameterList = useParameterList();
					return { parameterList, deleteActions, copyActions, renameActions };
				},
				{ wrapper },
			);
			await act(async () => {
				rerender();
			});
			expect(result.current.parameterList.convert).toEqual([
				"convert1",
				"convert2",
			]);

			await act(async () => {
				result.current.deleteActions.handleDelete();
			});
			expect(result.current.parameterList.convert).toEqual(["convert1"]);

			await act(async () => {
				result.current.copyActions.handleCopy();
			});
			expect(result.current.parameterList.convert).toEqual([
				"convert1",
				"convert2",
				"copy",
			]);

			await act(async () => {
				result.current.renameActions.handleRename("newName");
			});
			expect(result.current.parameterList.convert).toEqual([
				"newName",
				"convert2",
			]);
		});
	});
});
