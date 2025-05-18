import { renderHook, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { describe, expect, it, vi } from 'vitest';
import { type Enviroment, enviromentContext } from '../../context/EnviromentProvider';
import SelectParameterProvider, {
    useSelectParameter,
    useSetSelectParameter,
    useLoadSelectParameter,
    useRefreshSelectParameter,
    useSaveParameter,
    useExecParameter
} from '../../context/SelectParameterProvider';
import { type CommandParams, type ConvertParams, type GenerateParams, SelectParameter } from '../../model/CommandParam';
import type { FetchParams } from '../../utils/fetchUtils';
import { enviromentFixture } from '../setup';

// モックデータ
const createCommandParams = (): CommandParams => ({
    name: 'test-param',
    prefix: '',
    elements: []
});
const mockConvertParams: ConvertParams = {
    srcData: {
        name: 'test-param',
        prefix: '',
        elements: [],
        srcType: () => 'table',
        srcElements: () => createCommandParams(),
        srcTypeSettings: () => createCommandParams(),
        settingElements: () => createCommandParams()
    },
    convertResult: {
        name: 'test-param',
        prefix: '',
        elements: [],
        jdbc: createCommandParams()
    }
};
const mockRefreshConvertParams: ConvertParams = {
    srcData: { ...mockConvertParams.srcData, name: 'refresh-test-param' },
    convertResult: { ...mockConvertParams.convertResult, name: 'refresh-test-param' }
}
const mockGenerateParams: GenerateParams = {
    elements: [],
    srcData: {
        name: 'test-param',
        prefix: '',
        elements: [],
        srcType: () => 'csv',
        srcElements: () => createCommandParams(),
        srcTypeSettings: () => createCommandParams(),
        settingElements: () => createCommandParams()
    },
    templateOption: {
        name: 'test-param',
        prefix: '',
        elements: [],
    },
}
const mockRefreshGenerateParams: GenerateParams = {
    elements: [],
    srcData: { ...mockGenerateParams.srcData, name: 'refresh-test-param' },
    templateOption: { ...mockGenerateParams.templateOption, name: 'refresh-test-param' }
};
const { mockFetchData } = vi.hoisted(() => {
    return {
        mockFetchData: vi.fn((params: FetchParams) => {
            if (params.endpoint.includes('/generate/load')) {
                return Promise.resolve(new Response(JSON.stringify(mockGenerateParams)));
            }
            if (params.endpoint.includes('/generate/refresh')) {
                return Promise.resolve(new Response(JSON.stringify(mockRefreshGenerateParams)));
            }
            if (params.endpoint.includes('/generate/save')) {
                return Promise.resolve(new Response('success'));
            }
            if (params.endpoint.includes('/generate/exec')) {
                return Promise.resolve(new Response('success'));
            }
            if (params.endpoint.includes('/convert/load')) {
                return Promise.resolve(new Response(JSON.stringify(mockConvertParams)));
            }
            if (params.endpoint.includes('/convert/refresh')) {
                return Promise.resolve(new Response(JSON.stringify(mockRefreshConvertParams)));
            }
            if (params.endpoint.includes('/convert/save')) {
                return Promise.resolve(new Response('success'));
            }
            if (params.endpoint.includes('/convert/exec')) {
                return Promise.resolve(new Response('success'));
            }
            return Promise.resolve(new Response());
        })
    };
});
vi.mock('../../utils/fetchUtils', () => ({
    fetchData: mockFetchData,
    handleFetchError: vi.fn()
}));

const mockEnviroment: Enviroment = { ...enviromentFixture };

// カスタムラッパーコンポーネント
const wrapper = ({ children }: { children: ReactNode }) => (
    <enviromentContext.Provider value={mockEnviroment}>
        <SelectParameterProvider>
            {children}
        </SelectParameterProvider>
    </enviromentContext.Provider>
);

describe('SelectParameterProviderのテスト', () => {
    describe('useSelectParameter', () => {
        it('初期状態が正しいことを確認', () => {
            const { result } = renderHook(() => useSelectParameter(), { wrapper });
            waitFor(() => {
                expect(result.current).toEqual({} as SelectParameter);
            });
        });
    });
    describe('useSetSelectParameter', () => {
        it('パラメータを設定できることを確認', async () => {
            const { result } = renderHook(
                () => ({
                    parameter: useSelectParameter(),
                    setParameter: useSetSelectParameter()
                }),
                { wrapper }
            );
            await waitFor(() => {
                expect(result.current.parameter).toEqual({} as SelectParameter);
            });

            result.current.setParameter(mockConvertParams, 'convert', 'test-param');
            await waitFor(() => {
                expect(result.current.parameter.convert).toEqual(mockConvertParams);
                expect(result.current.parameter.name).toBe('test-param');
                expect(result.current.parameter.command).toBe('convert');
            });
        });
    });

    describe('useLoadSelectParameter', () => {
        it.each([
            { command: 'convert', response: mockConvertParams },
            { command: 'generate', response: mockGenerateParams }
        ])('指定したコマンドのパラメータを読み込めることを確認', async ({ command, response }) => {
            const { result } = renderHook(
                () => ({
                    parameter: useSelectParameter(),
                    loadParameter: useLoadSelectParameter()
                }),
                { wrapper }
            );
            await waitFor(() => {
                expect(result.current.parameter).toEqual({} as SelectParameter);
            });

            result.current.loadParameter(command, 'test-param');
            await waitFor(() => {
                expect(result.current.parameter.name).toBe('test-param');
                expect(result.current.parameter.command).toBe(command);
                expect(result.current.parameter).toEqual(new SelectParameter(response, command, "test-param"));
            });
        });
    });

    describe('useRefreshSelectParameter', () => {
        it.each([
            { command: 'convert', loadResponse: mockConvertParams, refreshResponse: mockRefreshConvertParams },
            { command: 'generate', loadResponse: mockGenerateParams, refreshResponse: mockRefreshGenerateParams }
        ])('パラメータを更新できることを確認：convert', async ({ command, loadResponse, refreshResponse }) => {
            const { result } = renderHook(
                () => ({
                    parameter: useSelectParameter(),
                    loadParameter: useLoadSelectParameter(),
                    refreshParameter: useRefreshSelectParameter(command)
                }),
                { wrapper }
            );
            await waitFor(() => {
                expect(result.current.parameter).toEqual({} as SelectParameter);
            });
            result.current.loadParameter(command, 'test-param');
            await waitFor(() => {
                expect(result.current.parameter).toEqual(new SelectParameter(loadResponse, command, "test-param"));
            });
            const mockData = { test: 'value' };
            result.current.refreshParameter(mockData);
            await waitFor(() => {
                expect(result.current.parameter.name).toBe('test-param');
                expect(result.current.parameter.command).toBe(command);
                expect(result.current.parameter).toEqual(new SelectParameter(refreshResponse, command, 'test-param'));
            });
        });
    });

    describe('useSaveParameter', () => {
        it.each([
            { command: 'convert', params: mockConvertParams },
            { command: 'generate', params: mockGenerateParams }
        ])('パラメータを保存できることを確認', async ({ command, params }) => {
            const mockHandleResult = vi.fn();
            const { result } = renderHook(
                () => ({
                    parameter: useSelectParameter(),
                    setParameter: useSetSelectParameter(),
                    saveParameter: useSaveParameter()
                }),
                { wrapper }
            );
            await waitFor(() => {
                expect(result.current.parameter).toEqual({} as SelectParameter);
            });

            result.current.setParameter(params, command, 'test-param');
            const mockInput = { test: 'value' };
            await result.current.saveParameter(mockInput, mockHandleResult);

            await waitFor(() => {
                expect(mockHandleResult).toHaveBeenCalledWith({
                    command: '',
                    resultMessage: 'Save Success',
                    resultDir: ''
                });
            });
        });

        it.each([
            { command: 'convert', params: mockConvertParams },
            { command: 'generate', params: mockGenerateParams }
        ])('エラー時の処理を確認', async ({ command, params }) => {
            const mockError = new Error('Save Failed');
            mockFetchData.mockRejectedValueOnce(mockError);

            const mockHandleResult = vi.fn();
            const { result } = renderHook(
                () => ({
                    parameter: useSelectParameter(),
                    setParameter: useSetSelectParameter(),
                    saveParameter: useSaveParameter()
                }),
                { wrapper }
            );

            result.current.setParameter(params, command, 'test-param');
            const mockInput = { test: 'value' };
            await result.current.saveParameter(mockInput, mockHandleResult);

            await waitFor(() => {
                expect(mockHandleResult).toHaveBeenCalledWith({
                    command: '',
                    resultMessage: mockError.message,
                    resultDir: ''
                });
            });
        });
    });

    describe('useExecParameter', () => {
        it.each([
            { command: 'convert', params: mockConvertParams },
            { command: 'generate', params: mockGenerateParams }
        ])('パラメータを実行できることを確認', async ({ command, params }) => {
            const mockHandleResult = vi.fn();
            const { result } = renderHook(
                () => ({
                    parameter: useSelectParameter(),
                    setParameter: useSetSelectParameter(),
                    execParameter: useExecParameter()
                }),
                { wrapper }
            );
            await waitFor(() => {
                expect(result.current.parameter).toEqual({} as SelectParameter);
            });

            result.current.setParameter(params, command, 'test-param');
            const mockInput = { test: 'value' };
            await result.current.execParameter(mockInput, mockHandleResult);

            await waitFor(() => {
                expect(mockHandleResult).toHaveBeenCalledWith({
                    command: '',
                    resultMessage: 'Execution Success',
                    resultDir: ''
                });
            });
        });

        it.each([
            { command: 'convert', params: mockConvertParams },
            { command: 'generate', params: mockGenerateParams }
        ])('エラー時の処理を確認', async ({ command, params }) => {
            const mockError = new Error('Execution Failed');
            mockFetchData.mockRejectedValueOnce(mockError);

            const mockHandleResult = vi.fn();
            const { result } = renderHook(
                () => ({
                    parameter: useSelectParameter(),
                    setParameter: useSetSelectParameter(),
                    execParameter: useExecParameter()
                }),
                { wrapper }
            );

            result.current.setParameter(params, command, 'test-param');
            const mockInput = { test: 'value' };
            await result.current.execParameter(mockInput, mockHandleResult);

            await waitFor(() => {
                expect(mockHandleResult).toHaveBeenCalledWith({
                    command: '',
                    resultMessage: mockError.message,
                    resultDir: ''
                });
            });
        });
    });
});