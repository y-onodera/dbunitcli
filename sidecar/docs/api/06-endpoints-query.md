# Query Datasource エンドポイント

このドキュメントでは、データソース設定に関連するエンドポイントについて説明します。

## エンドポイント一覧

### GET /query-datasource/list

データソースとして使用するクエリーが書かれたテキストファイルの一覧を取得します。

- リクエスト
  - メソッド: GET
  - パス: /query-datasource/list
  - クエリパラメータ
    - type: DataSourceType

- レスポンス
  - Content-Type: application/json
  - 本文: データソース名リスト（JSON配列）

### POST /query-datasource/load

データソース設定を読み込みます。

- リクエスト
  - メソッド: POST
  - パス: /query-datasource/load
  - Content-Type: application/json
  - 本文: データソース情報（名前とタイプ）

- レスポンス
  - Content-Type: application/json
  - 本文: 設定内容（JSON）