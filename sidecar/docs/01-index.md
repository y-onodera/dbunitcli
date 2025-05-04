# Sidecarプロジェクト ドキュメント構成

## 基本情報

### [プロジェクト概要](./project/01-overview.md)
- プロジェクトの位置付け
- 主要な責務
- 全体構成

### [技術スタック](./project/02-tech-stack.md)
- 使用技術一覧
- システム要件
- 実行環境要件

## アーキテクチャ設計

### [アーキテクチャ概要](./architecture/01-overview.md)
- パッケージ構造
- ワークスペース構成
- コンポーネント間の関係

### [GUIバックエンド設計](./architecture/02-gui-backend.md)
- Workspaceモデル
- GUIインターフェース
- コマンド管理

### [coreプロジェクト連携設計](./architecture/03-core-bridge.md)
- Command実行フロー
- データ変換層
- 結果管理

### [中間層設計](./architecture/04-middleware.md)
- ファイルシステム管理
- オプション管理
- リソース管理

## API仕様

### REST API仕様
- [APIの概要と共通仕様](./api/01-overview.md)
- [ワークスペース管理API](./api/02-endpoints-workspace.md)
- [コマンド設定API - 基本操作](./api/03-endpoints-command-basic.md)
- [コマンド設定API - 実行操作](./api/04-endpoints-command-exec.md)
- [リソースファイル管理API](./api/05-endpoints-resource.md)
- [データソース設定API](./api/06-endpoints-query.md)

### [OpenAPI仕様](./api/openapi.yaml)
- APIの詳細仕様
- スキーマ定義
- セキュリティ定義
