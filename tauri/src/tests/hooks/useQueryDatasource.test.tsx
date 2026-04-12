import { act, renderHook } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import {
	type Enviroment,
	enviromentContext,
} from "../../context/EnviromentProvider";
import {
	useDeleteDataSource,
	useLoadDataSource,
	useSaveDataSource,
} from "../../hooks/useQueryDatasource";
import type { QueryDatasource } from "../../model/QueryDatasource";
import type { FetchParams } from "../../utils/fetchUtils";
import { enviromentFixture } from "../setup";

// モックデータ
const mockQueryDatasource: QueryDatasource = {
	name: "test-query",
	contents: "SELECT * FROM test_table;",
};

const mockEnviroment: Enviroment = { ...enviromentFixture };

const wrapper = ({ children }: { children: React.ReactNode }) => (
	<enviromentContext.Provider value={mockEnviroment}>
		{children}
	</enviromentContext.Provider>
);

const mockLoadedContents = "SELECT * FROM test_table WHERE id = 1;";

// API呼び出しのモック
const { mockFetchData } = vi.hoisted(() => {
	return {
		mockFetchData: vi.fn((params: FetchParams) => {
			if (params.endpoint.includes("/query-datasource/load")) {
				return Promise.resolve(new Response(mockLoadedContents));
			}
			if (params.endpoint.includes("/query-datasource/save")) {
				return Promise.resolve(new Response("success"));
			}
			if (params.endpoint.includes("/query-datasource/delete")) {
				return Promise.resolve(new Response("success"));
			}
			return Promise.resolve(new Response());
		}),
	};
});

vi.mock("../../utils/fetchUtils", () => ({
	fetchData: mockFetchData,
}));

describe("QueryDatasourceProviderのテスト", () => {
	describe("useLoadDataSource", () => {
		it("正常なロードが行われることを確認", async () => {
			const { result } = renderHook(() => useLoadDataSource(), { wrapper });
			expect(result.current).toBeTypeOf("function");
			const res = await result.current("test-query");
			expect(res).toBe(mockLoadedContents);
		});

		it("csvqタイプでも正常にロードできることを確認", async () => {
			const { result } = renderHook(() => useLoadDataSource(), { wrapper });
			expect(result.current).toBeTypeOf("function");
			const res = await result.current("test-query");
			expect(res).toBe(mockLoadedContents);
		});

		it("tableタイプでも正常にロードできることを確認", async () => {
			const { result } = renderHook(() => useLoadDataSource(), { wrapper });
			expect(result.current).toBeTypeOf("function");
			const res = await result.current("test-table");
			expect(res).toBe(mockLoadedContents);
		});
	});

	describe("useSaveDataSource", () => {
		it("正常な保存が行われることを確認", async () => {
			const { result } = renderHook(() => useSaveDataSource(), { wrapper });
			const saveResult = await act(async () =>
				result.current(mockQueryDatasource),
			);
			expect(saveResult).toBe("success");
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
			const { result } = renderHook(() => useDeleteDataSource("sql"), {
				wrapper,
			});
			const deleteResult = await act(async () => result.current("test-query"));
			expect(deleteResult).toBe("success");
			expect(mockFetchData).toHaveBeenCalledWith({
				endpoint: `${mockEnviroment.apiUrl}query-datasource/delete`,
				options: {
					method: "POST",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify({ type: "sql", name: "test-query" }),
				},
			});
		});
	});
});
