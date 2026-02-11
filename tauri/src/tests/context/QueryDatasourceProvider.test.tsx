import { act, renderHook, waitFor } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { type Enviroment, enviromentContext } from "../../context/EnviromentProvider";
import {
	useDeleteDataSource,
	useLoadDataSource,
	useSaveDataSource,
} from "../../context/QueryDatasourceProvider";
import WorkspaceResourcesProvider, { useResourcesSettings } from "../../context/WorkspaceResourcesProvider";
import type { QueryDatasource } from "../../model/QueryDatasource";
import type { WorkspaceResources } from "../../model/WorkspaceResources";
import type { FetchParams } from "../../utils/fetchUtils";
import { enviromentFixture, workspaceResourcesFixture } from "../setup";

// モックデータ
const mockQueryDatasource: QueryDatasource = {
	type: "sql",
	name: "test-query",
	contents: "SELECT * FROM test_table;",
};

const mockWorkspaceResources: WorkspaceResources = { ...workspaceResourcesFixture };
const mockEnviroment: Enviroment = { ...enviromentFixture };

function MockProvider({ children }: { children: React.ReactNode }) {
	return <enviromentContext.Provider value={mockEnviroment}><WorkspaceResourcesProvider>{children}</WorkspaceResourcesProvider></enviromentContext.Provider>;
}
const wrapper = ({ children }: { children: React.ReactNode }) => (
	<MockProvider>{children}</MockProvider>
);

const mockUpdatedFiles = ['test-query', 'other-query'];
const mockRemainingFiles = ['other-query'];
const mockLoadedContents = "SELECT * FROM test_table WHERE id = 1;";

// API呼び出しのモック
const { mockFetchData } = vi.hoisted(() => {
	return {
		mockFetchData: vi.fn((params: FetchParams) => {
			if (params.endpoint.includes('/workspace/resources')) {
				return Promise.resolve(new Response(JSON.stringify(mockWorkspaceResources)));
			}
			if (params.endpoint.includes('/query-datasource/load')) {
				return Promise.resolve(new Response(mockLoadedContents));
			}
			if (params.endpoint.includes('/query-datasource/save')) {
				return Promise.resolve(new Response(JSON.stringify(mockUpdatedFiles)));
			}
			if (params.endpoint.includes('/query-datasource/delete')) {
				return Promise.resolve(new Response(JSON.stringify(mockRemainingFiles)));
			}
			return Promise.resolve(new Response());
		})
	};
});

vi.mock("../../utils/fetchUtils", () => ({
	fetchData: mockFetchData,
}));

describe("QueryDatasourceProviderのテスト", () => {

	describe("useLoadDataSource", () => {
		it("正常なロードが行われることを確認", async () => {
			const { result, rerender } = renderHook(() => useLoadDataSource(), { wrapper });
            await act(async () => {rerender()});
            expect(result.current).toBeTypeOf("function");
			const res = await result.current("sql", "test-query");
			expect(res).toBe(mockLoadedContents);
		});

		it("csvqタイプでも正常にロードできることを確認", async () => {
			const { result, rerender } = renderHook(() => useLoadDataSource(), { wrapper });
            await act(async () => {rerender()});
            expect(result.current).toBeTypeOf("function");
			const res = await result.current("table", "test-table");
			expect(res).toBe(mockLoadedContents);
		});

		it("tableタイプでも正常にロードできることを確認", async () => {
			const { result, rerender } = renderHook(() => useLoadDataSource(), { wrapper });
            await act(async () => {rerender()});
            expect(result.current).toBeTypeOf("function");
			const res = await result.current("table", "test-table");
			expect(res).toBe(mockLoadedContents);
		});
	});

	describe("useSaveDataSource", () => {
		it("正常な保存が行われることを確認", async () => {
			const { result, rerender } = renderHook(() => {
				const saveDataSource = useSaveDataSource();
				const resources = useResourcesSettings();
				return { resources, saveDataSource }
			}, { wrapper });

            await act(async () => {rerender()});
            expect(result.current.resources.queryFiles).toStrictEqual(mockWorkspaceResources.resources.queryFiles);

			const saveResult = await act(async () => {
				return await result.current.saveDataSource(mockQueryDatasource);
			});
			expect(saveResult).toBe('success');
			expect(mockFetchData).toHaveBeenCalledWith({
				endpoint: `${mockEnviroment.apiUrl}query-datasource/save`,
				options: {
					method: "POST",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify(mockQueryDatasource),
				},
			});
		});

	});

	describe("useDeleteDataSource", () => {
		it("sqlタイプの正常な削除が行われることを確認", async () => {
			const { result, rerender } = renderHook(() => {
				const deleteDataSource = useDeleteDataSource("sql");
				const resources = useResourcesSettings();
				return { resources, deleteDataSource }
			}, { wrapper });

			await waitFor(() => {
				rerender();
				expect(result.current.resources.queryFiles).toStrictEqual(mockWorkspaceResources.resources.queryFiles);
			});

			const deleteResult = await act(async () => {
				return await result.current.deleteDataSource("test-query");
			});
			expect(deleteResult).toBe('success');
			expect(mockFetchData).toHaveBeenCalledWith({
				endpoint: `${mockEnviroment.apiUrl}query-datasource/delete`,
				options: {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({ type: "sql", name: "test-query" }),
				},
			});
		});
	});
});