import { render, renderHook, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { type Enviroment, enviromentContext } from '../../context/EnviromentProvider';
import WorkspaceResourcesProvider, {
    useAddParameter,
    useCopyParameter,
    useDeleteParameter,
    useParameterList,
    useRenameParameter,
    useResourcesSettings,
    useWorkspaceContext,
    useWorkspaceUpdate,
} from '../../context/WorkspaceResourcesProvider';
import type { WorkspaceResources } from '../../model/WorkspaceResources';
import { ParameterList } from '../../model/WorkspaceResources';
import type { FetchParams } from '../../utils/fetchUtils';
import { enviromentFixture, workspaceResourcesFixture } from '../setup';

// モックデータ
const mockWorkspaceResources: WorkspaceResources = { ...workspaceResourcesFixture };
const mockEnviroment: Enviroment = { ...enviromentFixture };
function MockProvider({ children }: { children: React.ReactNode }) {
    return <enviromentContext.Provider value={mockEnviroment}><WorkspaceResourcesProvider>{children}</WorkspaceResourcesProvider></enviromentContext.Provider>;
}
const wrapper = ({ children }: { children: React.ReactNode }) => (
    <MockProvider>{children}</MockProvider>
);
const { mockFetchData } = vi.hoisted(() => {
    return {
        mockFetchData: vi.fn((params: FetchParams) => {
            if (params.endpoint === 'http://localhost:8080/workspace/update') {
                const body = JSON.parse(params.options.body as string);
                return Promise.resolve({
                    json: () => Promise.resolve({
                        context: {
                            ...mockWorkspaceResources.context
                            , workspace: body.workspace
                            , datasetBase: body.datasetBase
                            , resultBase: body.resultBase
                        },
                        parameterList: mockWorkspaceResources.parameterList,
                        resources: mockWorkspaceResources.resources,
                    })
                } as Response)
            }
            if (params.endpoint === 'http://localhost:8080/convert/add') {
                return Promise.resolve({
                    json: () => Promise.resolve(['convert1', 'convert2', 'add'])
                } as Response)
            }
            if (params.endpoint === 'http://localhost:8080/convert/delete') {
                return Promise.resolve({
                    json: () => Promise.resolve(['convert1'])
                } as Response)
            }
            if (params.endpoint === 'http://localhost:8080/convert/copy') {
                return Promise.resolve({
                    json: () => Promise.resolve(['convert1', 'convert2', 'copy'])
                } as Response)
            }
            if (params.endpoint === 'http://localhost:8080/convert/rename') {
                return Promise.resolve({
                    json: () => Promise.resolve(['newName', 'convert2'])
                } as Response)
            }
            return Promise.resolve({
                json: () => Promise.resolve(mockWorkspaceResources)
            } as Response)
        })
    };
});
vi.mock('../../utils/fetchUtils', () => ({
    fetchData: mockFetchData,
}));
beforeEach(() => {
    mockEnviroment.loaded = true;
});

describe('WorkspaceResourcesProviderのテスト', () => {

    describe('Workspaceのテスト', () => {
        it('enviromentがload済みならcontextがセットされる', async () => {
            const { result } = renderHook(() => ({
                context: useWorkspaceContext(),
                parameterList: useParameterList(),
                resourcesSettings: useResourcesSettings(),
            }), { wrapper });

            await waitFor(() => {
                expect(result.current.context).toStrictEqual(mockWorkspaceResources.context);
                expect(result.current.parameterList).toStrictEqual(ParameterList.from(mockWorkspaceResources.parameterList));
                expect(result.current.resourcesSettings).toStrictEqual(mockWorkspaceResources.resources);
            });
        });
        it('enviromentがloadが終わっていない場合、loading表示される', async () => {
            beforeEach(() => {
                mockEnviroment.loaded = false;
            });
            render(<div>test</div>, { wrapper });
            await waitFor(() => {
                expect(screen.getByText('Loading...')).toBeInTheDocument();
            });
        });
        it('useWorkspaceUpdateが正常に動作することを確認', async () => {
            const { result } = renderHook(() => {
                const workspaceUpdate = useWorkspaceUpdate();
                const context = useWorkspaceContext();
                return { context, workspaceUpdate };
            }, { wrapper });
            await waitFor(() => {
                expect(result.current.context.workspace).toBe('test-workspace');
                expect(result.current.context.datasetBase).toBe('dataset');
                expect(result.current.context.resultBase).toBe('result');
            });
            result.current.workspaceUpdate('new-workspace', 'new-dataset', 'new-result');
            await waitFor(() => {
                expect(result.current.context.workspace).toBe('new-workspace');
                expect(result.current.context.datasetBase).toBe('new-dataset');
                expect(result.current.context.resultBase).toBe('new-result');
            });
        });
    });

    describe('パラメータ操作のテスト', () => {
        it('useAddParameterが正常に動作することを確認', async () => {
            const { result } = renderHook(() => {
                const addConvert = useAddParameter('convert');
                const parameterList = useParameterList();
                return { parameterList, addConvert };
            }, { wrapper });
            await waitFor(() => {
                expect(result.current.parameterList.convert).toEqual(['convert1', 'convert2']);
            });
            result.current.addConvert();
            await waitFor(() => {
                expect(result.current.parameterList.convert).toEqual(['convert1', 'convert2', 'add']);
            });
        });

        it('useDeleteParameterが正常に動作することを確認', async () => {
            const { result } = renderHook(() => {
                const deleteConvert2 = useDeleteParameter('convert', 'convert2');
                const parameterList = useParameterList();
                return { parameterList, deleteConvert2 };
            }, { wrapper });
            await waitFor(() => {
                expect(result.current.parameterList.convert).toEqual(['convert1', 'convert2']);
            });
            result.current.deleteConvert2();
            await waitFor(() => {
                expect(result.current.parameterList.convert).toEqual(['convert1']);
            });
        });

        it('useCopyParameterが正常に動作することを確認', async () => {
            const { result } = renderHook(() => {
                const copyConvert1 = useCopyParameter('convert', 'convert1');
                const parameterList = useParameterList();
                return { parameterList, copyConvert1 };
            }, { wrapper });
            await waitFor(() => {
                expect(result.current.parameterList.convert).toEqual(['convert1', 'convert2']);
            });
            result.current.copyConvert1();
            await waitFor(() => {
                expect(result.current.parameterList.convert).toEqual(['convert1', 'convert2', 'copy']);
            });
        });

        it('useRenameParameterが正常に動作することを確認', async () => {
            const { result } = renderHook(() => {
                const renameConvert1 = useRenameParameter('convert', 'convert1');
                const parameterList = useParameterList();
                return { parameterList, renameConvert1 };
            }, { wrapper });
            await waitFor(() => {
                expect(result.current.parameterList.convert).toEqual(['convert1', 'convert2']);
            });
            result.current.renameConvert1("newName");
            await waitFor(() => {
                expect(result.current.parameterList.convert).toEqual(['newName', 'convert2']);
            });
        });
    });
});