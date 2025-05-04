# リソースファイル管理エンドポイント

このドキュメントでは、各種リソースファイル（データセット設定、Excelスキーマ等）を管理するエンドポイントについて説明します。

## 共通仕様

以下のエンドポイントは全てのリソースタイプで共通です。
パスの`{resource}`には、以下のいずれかが入ります：
- dataset-setting
- xlsx-schema
- template

## エンドポイント一覧

### GET /{resource}/list

リソースファイル一覧を取得します。

- リクエスト
  - メソッド: GET
  - パス: /{resource}/list

- レスポンス
  - Content-Type: application/json
  - 本文: ファイル名リスト（JSON配列）

### POST /{resource}/load

リソースファイルを読み込みます。

- リクエスト
  - メソッド: POST
  - パス: /{resource}/load
  - Content-Type: text/plain
  - 本文: ファイル名

- レスポンス
  - Content-Type: application/json
  - 本文: ファイル内容（JSON）

### POST /{resource}/save

リソースファイルを保存します。

- リクエスト
  - メソッド: POST
  - パス: /{resource}/save
  - Content-Type: application/json
  - 本文: ResourceSaveRequest
    ```json
    {
      "name": "ファイル名",
      "input": {
        // リソース内容
      }
    }
    ```

- レスポンス
  - Content-Type: application/json
  - 本文: ファイル名リスト（JSON配列）

### POST /{resource}/delete

リソースファイルを削除します。

- リクエスト
  - メソッド: POST
  - パス: /{resource}/delete
  - Content-Type: application/json
  - 本文: ファイル名

- レスポンス
  - Content-Type: application/json
  - 本文: ファイル名リスト（JSON配列）