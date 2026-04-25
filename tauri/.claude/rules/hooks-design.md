# hooks/ 設計規約

## useEffect 内で使う非同期処理はモジュールレベル関数で書く

`useEffect` 内から呼ぶ非同期 API 関数は、**フックではなくモジュールレベルの `async function`** として定義する。

### なぜか

フックが返すクロージャは毎レンダーで新しい参照になる。
`useEffect` の依存配列に含めると、effect 内の `setState` → 再レンダー → 新しいクロージャ → effect 再発火 の無限ループが起きる。

モジュールレベル関数は参照が安定しているため依存配列に入れる必要がなく、ループが起きない。

### OK: モジュールレベル関数 + apiUrl を deps に含める

```typescript
// hooks/useXxx.ts

// モジュールレベル関数 — 参照が安定
async function fetchXxx(apiUrl: string, param: Param): Promise<Result> {
    ...
}

export const useXxx = (param: Param) => {
    const { apiUrl } = useEnviroment();
    const [data, setData] = useState<Result | null>(null);

    useEffect(() => {
        let isMounted = true;
        fetchXxx(apiUrl, param).then((result) => {
            if (isMounted) { setData(result); }
        });
        return () => { isMounted = false; };
    }, [apiUrl, param]);  // fetchXxx は deps 不要

    return data;
};
```

### NG: フックが返すクロージャを deps に含める

```typescript
// 毎レンダーで新しい関数参照 → 無限ループ
const loadData = useLoadDataApi();  // フックが返すクロージャ
useEffect(() => {
    setLoading(true);               // 再レンダー → 新クロージャ → effect 再発火
    loadData(param).then(...);
}, [loadData, param]);              // NG: loadData が deps にある
```

### イベントハンドラ用フックはそのままでよい

ボタンクリックなど **`useEffect` 外から呼ぶだけ** の関数はフックが返すクロージャでも問題ない。
deps に含めないので無限ループは起きない。

```typescript
// イベントハンドラとして使うだけなら OK
const save = useSaveXxx();
<button onClick={() => save(input)} />
```

## isMounted ガード

非同期処理が完了する前にコンポーネントがアンマウントされる可能性がある場合は必ず `isMounted` ガードを付ける。

```typescript
useEffect(() => {
    let isMounted = true;
    fetchXxx(apiUrl, param).then((result) => {
        if (isMounted) { setData(result); }
    });
    return () => { isMounted = false; };
}, [apiUrl, param]);
```
