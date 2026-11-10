import { act, renderHook } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import {
	type Environment,
	environmentContext,
} from "../../context/EnvironmentProvider";
import { ResourceEncodingProvider } from "../../context/ResourceEncodingProvider";
import WorkspaceResourcesProvider from "../../context/WorkspaceResourcesProvider";
import { useResourceLoad } from "../../hooks/useResourceFiles";
import { XLSX_SCHEMA_PARSER } from "../../hooks/useXlsxSchema";
import type { WorkspaceResources } from "../../model/WorkspaceResources";
import type { XlsxSchemaBuilder } from "../../model/XlsxSchema";
import { XlsxSchema } from "../../model/XlsxSchema";
import type { FetchParams } from "../../utils/fetchUtils";
import { environmentFixture, workspaceResourcesFixture } from "../setup";

const mockXlsxSchema: XlsxSchemaBuilder = {
	rows: [
		{
			sheetName: "Sheet1",
			tableName: "Table1",
			header: ["col1", "col2"],
			dataStart: "A2",
			columnIndex: ["A", "B"],
			breakKey: ["col1"],
			addFileInfo: true,
		},
	],
	cells: [
		{
			sheetName: "Sheet1",
			tableName: "Table2",
			header: ["col1", "col2"],
			rows: [{ cellAddress: ["A1", "B1"] }],
			addFileInfo: true,
		},
	],
};
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

const { mockFetchData, mockFetchAndUpdate } = vi.hoisted(() => {
	const mockFetchData = vi.fn((params: FetchParams) => {
		if (params.endpoint.includes("/workspace/resources")) {
			return Promise.resolve(
				new Response(JSON.stringify(mockWorkspaceResources)),
			);
		}
		if (params.endpoint.includes("/xlsx-schema/load")) {
			return Promise.resolve(
				new Response(
					JSON.stringify({
						content: mockXlsxSchema,
						location: {
							folder: "/ws/resources/xlsx-schema",
							defaultFolder: "/ws/resources/xlsx-schema",
							fileName: "test-setting",
							defaultFolderHasFile: true,
						},
					}),
				),
			);
		}
		return Promise.resolve(new Response());
	});
	const mockFetchAndUpdate = vi.fn(
		async (params: FetchParams, onSuccess: (data: unknown) => void) => {
			const response = await mockFetchData(params);
			const data = await response.json();
			onSuccess(data);
			return "success" as const;
		},
	);
	return { mockFetchData, mockFetchAndUpdate };
});

vi.mock("../../utils/fetchUtils", async (importOriginal) => ({
	...(await importOriginal<typeof import("../../utils/fetchUtils")>()),
	fetchData: mockFetchData,
	fetchAndUpdate: mockFetchAndUpdate,
}));

describe("XlsxSchemaProviderのテスト", () => {
	describe("XLSX_SCHEMA_PARSERでの読込（useResourceLoad）", () => {
		it("名前が空文字の場合にデフォルト値を返しloadingがfalseになることを確認", async () => {
			const { result, rerender } = renderHook(
				() => useResourceLoad("xlsxSchema", "", XLSX_SCHEMA_PARSER),
				{
					wrapper,
				},
			);
			await act(async () => {
				rerender();
			});
			expect(result.current.loading).toBe(false);
			expect(result.current.data).toEqual(XlsxSchema.create());
		});

		it("正常なロードが行われることを確認", async () => {
			const { result, rerender } = renderHook(
				() => useResourceLoad("xlsxSchema", "test-setting", XLSX_SCHEMA_PARSER),
				{ wrapper },
			);
			await act(async () => {
				rerender();
			});
			expect(result.current.loading).toBe(false);
			expect(result.current.data.rows).toHaveLength(1);
			expect(result.current.data.cells).toHaveLength(1);
		});

		it("ResourceEncodingProviderのxlsxSchemaの文字コードで読み込むことを確認", async () => {
			mockFetchData.mockClear();
			const { rerender } = renderHook(
				() => useResourceLoad("xlsxSchema", "test-setting", XLSX_SCHEMA_PARSER),
				{
					wrapper: ({ children }: { children: React.ReactNode }) => (
						<MockProvider>
							<ResourceEncodingProvider
								encodings={{ xlsxSchema: "MS932", datasetSetting: "UTF-8" }}
							>
								{children}
							</ResourceEncodingProvider>
						</MockProvider>
					),
				},
			);
			await act(async () => {
				rerender();
			});
			const loads = mockFetchData.mock.calls.filter(([params]) =>
				params.endpoint.endsWith("/xlsx-schema/load"),
			);
			expect(JSON.parse(String(loads[0][0].options.body))).toStrictEqual({
				name: "test-setting",
				encoding: "MS932",
			});
		});
	});
});
