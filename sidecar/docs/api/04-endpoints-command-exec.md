# コマンド設定エンドポイント - 実行操作

このドキュメントでは、各種コマンドの実行に関連するエンドポイントについて説明します。

## 共通仕様

以下のエンドポイントは全てのコマンドタイプで共通です。
パスの`{command}`には、以下のいずれかが入ります：
- compare
- convert
- generate
- parameterize
- run

## エンドポイント一覧

### GET /{command}/reset

デフォルト設定を取得します。

- リクエスト
  - メソッド: GET
  - パス: /{command}/reset

- レスポンス
  - Content-Type: application/json
  - 本文: デフォルトパラメータMap（JSON）

### POST /{command}/refresh

パラメータ更新とバリデーションを行います。

- リクエスト
  - メソッド: POST
  - パス: /{command}/refresh
  - Content-Type: application/json
  - 本文: パラメータMap（JSON）

- レスポンス
  - Content-Type: application/json
  - 本文: 検証済みパラメータMap（JSON）

### POST /{command}/save

設定を保存します。

- リクエスト
  - メソッド: POST
  - パス: /{command}/save
  - Content-Type: application/json
  - 本文: CommandRequestDto
    ```json
    {
      "name": "設定名",
      "input": {
        // パラメータMap
      }
    }
    ```

- レスポンス
  - Content-Type: text/plain
  - 本文: "success"

### POST /{command}/exec

コマンドを実行します。

- リクエスト
  - メソッド: POST
  - パス: /{command}/exec
  - Content-Type: application/json
  - 本文: CommandRequestDto
    ```json
    {
      "name": "設定名",
      "input": {
        // パラメータMap
      }
    }
    ```

- レスポンス
  - Content-Type: text/plain
  - 本文: 結果ディレクトリパス（実行結果の出力先）