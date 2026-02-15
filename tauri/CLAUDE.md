# CLAUDE.md

このファイルは、Claude Code (claude.ai/code) がこのリポジトリのコードを操作する際のガイダンスを提供します。

## プロジェクト概要

これは **Tauri v2** で構築された **DBUnit CLI GUI** アプリケーションで、DBUnit CLI ツールのデスクトップインターフェースを提供します。アーキテクチャは以下で構成されています：

- **フロントエンド**: React + TypeScript with Tailwind CSS
- **バックエンド**: Tauri (Rust) が Java DBUnit CLI プロセスを生成・管理
- **ビルドシステム**: パッケージ管理に Bun、バンドルに Vite

## 開発コマンド

### フロントエンド開発
```bash
# 依存関係のインストール
bun install

# Tailwind CSS コンパイル + Vite 開発サーバー起動
bun run dev

# Tailwind CSS コンパイル + TypeScript チェック + Vite ビルド + テスト実行
bun run build
```

### Tauri 開発
```bash
# Tauri 開発モード開始（bun run dev + Rust ビルド + アプリ起動）
bun tauri dev

# 配布用 Tauri アプリケーションビルド（bun run build + Rust リリースビルド）
bun tauri build
```

### テスト
```bash
# Vitest でフロントエンドテスト実行
bun vitest run

# ウォッチモードでテスト実行
bun vitest

# 特定のテストファイル実行
bun vitest run src/tests/model/CommandParam.test.ts
```

### コード品質（Biome v2）
- **フォーマット**: VSCode 保存時に自動実行（`.vscode/settings.json` で設定済み）
- **import 整理**: 保存時に自動実行（`source.organizeImports.biome`）
- **lint**: コード修正後に手動実行（`bunx biome lint --write .`）

```bash
# 手動でフォーマット実行（全ファイル）
bunx biome format --write .

# 手動でリント実行
bunx biome lint .

# lint 自動修正（safe fix のみ）
bunx biome lint --write .

# TypeScript 型チェック
bunx tsc --noEmit
```

## アーキテクチャ概要

### フロントエンド構造
- **`src/app/`** - 機能別に整理されたメインアプリケーションコンポーネント：
  - `form/` - コマンド固有のフォーム（Compare, Convert, Generate など）
  - `main/` - コアレイアウトコンポーネント（Layout, Header, Sidebar）
  - `settings/` - 設定ダイアログとエディタ
  - `startup/` - 初期ワークスペース選択
- **`src/components/`** - 再利用可能な UI コンポーネント
- **`src/context/`** - 状態管理用の React Context プロバイダ
- **`src/model/`** - データモデル用の TypeScript インターフェースとクラス
- **`src/tests/`** - ソース構造をミラーしたテストファイル

### バックエンド構造（Tauri）
- **`src-tauri/src/lib.rs`** - メイン Tauri アプリケーションロジック
- **`src-tauri/src/main.rs`** - エントリーポイント
- **`src-tauri/backend/`** - Java DBUnit CLI 実行ファイルと依存関係
- **`src-tauri/tauri.conf.json`** - Tauri 設定

### 主要なデータフロー
1. フロントエンドが Tauri の HTTP API 経由で Java バックエンドにコマンドを送信
2. Java DBUnit CLI プロセスは Rust バックエンドで管理
3. 結果は HTTP レスポンスで返され、React UI に表示
4. 設定は React Context プロバイダで管理

### 状態管理パターン
- **Context プロバイダ**: グローバル状態（Environment, WorkspaceResources, SelectParameter）
- **ローカル状態**: `useState` によるコンポーネント固有の状態
- **モデル**: 複雑なデータ構造用の TypeScript クラス（CommandParam, DatasetSettings など）

## 重要な実装ノート

### Tauri バックエンド
- カスタム JVM 引数で Java プロセス（`dbunit-cli-sidecar.exe`）を生成
- ワークスペースとポート設定の CLI 引数解析を処理
- プロセスライフサイクル管理（アプリ起動時に開始、ウィンドウ閉時に終了）
- ファイルシステム統合用の `open_directory` コマンドを提供

### フロントエンドパターン
- コンポーネントはアトミックデザイン原則に従う（UI コンポーネント → App コンポーネント）
- フォームは共有ベースコンポーネントを持つコマンド固有
- Context プロバイダがコンポーネント間の状態と API 呼び出しを処理
- TypeScript モデルが複雑なデータ構造の型安全性を提供

### テストアプローチ
- jsdom 環境での単体テストに **Vitest**
- React コンポーネントテストに **Testing Library**
- テストファイルはソース構造をミラー（`src/model/File.ts` → `src/tests/model/File.test.ts`）
- Context プロバイダは専用のテストカバレッジを持つ

### ビルド設定
- **CSS**: `@tailwindcss/cli`（v4）で `App-src.css` → `App.css` にプリコンパイル。Vite/PostCSS 統合は使わない
- **Tailwind v4**: CSS ベース設定（`tailwind.config.js` 不要）。`@import "tailwindcss"` + `@plugin` 構文
- **リンター**: Biome v2（フォーマット + lint + import整理）。設定は `biome.json`
- **TypeScript**: パスマッピング付きの厳密モード有効
- **バンドル**: フロントエンド用 Vite + React プラグイン、Rust バックエンド用 Cargo
- **リソース**: Java ランタイムと CLI 実行ファイルをアプリケーションにバンドル

## このコードベースでの作業方法

### 新しいコマンドの追加
1. `src/app/form/` にフォームコンポーネントを作成
2. `src/model/CommandParam.ts` に対応するモデル型を追加
3. 状態管理用に Context プロバイダを更新
4. メイン `Form.tsx` のルーティングにフォームを追加

### バックエンド統合の変更
- Java プロセス引数は `lib.rs` で設定
- HTTP API 呼び出しは fetch ユーティリティで処理
- ファイルシステム操作は Tauri の dialog プラグインを使用

### 新機能のテスト
- モデルとユーティリティの単体テストを追加
- Context プロバイダの統合テストを追加
- コンポーネントインタラクションテストには Testing Library を使用