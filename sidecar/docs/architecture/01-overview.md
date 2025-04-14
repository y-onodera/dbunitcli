# Sidecarプロジェクト アーキテクチャ詳細

## パッケージ構造

```
yo.dbunitcli.sidecar
├── controller        # REST APIエンドポイントを提供するコントローラー
├── domain           # ビジネスロジックとドメインモデル
│   └── project     # プロジェクト関連のドメインモデル
└── dto             # APIリクエスト/レスポンス用のデータ転送オブジェクト
```

## アーキテクチャ構成

このプロジェクトは、以下の3つの主要なレイヤーで構成されます：

1. [GUIインターフェース層](./02-gui-backend.md)
   - RESTful APIの提供
   - GUIからのリクエスト処理
   - 実行結果の最適化

2. [コア連携層](./03-core-bridge.md)
   - coreプロジェクトの機能呼び出し
   - データ変換処理
   - 実行状態管理

3. [ファイルシステム層](./04-middleware.md)
   - リソース管理
   - 設定ファイル管理
   - 実行結果管理

## 関連ドキュメント
- [プロジェクト概要](../project/01-overview.md)
- [技術スタック](../project/02-tech-stack.md)
- [API仕様](../api/01-endpoints.md)