# DBUnit CLI GUI アプリケーション

このドキュメントは、DBUnit CLI GUIアプリケーションの技術ドキュメントです。

## フロントエンド実装

### アーキテクチャ
- [プロジェクト構造の概要](frontend/architecture/01-overview.md)
- [モデルレイヤー](frontend/architecture/02-model-layer.md)
- [アプリケーションコンポーネント](frontend/architecture/03-app-components.md)
- [コンテキストレイヤー](frontend/architecture/04-context-layer.md)
- [データフロー](frontend/architecture/05-data-flow.md)
- [UIコンポーネント](frontend/architecture/06-ui-components.md)

### コーディング規約
- [共通規約](frontend/standards/01-overview.md)
- [モデルレイヤーの規約](frontend/standards/02-model-standards.md)
- [アプリケーションコンポーネントの規約](frontend/standards/03-app-standards.md)
- [コンテキストレイヤーの規約](frontend/standards/04-context-standards.md)
- [UIコンポーネントの規約](frontend/standards/05-ui-standards.md)

### テスト
- [テスト概要](frontend/tests/01-index.md)
- [テスト基本規約](frontend/tests/02-test-rules.md)
- [Contextレイヤーの規約](frontend/tests/03-context-rules.md)
- [Contextテストの実装例](frontend/tests/04-context-examples.md)

## バックエンド実装

### アーキテクチャ
- [実装概要](backend/01-overview.md)
- [プラグイン構成](backend/02-plugins.md)
- [プロセス管理](backend/03-process.md)
- [コマンドライン引数](backend/04-cli.md)

## 共有設定

### 設定とデータ連携
- [設定の概要と構成](shared/01-configuration.md)
- [コマンドライン引数の連携](shared/02-cli-parameters.md)

## 技術スタック

### フロントエンド
- React + TypeScript
- Tailwind CSS
- Vite

### バックエンド
- Tauri v2
- Rust
- Java (DBUnit CLI)

### 開発ツール
- Vitest
- Bun
- Biome