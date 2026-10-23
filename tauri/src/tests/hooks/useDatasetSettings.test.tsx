import { act, renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
	type Environment,
	environmentContext,
} from "../../context/EnvironmentProvider";
import WorkspaceResourcesProvider, {
	useResourcesSettings,
} from "../../context/WorkspaceResourcesProvider";
import {
	useDatasetSettingsData,
	useDeleteDatasetSettings,
	useSaveDatasetSettings,
	useSrcInfoColumnNames,
} from "../../hooks/useDatasetSettings";
import type { DatasetSrcInfo } from "../../model/CommandOption";
import { DatasetSettings } from "../../model/DatasetSettings";
import type { WorkspaceResources } from "../../model/WorkspaceResources";
import type { FetchParams } from "../../utils/fetchUtils";
import { environmentFixture, workspaceResourcesFixture } from "../setup";

// モックデータ
const mockDatasetSettingsResponse: {
	settings: { name: string[] }[];
	commonSettings: unknown[];
} = {
	settings: [{ name: ["test-setting"] }],
	commonSettings: [],
};

const mockUpdatedSettings = ["test-setting", "other-setting"];
const mockRemainingSettings = [] as string[];

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
		if (params.endpoint.includes("/dataset-setting/load")) {
			return Promise.resolve(
				new Response(JSON.stringify(mockDatasetSettingsResponse)),
			);
		}
		if (params.endpoint.includes("/dataset-setting/save")) {
			return Promise.resolve(new Response(JSON.stringify(mockUpdatedSettings)));
		}
		if (params.endpoint.includes("/dataset-setting/delete")) {
			return Promise.resolve(
				new Response(JSON.stringify(mockRemainingSettings)),
			);
		}
		if (params.endpoint.includes("/dataset-setting/table-names")) {
			return Promise.resolve(new Response(JSON.stringify(["t1", "t2"])));
		}
		if (params.endpoint.includes("/dataset-setting/table-preview")) {
			const { tableName } = JSON.parse(String(params.options.body));
			const headers = tableName === "t1" ? ["id", "name"] : ["id", "age"];
			return Promise.resolve(
				new Response(JSON.stringify({ headers, rows: [] })),
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

vi.mock("../../utils/fetchUtils", () => ({
	fetchData: mockFetchData,
	fetchAndUpdate: mockFetchAndUpdate,
}));

describe("DatasetSettingsProviderのテスト", () => {
	describe("useDatasetSettingsDataのテスト", () => {
		it("設定ファイル名が空白のときは初期値が返却されloadingがfalseになることを確認", async () => {
			const { result, rerender } = renderHook(
				() => useDatasetSettingsData(""),
				{ wrapper },
			);
			await act(async () => {
				rerender();
			});
			expect(result.current.loading).toBe(false);
			expect(result.current.settings).toEqual(DatasetSettings.create());
		});
		it("データセット設定を正常に読み込めることを確認", async () => {
			const { result, rerender } = renderHook(
				() => useDatasetSettingsData("test-setting"),
				{ wrapper },
			);
			await act(async () => {
				rerender();
			});
			expect(result.current.loading).toBe(false);
			expect(result.current.settings.settings).toHaveLength(1);
			expect(result.current.settings.settings[0].name).toStrictEqual(
				mockDatasetSettingsResponse.settings[0].name,
			);
		});
	});

	describe("useSaveDatasetSettingsのテスト", () => {
		it("データセット設定を正常に保存できることを確認", async () => {
			const { result, rerender } = renderHook(
				() => {
					const saveDatasetSettings = useSaveDatasetSettings();
					const resources = useResourcesSettings();
					return { resources, saveDatasetSettings };
				},
				{ wrapper },
			);
			await act(async () => {
				rerender();
			});
			expect(result.current.resources.datasetSettings).toStrictEqual(
				mockWorkspaceResources.resources.datasetSettings,
			);
			await act(async () => {
				result.current.saveDatasetSettings(
					"test-setting",
					DatasetSettings.create(),
				);
			});
			expect(result.current.resources.datasetSettings).toStrictEqual(
				mockUpdatedSettings,
			);
		});
	});

	describe("useDeleteDatasetSettingsのテスト", () => {
		it("データセット設定を正常に削除できることを確認", async () => {
			const { result, rerender } = renderHook(
				() => {
					const deleteDatasetSettings = useDeleteDatasetSettings();
					const resources = useResourcesSettings();
					return { resources, deleteDatasetSettings };
				},
				{ wrapper },
			);
			await act(async () => {
				rerender();
			});
			expect(result.current.resources.datasetSettings).toStrictEqual(
				mockWorkspaceResources.resources.datasetSettings,
			);
			await act(async () => {
				result.current.deleteDatasetSettings("test-setting");
			});
			expect(result.current.resources.datasetSettings).toStrictEqual(
				mockRemainingSettings,
			);
		});
	});

	describe("useSrcInfoColumnNamesのテスト", () => {
		const srcInfo: DatasetSrcInfo = {
			srcPath: "data/src",
			srcType: "csv",
			setting: "",
			settingEncoding: "",
			encoding: "UTF-8",
			regInclude: "",
			regExclude: "",
			recursive: "false",
			extension: "",
			regTableInclude: "",
			regTableExclude: "",
			xlsxSchema: "",
			fixedLength: "",
			fixedLengthType: "",
			regHeaderSplit: "",
			regDataSplit: "",
			delimiter: ",",
			ignoreQuoted: false,
			headerName: "",
			startRow: "",
			addFileInfo: false,
		};
		const callsTo = (endpoint: string) =>
			mockFetchData.mock.calls.filter(([params]) =>
				params.endpoint.includes(endpoint),
			);

		// フックはWorkspaceResourcesに依存しないため環境設定だけを渡す
		const envWrapper = ({ children }: { children: React.ReactNode }) => (
			<environmentContext.Provider value={mockEnvironment}>
				{children}
			</environmentContext.Provider>
		);

		beforeEach(() => {
			mockFetchData.mockClear();
		});

		it("全テーブルの列名を重複なく出現順で返すことを確認", async () => {
			const { result } = renderHook(() => useSrcInfoColumnNames(srcInfo), {
				wrapper: envWrapper,
			});
			await waitFor(() => {
				expect(result.current).toStrictEqual(["id", "name", "age"]);
			});
			expect(callsTo("/dataset-setting/table-preview")).toHaveLength(2);
		});

		it("srcPathが空のときは取得せず空配列を返すことを確認", async () => {
			const noPath: DatasetSrcInfo = { ...srcInfo, srcPath: "" };
			const { result, rerender } = renderHook(
				() => useSrcInfoColumnNames(noPath),
				{ wrapper: envWrapper },
			);
			await act(async () => {
				rerender();
			});
			expect(result.current).toStrictEqual([]);
			expect(callsTo("/dataset-setting/table-names")).toHaveLength(0);
		});

		it("JDBC未接続のsqlソースでは取得しないことを確認", async () => {
			const sqlSrc: DatasetSrcInfo = { ...srcInfo, srcType: "sql" };
			const { result, rerender } = renderHook(
				() => useSrcInfoColumnNames(sqlSrc),
				{ wrapper: envWrapper },
			);
			await act(async () => {
				rerender();
			});
			expect(result.current).toStrictEqual([]);
			expect(callsTo("/dataset-setting/table-names")).toHaveLength(0);
		});

		it("srcPath・srcType以外の項目が変わっても再取得されることを確認", async () => {
			const { rerender } = renderHook(
				(info: DatasetSrcInfo) => useSrcInfoColumnNames(info),
				{ wrapper: envWrapper, initialProps: srcInfo },
			);
			await waitFor(() => {
				expect(callsTo("/dataset-setting/table-names")).toHaveLength(1);
			});
			rerender({ ...srcInfo, regTableInclude: "^t1$" });
			await waitFor(() => {
				expect(callsTo("/dataset-setting/table-names")).toHaveLength(2);
			});
			const [lastParams] = callsTo("/dataset-setting/table-names")[1];
			expect(JSON.parse(String(lastParams.options.body))).toMatchObject({
				regTableInclude: "^t1$",
			});
		});
	});
});
