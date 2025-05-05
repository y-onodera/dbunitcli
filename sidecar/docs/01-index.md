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
- リソース管理機能

### [GUIバックエンド設計](./architecture/02-gui-backend.md)
- RESTful APIの提供
- GUIとの連携
- コマンド管理
- リソース操作インターフェース

### [中間層設計](./architecture/03-middleware.md)
- ファイルシステム管理
- オプション管理
- ファイル操作の抽象化

### [コアプロジェクト連携設計](./architecture/04-core-bridge.md)
- Command実行フロー
- データ変換層
- 結果管理

### [リソースコントローラー](./architecture/05-resource-controllers.md)
- リソースコントローラーの基本構造
- 各種コントローラーの責務
- バリデーション機能

## API仕様

### REST API仕様
- [APIの概要と共通仕様](./api/01-overview.md)
- [ワークスペース管理API](./api/02-endpoints-workspace.md)
- [コマンド設定API](./api/03-endpoints-command-basic.md)
- [コマンド実行API](./api/04-endpoints-command-exec.md)
- [リソースファイル管理API](./api/05-endpoints-resource.md)
- [データソース設定API](./api/06-endpoints-query.md)

### OpenAPI仕様
- [API定義](./api/openapi.yaml)
- APIの詳細仕様
- スキーマ定義
- セキュリティ定義
