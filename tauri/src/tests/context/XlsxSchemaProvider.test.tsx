import { renderHook, waitFor } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { type Enviroment, enviromentContext } from "../../context/EnviromentProvider";
import WorkspaceResourcesProvider, { useResourcesSettings } from "../../context/WorkspaceResourcesProvider";
import {
	useDeleteXlsxSchema,
	useLoadXlsxSchema,
	useSaveXlsxSchema,
} from "../../context/XlsxSchemaProvider";
import type { WorkspaceResources } from "../../model/WorkspaceResources";
import { XlsxSchema } from "../../model/XlsxSchema";
import type { XlsxSchemaBuilder } from "../../model/XlsxSchema";
import type { FetchParams } from "../../utils/fetchUtils";
import { enviromentFixture, workspaceResourcesFixture } from "../setup";

// モックデータ
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
const mockWorkspaceResources: WorkspaceResources = { ...workspaceResourcesFixture };
const mockEnviroment: Enviroment = { ...enviromentFixture };

function MockProvider({ children }: { children: React.ReactNode }) {
	return <enviromentContext.Provider value={mockEnviroment}><WorkspaceResourcesProvider>{children}</WorkspaceResourcesProvider></enviromentContext.Provider>;
}
const wrapper = ({ children }: { children: React.ReactNode }) => (
	<MockProvider>{children}</MockProvider>
);

const mockUpdatedSettings = ['test-setting', 'other-setting'];
const mockRemainingSettings = [] as string[];

// API呼び出しのモック
const { mockFetchData } = vi.hoisted(() => {
	return {
		mockFetchData: vi.fn((params: FetchParams) => {
			if (params.endpoint.includes('/workspace/resources')) {
				return Promise.resolve(new Response(JSON.stringify(mockWorkspaceResources)));
			}
			if (params.endpoint.includes('/xlsx-schema/load')) {
				return Promise.resolve(new Response(JSON.stringify(mockXlsxSchema)));
			}
			if (params.endpoint.includes('/xlsx-schema/save')) {
				return Promise.resolve(new Response(JSON.stringify(mockUpdatedSettings)));
			}
			if (params.endpoint.includes('/xlsx-schema/delete')) {
				return Promise.resolve(new Response(JSON.stringify(mockRemainingSettings)));
			}
			return Promise.resolve(new Response());
		})
	};
});

vi.mock("../../utils/fetchUtils", () => ({
	fetchData: mockFetchData,
}));


describe("XlsxSchemaProviderのテスト", () => {

	describe("useLoadXlsxSchema", () => {
		it("名前が空文字の場合にデフォルト値を返すことを確認", async () => {
			const { result } = renderHook(() => useLoadXlsxSchema()(''), { wrapper });
			await waitFor(() => {
				result.current.then((res) => {
					expect(res).toEqual(XlsxSchema.create());
				});
			});
		});

		it("正常なロードが行われることを確認", async () => {
			const { result } = renderHook(() => useLoadXlsxSchema()('test-setting'), { wrapper });
			await waitFor(() => {
				result.current.then((res) => {
					expect(res.rows).toHaveLength(1);
					expect(res.cells).toHaveLength(1);
				});
			});
		});
	});

	describe("useSaveXlsxSchema", () => {
		it("正常な保存が行われることを確認", async () => {
			const { result } = renderHook(() => {
				const saveXlsxSchema = useSaveXlsxSchema();
				const resources = useResourcesSettings();
				return { resources, saveXlsxSchema }
			}, { wrapper });
			await waitFor(() => {
				expect(result.current.resources.xlsxSchemas).toStrictEqual(mockWorkspaceResources.resources.xlsxSchemas);
			});
			result.current.saveXlsxSchema('test-setting', XlsxSchema.create());
			await waitFor(() => {
				expect(result.current.resources.xlsxSchemas).toStrictEqual(mockUpdatedSettings);
			});
		});
	});

	describe("useDeleteXlsxSchema", () => {
		it("正常な削除が行われることを確認", async () => {
			const { result } = renderHook(() => {
				const deleteXlsxSchema = useDeleteXlsxSchema();
				const resources = useResourcesSettings();
				return { resources, deleteXlsxSchema }
			}, { wrapper });
			await waitFor(() => {
				expect(result.current.resources.xlsxSchemas).toStrictEqual(mockWorkspaceResources.resources.xlsxSchemas);
			});
			result.current.deleteXlsxSchema('test-setting');
			await waitFor(() => {
				expect(result.current.resources.xlsxSchemas).toStrictEqual(mockRemainingSettings);
			});
		});
	});
});
