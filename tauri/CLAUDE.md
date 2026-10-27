# DBUnit CLI GUI (Tauri)

Tauri v2 ベースのデスクトップ GUI。React フロントエンドが sidecar (Java) をバックエンドとして利用する。

## 技術スタック
- **フロントエンド**: React 19 + TypeScript 7 + Tailwind CSS 4.x
- **バックエンド**: Tauri v2 (Rust) — Java sidecar プロセスを起動・管理
- **ビルド**: Bun + Vite、品質管理: Biome v2

## フロントエンド構造

```
src/
├── app/
│   ├── form/       — コマンド別フォーム（Compare, Convert, Generate, Run, Parameterize）
│   ├── main/       — コアレイアウト（Layout, Header, Sidebar, Form）
│   ├── sidebar/    — NamedParameters, NameEditMenu
│   ├── footer/     — Footer, ResultDialog
│   └── startup/    — StartupForm（初期ワークスペース選択）
├── components/     — 再利用可能 UI（dialog/, element/）
├── context/        — React Context プロバイダ（Provider コンポーネントを持つもののみ）
├── hooks/          — カスタムフック（APIコール・状態操作。JSXなし → .ts）
├── model/          — TypeScript 型定義（SelectParameter, CommandOption 等）
├── utils/          — fetchUtils.ts（HTTP API 呼び出し）
└── tests/          — ソース構造をミラーしたテスト
```

## バックエンド構造（Tauri）
- `src-tauri/src/lib.rs` — メイン Tauri ロジック（JVM 起動・プロセス管理）
- `src-tauri/tauri.conf.json` — Tauri 設定（CLI 引数、バンドルリソース）
- `src-tauri/backend/` — Java sidecar 実行ファイルと依存 DLL

## コマンド（`tauri/` で実行）
`npx` は使わず `bunx` を使う（別パッケージを取得してしまう）。依存は SessionStart フックで `bun install --frozen-lockfile` 済み。
- 型チェック `bunx tsc --noEmit` / テスト `bunx vitest run` / Lint `bunx biome check`
- `bunx biome check --write` は変更したファイルだけに使う（未整形の既存ファイルに無関係な差分が出る）
- アプリ起動 `bun tauri dev` / 配布ビルド `bun tauri build`

## 重要な実装ノート

### Tauri バックエンド
- `dbunit-cli-sidecar.exe` をカスタム JVM 引数で起動
- CLI 引数（`--port`, `--workspace`, `--dataset.base`, `--result.base`）を解析
- アプリ起動時に sidecar 開始、ウィンドウ閉時に終了
- `open_directory` Tauri コマンドでファイルシステム統合

### CSS ビルド
- `App-src.css` → `bunx @tailwindcss/cli` → `App.css`（Vite/PostCSS 統合は使わない）
- Tailwind v4: `tailwind.config.js` 不要、`@import "tailwindcss"` + `@plugin` 構文
