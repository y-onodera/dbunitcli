# DBUnit CLI GUI (Tauri)

Tauri v2 ベースのデスクトップ GUI。React フロントエンドが sidecar (Java) をバックエンドとして利用する。

## 技術スタック
- **フロントエンド**: React 19 + TypeScript 5.9 + Tailwind CSS 4.x
- **バックエンド**: Tauri v2 (Rust) — Java sidecar プロセスを起動・管理
- **ビルド**: Bun + Vite、品質管理: Biome v2

## フロントエンド構造

```
src/
├── app/
│   ├── form/       — コマンド別フォーム（Compare, Convert, Generate, Run, Parameterize）
│   ├── main/       — コアレイアウト（Layout, Header, Sidebar, Form）
│   ├── settings/   — 設定ダイアログ（Dataset, JDBC, XlsxSchema, SqlEditor）
│   ├── sidebar/    — NamedParameters, NameEditMenu
│   ├── footer/     — Footer, ResultDialog
│   └── startup/    — StartupForm（初期ワークスペース選択）
├── components/     — 再利用可能 UI（dialog/, element/）
├── context/        — React Context プロバイダ（6個）
├── model/          — TypeScript 型定義（CommandParam, DatasetSettings 等）
├── utils/          — fetchUtils.ts（HTTP API 呼び出し）
└── tests/          — ソース構造をミラーしたテスト
```

## バックエンド構造（Tauri）
- `src-tauri/src/lib.rs` — メイン Tauri ロジック（JVM 起動・プロセス管理）
- `src-tauri/tauri.conf.json` — Tauri 設定（CLI 引数、バンドルリソース）
- `src-tauri/backend/` — Java sidecar 実行ファイルと依存 DLL

## 重要な実装ノート

### Tauri バックエンド
- `dbunit-cli-sidecar.exe` をカスタム JVM 引数で起動
- CLI 引数（`--port`, `--workspace`, `--dataset.base`, `--result.base`）を解析
- アプリ起動時に sidecar 開始、ウィンドウ閉時に終了
- `open_directory` Tauri コマンドでファイルシステム統合

### CSS ビルド
- `App-src.css` → `bunx @tailwindcss/cli` → `App.css`（Vite/PostCSS 統合は使わない）
- Tailwind v4: `tailwind.config.js` 不要、`@import "tailwindcss"` + `@plugin` 構文
