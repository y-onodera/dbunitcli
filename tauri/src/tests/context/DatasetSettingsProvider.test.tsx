import { renderHook, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { useDeleteDatasetSettings, useLoadDatasetSettings, useSaveDatasetSettings } from '../../context/DatasetSettingsProvider';
import { type Enviroment, enviromentContext } from '../../context/EnviromentProvider';
import WorkspaceResourcesProvider, { useResourcesSettings } from '../../context/WorkspaceResourcesProvider';
import { DatasetSettings } from '../../model/DatasetSettings';
import type { WorkspaceResources } from '../../model/WorkspaceResources';
import type { FetchParams } from '../../utils/fetchUtils';
import { enviromentFixture, workspaceResourcesFixture } from '../setup';

// モックデータ
const mockDatasetSettingsResponse: {
    settings: { name: string[] }[];
    commonSettings: unknown[];
} = {
    settings: [{ name: ['test-setting'] }],
    commonSettings: []
};

const mockUpdatedSettings = ['test-setting', 'other-setting'];
const mockRemainingSettings = [] as string[];

const mockWorkspaceResources: WorkspaceResources = { ...workspaceResourcesFixture };
const mockEnviroment: Enviroment = { ...enviromentFixture };

function MockProvider({ children }: { children: React.ReactNode }) {
    return <enviromentContext.Provider value={mockEnviroment}><WorkspaceResourcesProvider>{children}</WorkspaceResourcesProvider></enviromentContext.Provider>;
}
const wrapper = ({ children }: { children: React.ReactNode }) => (
    <MockProvider>{children}</MockProvider>
);

// API呼び出しのモック
const { mockFetchData } = vi.hoisted(() => {
    return {
        mockFetchData: vi.fn((params: FetchParams) => {
            if (params.endpoint.includes('/workspace/resources')) {
                return Promise.resolve(new Response(JSON.stringify(mockWorkspaceResources)));
            }
            if (params.endpoint.includes('/dataset-setting/load')) {
                return Promise.resolve(new Response(JSON.stringify(mockDatasetSettingsResponse)));
            }
            if (params.endpoint.includes('/dataset-setting/save')) {
                return Promise.resolve(new Response(JSON.stringify(mockUpdatedSettings)));
            }
            if (params.endpoint.includes('/dataset-setting/delete')) {
                return Promise.resolve(new Response(JSON.stringify(mockRemainingSettings)));
            }
            return Promise.resolve(new Response());
        })
    };
});

// 必要なモジュールをモック化
vi.mock('../../utils/fetchUtils', () => ({
    fetchData: mockFetchData
}));

describe('DatasetSettingsProviderのテスト', () => {

    describe('useLoadDatasetSettingsのテスト', () => {
        it('設定ファイル名が空白のときは初期値が返却されることを確認', async () => {
            const { result, rerender } = renderHook(() => {
                return useLoadDatasetSettings()('');
            }, { wrapper });
            await waitFor(() => {
                rerender();
                result.current.then((res) => {
                    expect(res).toEqual(DatasetSettings.create());
                });
            });
        });
        it('データセット設定を正常に読み込めることを確認', async () => {
            const { result, rerender } = renderHook(() => {
                return useLoadDatasetSettings()('test-setting');
            }, { wrapper });
            await waitFor(() => {
                rerender();
                result.current.then((res) => {
                    expect(res.settings).toHaveLength(1);
                    expect(res.settings[0].name).toStrictEqual(mockDatasetSettingsResponse.settings[0].name);
                });
            });
        });
    });

    describe('useSaveDatasetSettingsのテスト', () => {
        it('データセット設定を正常に保存できることを確認', async () => {
            const { result, rerender } = renderHook(() => {
                const saveDatasetSettings = useSaveDatasetSettings();
                const resources = useResourcesSettings();
                return { resources, saveDatasetSettings }
            }, { wrapper });
            await waitFor(() => {
                rerender();
                expect(result.current.resources.metadataSetting).toStrictEqual(mockWorkspaceResources.resources.metadataSetting);
            });
            result.current.saveDatasetSettings('test-setting', DatasetSettings.create());
            await waitFor(() => {
                expect(result.current.resources.metadataSetting).toStrictEqual(mockUpdatedSettings);
            });
        });
    });

    describe('useDeleteDatasetSettingsのテスト', () => {
        it('データセット設定を正常に削除できることを確認', async () => {
            const { result, rerender } = renderHook(() => {
                const deleteDatasetSettings = useDeleteDatasetSettings();
                const resources = useResourcesSettings();
                return { resources, deleteDatasetSettings }
            }, { wrapper });
            await waitFor(() => {
                rerender();
                expect(result.current.resources.metadataSetting).toStrictEqual(mockWorkspaceResources.resources.metadataSetting);
            });
            result.current.deleteDatasetSettings('test-setting');
            await waitFor(() => {
                expect(result.current.resources.metadataSetting).toStrictEqual(mockRemainingSettings);
            });
        });
    });
});