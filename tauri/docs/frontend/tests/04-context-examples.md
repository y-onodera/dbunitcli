# Contextテストの実装例

このドキュメントは、Contextレイヤーのテスト実装例を提供します。

## 1. APIコールのモック化

```typescript
const { mockFetchData } = vi.hoisted(() => ({
  mockFetchData: vi.fn((params: FetchParams) => {
    if (params.endpoint.includes('/endpoint')) {
      return Promise.resolve(new Response(JSON.stringify(mockResponse)));
    }
    return Promise.resolve(new Response());
  })
}));

vi.mock('../../utils/fetchUtils', () => ({
  fetchData: mockFetchData
}));
```

## 2. データ操作のテスト例

### 2.1. データロードのテスト

```typescript
describe('useLoadDataのテスト', () => {
  it('正しくデータを読み込めることを確認', async () => {
    const { result } = renderHook(() => useLoadData(), { wrapper });
    
    await waitFor(() => {
      result.current.then((res) => {
        expect(res).toEqual(expectedData);
      });
    });
  });
});
```

### 2.2. データ保存のテスト

```typescript
describe('useSaveDataのテスト', () => {
  it('正しくデータを保存できることを確認', async () => {
    const { result } = renderHook(() => {
      const saveData = useSaveData();
      const resources = useResources();
      return { resources, saveData };
    }, { wrapper });

    result.current.saveData('test-data', newData);
    
    await waitFor(() => {
      expect(result.current.resources).toEqual(expectedResources);
    });
  });
});
```

## 3. 状態更新のテスト例

```typescript
describe('useSetDataのテスト', () => {
  it('状態を更新できることを確認', () => {
    const { result } = renderHook(() => ({
      data: useData(),
      setData: useSetData()
    }), { wrapper });

    act(() => {
      result.current.setData(newData);
    });

    expect(result.current.data).toEqual(newData);
  });
});
```

## 4. エラーケースのテスト例

```typescript
describe('エラーハンドリングのテスト', () => {
  it('APIエラー時の処理を確認', async () => {
    mockFetchData.mockRejectedValueOnce(new Error('API Error'));
    
    const { result } = renderHook(() => useLoadData(), { wrapper });
    
    await waitFor(() => {
      expect(result.current.error).toBeDefined();
    });
  });
});