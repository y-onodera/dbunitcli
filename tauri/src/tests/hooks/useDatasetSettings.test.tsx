import { act, renderHook } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import {
	type Enviroment,
	enviromentContext,
} from "../../context/EnviromentProvider";
import WorkspaceResourcesProvider, {
	useResourcesSettings,
} from "../../context/WorkspaceResourcesProvider";
import {
	useDatasetSettingsData,
	useDeleteDatasetSettings,
	useSaveDatasetSettings,
} from "../../hooks/useDatasetSettings";
import { DatasetSettings } from "../../model/DatasetSettings";
import type { WorkspaceResources } from "../../model/WorkspaceResources";
import type { FetchParams } from "../../utils/fetchUtils";
import { enviromentFixture, workspaceResourcesFixture } from "../setup";

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

// API呼び出しのモック
const { mockFetchData } = vi.hoisted(() => {
	return {
		mockFetchData: vi.fn((params: FetchParams) => {
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
				return Promise.resolve(
					new Response(JSON.stringify(mockUpdatedSettings)),
				);
			}
			if (params.endpoint.includes("/dataset-setting/delete")) {
				return Promise.resolve(
					new Response(JSON.stringify(mockRemainingSettings)),
				);
			}
			return Promise.resolve(new Response());
		}),
	};
});

// 必要なモジュールをモック化
vi.mock("../../utils/fetchUtils", () => ({
	fetchData: mockFetchData,
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
});
