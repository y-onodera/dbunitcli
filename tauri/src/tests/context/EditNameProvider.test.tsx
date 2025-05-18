import { act, cleanup, renderHook } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import EditNameProvider, { type EditName, useEditName, useSetEditName } from '../../context/EditNameProvider';

describe('EditNameProviderのテスト', () => {
    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    it('useEditNameで初期状態が空のオブジェクトであることを確認', () => {
        const wrapper = ({ children }: { children: React.ReactNode }) => (
            <EditNameProvider>{children}</EditNameProvider>
        );

        const { result } = renderHook(() => useEditName(), { wrapper });
        expect(result.current).toEqual({});
    });

    it('useSetEditNameで状態を更新できることを確認', () => {
        const wrapper = ({ children }: { children: React.ReactNode }) => (
            <EditNameProvider>{children}</EditNameProvider>
        );

        const mockEditName: EditName = {
            name: 'test-name',
            command: 'convert',
            x: 100,
            y: 200,
            afterEdge: true,
            setMenuList: () => { },
        };

        const { result } = renderHook(
            () => ({
                editName: useEditName(),
                setEditName: useSetEditName(),
            }),
            { wrapper }
        );

        act(() => {
            result.current.setEditName(mockEditName);
        });

        expect(result.current.editName).toEqual(mockEditName);
    });

    it('setMenuListコールバックが正しく動作することを確認', () => {
        const wrapper = ({ children }: { children: React.ReactNode }) => (
            <EditNameProvider>{children}</EditNameProvider>
        );

        const mockSetMenuList = vi.fn();
        const mockEditName: EditName = {
            name: 'test-name',
            command: 'convert',
            x: 100,
            y: 200,
            afterEdge: true,
            setMenuList: mockSetMenuList,
        };

        const { result } = renderHook(
            () => ({
                editName: useEditName(),
                setEditName: useSetEditName(),
            }),
            { wrapper }
        );

        act(() => {
            result.current.setEditName(mockEditName);
        });

        const mockMenuList = ['menu1', 'menu2'];
        result.current.editName.setMenuList(mockMenuList);

        expect(mockSetMenuList).toHaveBeenCalledWith(mockMenuList);
    });

    it('useSetEditNameで部分的な更新ができることを確認', () => {
        const wrapper = ({ children }: { children: React.ReactNode }) => (
            <EditNameProvider>{children}</EditNameProvider>
        );

        const initialEditName: EditName = {
            name: 'test-name',
            command: 'convert',
            x: 100,
            y: 200,
            afterEdge: true,
            setMenuList: () => { },
        };

        const { result } = renderHook(
            () => ({
                editName: useEditName(),
                setEditName: useSetEditName(),
            }),
            { wrapper }
        );

        act(() => {
            result.current.setEditName(initialEditName);
        });

        act(() => {
            result.current.setEditName(prev => ({
                ...prev,
                x: 300,
                y: 400,
            }));
        });

        expect(result.current.editName).toEqual({
            ...initialEditName,
            x: 300,
            y: 400,
        });
    });
});