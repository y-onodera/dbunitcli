# コマンド設定エンドポイント - 基本操作

このドキュメントでは、各種コマンドの設定を管理する基本的なエンドポイントについて説明します。

## 共通仕様

以下のエンドポイントは全てのコマンドタイプで共通です。
パスの`{command}`には、以下のいずれかが入ります：
- compare
- convert
- generate
- parameterize
- run

## エンドポイント一覧

### GET /{command}/add

新規コマンド設定を追加します。

- リクエスト
  - メソッド: GET
  - パス: /{command}/add

- レスポンス
  - Content-Type: application/json
  - 本文: 設定名リスト（JSON配列）

### POST /{command}/copy

既存設定をコピーします。

- リクエスト
  - メソッド: POST
  - パス: /{command}/copy
  - Content-Type: application/json
  - 本文: CommandRequestDto
    ```json
    {
      "name": "コピー元設定名"
    }
    ```

- レスポンス
  - Content-Type: application/json
  - 本文: 設定名リスト（JSON配列）

### POST /{command}/delete

設定を削除します。

- リクエスト
  - メソッド: POST
  - パス: /{command}/delete
  - Content-Type: application/json
  - 本文: CommandRequestDto
    ```json
    {
      "name": "削除する設定名"
    }
    ```

- レスポンス
  - Content-Type: application/json
  - 本文: 設定名リスト（JSON配列）

### POST /{command}/load

設定内容を読み込みます。

- リクエスト
  - メソッド: POST
  - パス: /{command}/load
  - Content-Type: application/json
  - 本文: CommandRequestDto
    ```json
    {
      "name": "設定名"
    }
    ```

- レスポンス
  - Content-Type: application/json
  - 本文: パラメータMap（JSON）