# hooks/ 設計規約

## useEffect 内の非同期処理はモジュールレベル関数で書く

`useEffect` 内から呼ぶ非同期 API 関数は**モジュールレベルの `async function`** として定義する。フックが返すクロージャを deps に含めると「setState → 再レンダー → 新クロージャ → effect 再発火」の無限ループが起きる。

イベントハンドラとして使うだけの関数（deps に含めない）はクロージャのままでよい。

## isMounted ガード

非同期処理がアンマウント後に完了しうる場合は必ず付ける。

| 起点 | パターン |
|---|---|
| `useEffect` 内 | `let isMounted = true` ローカル変数 + `return () => { isMounted = false }` |
| イベントハンドラ | `const isMountedRef = useRef(true)` + `useEffect(() => () => { isMountedRef.current = false }, [])` |

`useEffect` 外から Promise を開始する場合はクロージャをまたぐため `useRef` が必要。

