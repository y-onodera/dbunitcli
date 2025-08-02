import { renderHook, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { type Enviroment, enviromentContext } from '../../context/EnviromentProvider';
import { useDeleteDataSource, useLoadDataSource, useQueryDatasource, useSaveDataSource } from '../../context/QueryDatasourceProvider';
import QueryDatasourceProvider from '../../context/QueryDatasourceProvider';
import type { FetchParams } from '../../utils/fetchUtils';
import { enviromentFixture } from '../setup';

// モックの設定
const mockType = 'sql';
const mockDatasources = ['datasource1', 'datasource2'];
const mockLoadBody = 'text content';
const mockUpdatedSettings = ['test-setting', 'other-setting'];
const mockRemainingSettings = [] as string[];

const { mockFetchData } = vi.hoisted(() => {
    return {
        mockFetchData: vi.fn((params: FetchParams) => {
            if (params.endpoint.includes(`/query-datasource/list?type=${mockType}`)) {
                return Promise.resolve(new Response(JSON.stringify(mockDatasources)));
            }
            if (params.endpoint.includes('/query-datasource/load')) {
                return Promise.resolve(new Response(mockLoadBody));
            }
            if (params.endpoint.includes('/query-datasource/save')) {
                return Promise.resolve(new Response(JSON.stringify(mockUpdatedSettings)));
            }
            if (params.endpoint.includes('/query-datasource/delete')) {
                return Promise.resolve(new Response(JSON.stringify(mockRemainingSettings)));
            }
            return Promise.resolve(new Response());
        })
    };
});
vi.mock('../../utils/fetchUtils', () => ({
    fetchData: mockFetchData,
}));

const mockEnviroment: Enviroment = { ...enviromentFixture };

const wrapper = ({ children }: { children: React.ReactNode }) => (
    <enviromentContext.Provider value={mockEnviroment}>
        <QueryDatasourceProvider type={mockType}>
            {children}
        </QueryDatasourceProvider>
    </enviromentContext.Provider>
);

describe('QueryDatasourceProviderのテスト', () => {

    describe('useQueryDatasource', () => {
        it('初期状態が指定したタイプでfetchした結果であることを確認', async () => {
            const { result, rerender } = renderHook(() => useQueryDatasource(), { wrapper });
            await waitFor(() => {
                rerender();
                expect(result.current).toEqual(mockDatasources);
            });
        });
    });

    describe('useSaveDataSource', () => {
        it('正しくデータソースを保存できることを確認', async () => {
            const { result, rerender } = renderHook(() => {
                const saveDataSource = useSaveDataSource();
                const resources = useQueryDatasource();
                return { resources, saveDataSource }
            }, { wrapper });
            await waitFor(() => {
                rerender();
                expect(result.current.resources).toStrictEqual(mockDatasources);
            })
            result.current.saveDataSource({ type: mockType, name: 'test-setting', contents: mockLoadBody });
            await waitFor(() => {
                expect(result.current.resources).toStrictEqual(mockUpdatedSettings);
            });
        });
    });

    describe('useDeleteDataSource', () => {
        it('正しくデータソースを削除できることを確認', async () => {
            const { result, rerender } = renderHook(() => {
                const deleteDataSource = useDeleteDataSource(mockType);
                const resources = useQueryDatasource();
                return { resources, deleteDataSource }
            }, { wrapper });
            await waitFor(() => {
                rerender();
                expect(result.current.resources).toStrictEqual(mockDatasources);
            })
            result.current.deleteDataSource('test-setting');
            await waitFor(() => {
                expect(result.current.resources).toStrictEqual(mockRemainingSettings);
            });
        });
    });

    describe('useLoadDataSource', () => {
        it('正しくデータソースを読み込めることを確認', async () => {
            const { result, rerender } = renderHook(() => {
                return useLoadDataSource()(mockType, 'test-setting');
            }, { wrapper });
            await waitFor(() => {
                rerender();
                result.current.then((res) => {
                    expect(res).toEqual(mockLoadBody);
                });
            });
        });
    });
});
;