# DBUnit CLI GUI (Tauri)

Tauri v2 のデスクトップ GUI。React 19 + TypeScript 7 + Tailwind CSS 4 のフロントエンドが sidecar (Java) の REST API を使う。ビルドは Bun + Vite、品質管理は Biome v2。

## 構成
- `src/` — フロントエンド。`app/`（画面）・`components/`（再利用 UI）・`context/`・`hooks/`・`model/`・`utils/`。テストは `src/tests/` にソース構造をミラーして置く
- `src-tauri/src/lib.rs` — sidecar（ビルド時に `src-tauri/backend/` へ置く `dbunit-cli-sidecar.exe`）を、`tauri.conf.json` の CLI 引数（`--port` 等）を JVM 引数にして起動し、ウィンドウを閉じたら終了する。`open_directory` コマンドも持つ

## コマンド（`tauri/` で実行）
`npx` は使わず `bunx` を使う（別パッケージを取得してしまう）。依存は SessionStart フックで `bun install --frozen-lockfile` 済み。
- 型チェック `bunx tsc --noEmit` / テスト `bunx vitest run` / Lint `bunx biome check`
- `bunx biome check --write` は変更したファイルだけに使う（未整形の既存ファイルに無関係な差分が出る）
- アプリ起動 `bun tauri dev` / 配布ビルド `bun tauri build`
- 画面のスモークテスト `bash screenshot/run.sh smoke`（web 環境専用。vitest で捕まらない描画中の例外等を検出。大きな変更の後に流す）

## CSS
`App-src.css` → `bunx @tailwindcss/cli` → `App.css`（Vite/PostCSS 統合は使わない）。Tailwind v4 のため `tailwind.config.js` は無く、`@import "tailwindcss"` + `@plugin` 構文で書く。
