import { act, renderHook, waitFor } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import {
	type Environment,
	environmentContext,
} from "../../context/EnvironmentProvider";
import WorkspaceResourcesProvider from "../../context/WorkspaceResourcesProvider";
import {
	useDatasetSettingsData,
	useDatasetTableConvert,
	useDatasetTableNames,
	useDatasetTablePreview,
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
// WorkspaceResourcesに依存しないフック用に、環境設定だけを渡す
const envWrapper = ({ children }: { children: React.ReactNode }) => (
	<environmentContext.Provider value={mockEnvironment}>
		{children}
	</environmentContext.Provider>
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
		if (params.endpoint.includes("/dataset-setting/table-names")) {
			return Promise.resolve(new Response(JSON.stringify(["t1", "t2"])));
		}
		if (params.endpoint.includes("/dataset-setting/table-convert")) {
			return Promise.resolve(new Response("/work/result"));
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

vi.mock("../../utils/fetchUtils", async (importOriginal) => ({
	...(await importOriginal<typeof import("../../utils/fetchUtils")>()),
	fetchData: mockFetchData,
	fetchAndUpdate: mockFetchAndUpdate,
}));

const srcInfo: DatasetSrcInfo = {
	values: {
		src: "data/src",
		srcType: "csv",
		setting: "",
		encoding: "UTF-8",
		recursive: "false",
		regTableInclude: "",
	},
};

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

	describe("useSrcInfoColumnNamesのテスト", () => {
		const callsTo = (endpoint: string) =>
			mockFetchData.mock.calls.filter(([params]) =>
				params.endpoint.includes(endpoint),
			);

		it("全テーブルの列名を重複なく出現順で返すことを確認", async () => {
			const { result } = renderHook(() => useSrcInfoColumnNames(srcInfo), {
				wrapper: envWrapper,
			});
			await waitFor(() => {
				expect(result.current).toStrictEqual(["id", "name", "age"]);
			});
			expect(callsTo("/dataset-setting/table-preview")).toHaveLength(2);
		});

		// 入力は毎レンダー同一参照にする（新規オブジェクトだとeffectが再発火し続ける）
		it.each<[string, DatasetSrcInfo]>([
			["srcが空", { values: { ...srcInfo.values, src: "" } }],
			[
				"JDBC未接続のsqlソース",
				{ values: { ...srcInfo.values, srcType: "sql" } },
			],
		])("%sのときは取得せず空配列を返すことを確認", async (_, info) => {
			const { result, rerender } = renderHook(
				() => useSrcInfoColumnNames(info),
				{ wrapper: envWrapper },
			);
			await act(async () => {
				rerender();
			});
			expect(result.current).toStrictEqual([]);
			expect(callsTo("/dataset-setting/table-names")).toHaveLength(0);
		});

		it("src・srcType以外の項目が変わっても再取得されることを確認", async () => {
			const { rerender } = renderHook(
				(info: DatasetSrcInfo) => useSrcInfoColumnNames(info),
				{ wrapper: envWrapper, initialProps: srcInfo },
			);
			await waitFor(() => {
				expect(callsTo("/dataset-setting/table-names")).toHaveLength(1);
			});
			rerender({ values: { ...srcInfo.values, regTableInclude: "^t1$" } });
			await waitFor(() => {
				expect(callsTo("/dataset-setting/table-names")).toHaveLength(2);
			});
			const [lastParams] = callsTo("/dataset-setting/table-names")[1];
			expect(JSON.parse(String(lastParams.options.body))).toMatchObject({
				srcOptions: { regTableInclude: "^t1$" },
			});
		});
	});

	describe("Preview用フック（useDatasetTableNames・useDatasetTablePreview）のテスト", () => {
		// 読込中にログを2行流してから失敗する
		const failWithLogs = () =>
			mockFetchData.mockImplementationOnce(async (params: FetchParams) => {
				params.onLog?.("step1");
				params.onLog?.("step2");
				throw new Error("boom");
			});

		// どちらのフックも同じ形（data・loading・logs・error）で見る
		const previewHooks = {
			tableNames: () => {
				const { tableNames, ...rest } = useDatasetTableNames(srcInfo, true);
				return { ...rest, data: tableNames as unknown };
			},
			preview: () => {
				const { preview, ...rest } = useDatasetTablePreview(srcInfo, "t1");
				return { ...rest, data: preview as unknown };
			},
		};
		const renderPreviewHook = (name: keyof typeof previewHooks) =>
			renderHook(previewHooks[name], { wrapper: envWrapper });

		it.each([
			{ name: "tableNames", empty: [] },
			{ name: "preview", empty: null },
		] as const)(
			"$name: 読込に失敗したら、データは空にしてエラーと読込中に届いたログを返すことを確認",
			async ({ name, empty }) => {
				failWithLogs();
				const { result } = renderPreviewHook(name);

				await waitFor(() => {
					expect(result.current.loading).toBe(false);
				});
				expect(result.current.error).toBe("boom");
				expect(result.current.logs).toStrictEqual(["step1", "step2"]);
				expect(result.current.data).toStrictEqual(empty);
			},
		);

		it.each(["tableNames", "preview"] as const)(
			"%s: 読込に成功したらエラーは空であることを確認",
			async (name) => {
				const { result } = renderPreviewHook(name);
				await waitFor(() => {
					expect(result.current.loading).toBe(false);
				});
				expect(result.current.error).toBe("");
			},
		);

		it("入力候補（useSrcInfoColumnNames）は、テーブル一覧の読込に失敗しても空配列のままにすることを確認", async () => {
			failWithLogs();
			const { result } = renderHook(() => useSrcInfoColumnNames(srcInfo), {
				wrapper: envWrapper,
			});
			await waitFor(() => {
				expect(mockFetchData).toHaveBeenCalled();
			});
			await act(async () => {});
			expect(result.current).toStrictEqual([]);
		});
	});

	describe("useDatasetTableConvertのテスト", () => {
		it("読込元・テーブル名・resultの値を送り、出力先ディレクトリを返すことを確認", async () => {
			const { result } = renderHook(() => useDatasetTableConvert(), {
				wrapper: envWrapper,
			});
			const resultDir = await result.current(srcInfo, "t1", {
				"-result.resultType": "csv",
			});

			expect(resultDir).toBe("/work/result");
			const [params] = mockFetchData.mock.calls.filter(([p]) =>
				p.endpoint.includes("/dataset-setting/table-convert"),
			)[0];
			expect(JSON.parse(String(params.options.body))).toMatchObject({
				srcOptions: { src: "data/src", srcType: "csv" },
				tableName: "t1",
				result: { "-result.resultType": "csv" },
			});
		});
	});
});
