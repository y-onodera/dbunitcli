# DBUnit CLI Sidecar API 概要

このドキュメントでは、DBUnit CLI Sidecarが提供するREST APIの概要について説明します。

## 1. API概要

Sidecar APIは、以下の主要な機能を提供します：

- Tauriフロントエンドとcoreプロジェクト間の橋渡し
- ワークスペース管理（設定ファイル、リソースファイルの操作）
- コマンド実行と結果の管理
- データソース設定の管理

## 2. APIの構成

APIは以下の4つの機能グループに分類されます：

1. **Workspace**
   - ワークスペース全体の管理
   - ディレクトリ構成の設定
   - リソース一覧の取得

2. **Command Settings**
   - 各コマンド（compare, convert, generate, parameterize, run）の設定管理
   - 設定ファイルのCRUD操作
   - コマンド実行制御

3. **Resource Files**
   - データセット設定
   - Excelスキーマ
   - テンプレート
   - その他リソースファイルの管理

4. **Query Datasource**
   - クエリーの管理

## エラー応答

APIからエラーが返される場合、以下の形式でレスポンスが返されます：

- ステータスコード: 400 Bad Request
- Content-Type: application/json
- レスポンス本文: JsonErrorオブジェクト
  ```json
  {
    "message": "Execution failed. cause: [エラーメッセージ]"
  }