import { act, renderHook, waitFor } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import {
	type Environment,
	environmentContext,
} from "../../context/EnvironmentProvider";
import {
	useDataSourceContent,
	useDeleteDataSource,
	useSaveDataSource,
} from "../../hooks/useQueryDatasource";
import type { QueryDatasource } from "../../model/QueryDatasource";
import type { FetchParams } from "../../utils/fetchUtils";
import { environmentFixture } from "../setup";

// モックデータ
const mockQueryDatasource: QueryDatasource = {
	name: "test-query",
	contents: "SELECT * FROM test_table;",
};

const mockEnvironment: Environment = { ...environmentFixture };

const wrapper = ({ children }: { children: React.ReactNode }) => (
	<environmentContext.Provider value={mockEnvironment}>
		{children}
	</environmentContext.Provider>
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
	describe("useDataSourceContent", () => {
		it("名前を指定するとその内容を読み込む", async () => {
			const { result } = renderHook(() => useDataSourceContent("test-query"), {
				wrapper,
			});
			expect(result.current.loading).toBe(true);

			await waitFor(() => expect(result.current.loading).toBe(false));
			expect(result.current.content).toBe(mockLoadedContents);
			expect(mockFetchData).toHaveBeenCalledWith({
				endpoint: `${mockEnvironment.apiUrl}query-datasource/load`,
				options: {
					method: "POST",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify({ name: "test-query" }),
				},
			});
		});

		it("名前が空なら読み込まず空の内容を返す", () => {
			const { result } = renderHook(() => useDataSourceContent(""), {
				wrapper,
			});

			expect(result.current).toStrictEqual({ content: "", loading: false });
			expect(mockFetchData).not.toHaveBeenCalled();
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
				endpoint: `${mockEnvironment.apiUrl}query-datasource/save`,
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
				endpoint: `${mockEnvironment.apiUrl}query-datasource/delete`,
				options: {
					method: "POST",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify({ type: "sql", name: "test-query" }),
				},
			});
		});
	});
});
