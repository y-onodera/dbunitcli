# hooks/ 設計規約

## useEffect 内の非同期処理はモジュールレベル関数で書く

`useEffect` 内から呼ぶ非同期 API 関数は**モジュールレベルの `async function`** として定義する。フックが返すクロージャを deps に含めると「setState → 再レンダー → 新クロージャ → effect 再発火」の無限ループが起きる。

イベントハンドラとして使うだけの関数（deps に含めない）はクロージャのままでよい。

## アンマウントガード（AbortController）

非同期処理がアンマウント後に完了しうる場合は `AbortController` で必ずガードする。

| 起点 | パターン |
|---|---|
| `useEffect` 内 | `const controller = new AbortController()` + `return () => controller.abort()`、コールバックで `if (!controller.signal.aborted)` |
| イベントハンドラ | `const abortControllerRef = useRef<AbortController \| null>(null)` + `useEffect` でマウント時に生成・アンマウント時に `controller.abort()`、コールバックで `if (!abortControllerRef.current?.signal.aborted)` |

`useEffect` 外から Promise を開始する場合はクロージャをまたぐため `useRef` が必要。cleanup で ref を `null` にしないこと（`signal.aborted` 判定が無効化されるため）。

