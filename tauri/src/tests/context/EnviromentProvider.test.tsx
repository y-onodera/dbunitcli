import { renderHook, waitFor } from '@testing-library/react';
import { act } from '@testing-library/react';
import { render, screen } from '@testing-library/react';
import type { ReactNode } from 'react';
import { describe, expect, it, vi } from 'vitest';
import EnviromentProvider, { useEnviroment } from '../../context/EnviromentProvider';

// Tauri Plugin CLIのモック
const mockArgs: {
    args: {
        port?: { value: string },
        workspace?: { value: string },
        "dataset.base"?: { value: string },
        "result.base"?: { value: string },
    }
} = {
    args: {
        port: { value: "8081" },
        workspace: { value: "test/workspace" },
        "dataset.base": { value: "test/dataset" },
        "result.base": { value: "test/result" },
    }
};
const mockGetMatches = vi.fn(() => {
    return Promise.resolve(mockArgs);
});
vi.mock("@tauri-apps/plugin-cli", () => ({
    getMatches: () => mockGetMatches(),
}));

// テストコンポーネント
const TestComponent = () => {
    return <div data-testid="test-component">'Loaded Content'</div>;
};

// カスタムラッパーコンポーネント
const wrapper = ({ children }: { children: ReactNode }) => (
    <EnviromentProvider>
        {children}
    </EnviromentProvider>
);

describe('EnviromentProviderのテスト', () => {
    it('環境設定が正しく読み込まれることを確認', async () => {
        const { result } = renderHook(() => useEnviroment(), { wrapper });

        waitFor(() => {
            expect(result.current).toEqual({
                apiUrl: `http://localhost:${mockArgs.args.port}/dbunit-cli/`,
                workspace: `${mockArgs.args.workspace}`,
                dataset_base: `${mockArgs.args['dataset.base']}`,
                result_base: `${mockArgs.args['result.base']}`,
                loaded: true
            });
        });
        expect(mockGetMatches).toHaveBeenCalled();
    });

    it('環境設定で指定がない場合にデフォルト値が正しく設定されることを確認', async () => {
        beforeEach(() => {
            mockArgs.args = {};
        });
        const { result } = renderHook(() => useEnviroment(), { wrapper });

        waitFor(() => {
            expect(result.current).toEqual({
                apiUrl: 'http://localhost:8080/dbunit-cli/',
                workspace: '.',
                dataset_base: '.',
                result_base: '.',
                loaded: true
            });
        });
    });

    it('ロード完了後に子コンポーネントが表示されることを確認', async () => {
        render(<TestComponent />, { wrapper });

        expect(screen.getByText('loading')).toHaveTextContent('loading');

        // 非同期処理の完了を待機
        await act(async () => {
            await new Promise(resolve => setTimeout(resolve, 100));
        });
        expect(screen.getByTestId('test-component')).toHaveTextContent('Loaded Content');

        expect(mockGetMatches).toHaveBeenCalled();
    });
});