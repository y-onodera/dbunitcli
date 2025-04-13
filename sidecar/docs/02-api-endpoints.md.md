# APIエンドポイント概要

## 概要

このドキュメントは、DBUnit CLI Sidecarが提供するREST APIのエンドポイントの概要を説明します。
Sidecarは主に以下の役割を担います。

1.  **coreプロジェクトのCLIコマンド実行ブリッジ:** Tauriフロントエンドからのリクエストを受け、coreプロジェクトのコマンドを実行します。
2.  **Tauriフロントエンドのためのワークスペース管理:** 設定ファイルやリソースファイルの管理機能を提供します。

各エンドポイントの詳細な仕様（リクエスト/レスポンス形式、パラメータ、スキーマ）については、以下のOpenAPI仕様ファイルを参照してください。

- [OpenAPI Specification (openapi.yaml)](openapi.yaml)

## APIエンドポイント一覧

### 1. コマンド実行と設定管理 (`/{command}`)

`{command}` は `compare`, `convert`, `generate`, `parameterize`, `run` のいずれか。

-   **コマンド実行:**
    -   `POST /{command}/exec`: 指定された設定でcoreプロジェクトのコマンドを実行します。
-   **設定ファイル管理:**
    -   `GET /{command}/add`: 新規設定ファイルを作成します。
    -   `POST /{command}/copy`: 既存の設定ファイルをコピーします。
    -   `POST /{command}/delete`: 設定ファイルを削除します。
    -   `POST /{command}/rename`: 設定ファイル名を変更します。
    -   `POST /{command}/load`: 設定ファイルの内容を読み込みます。
    -   `GET /{command}/reset`: 設定をデフォルト値にリセットします。
    -   `POST /{command}/refresh`: 設定パラメータをリフレッシュ（検証・整形）します。
    -   `POST /{command}/save`: 設定ファイルにパラメータを保存します。

### 2. ワークスペース管理

Tauriフロントエンドがワークスペースの状態確認や設定を行うためのエンドポイントです。

-   **ワークスペース全体:**
    -   `GET /workspace/resources`: ワークスペースの状態（設定ファイルリスト、ディレクトリパスなど）を取得します。
    -   `POST /workspace/update`: ワークスペースのベースディレクトリ設定などを更新します。
-   **リソースファイル管理 (`/{resource}`):**
    `{resource}` は `metadata` (MetaDataSettings), `xlsx-schema` (XlsxSchema) のいずれか。
    -   `GET /{resource}/list`: リソースファイル名リストを取得します。
    -   `POST /{resource}/load`: リソースファイルの内容を読み込みます。
    -   `POST /{resource}/save`: リソースファイルの内容を保存します。
    -   `POST /{resource}/delete`: リソースファイルを削除します。
-   **クエリデータソース管理 (`/query-datasource`):**
    -   `GET /query-datasource/list`: データソース名リストを取得します。
    -   `POST /query-datasource/load`: データソース設定を読み込みます。
    -   `POST /query-datasource/save`: データソース設定を保存します。
    -   `POST /query-datasource/delete`: データソース設定を削除します。

## ファイルシステムとの連携

- 各コントローラーは、`../.roo/rules/01-rules.md` の「ワークスペース構成」で定義されたディレクトリ構造に従ってファイルを操作します。
- パスはすべてワークスペースからの相対パスで扱われます。
- `dataset/` と `result/` のパスは、Javaシステムプロパティ `datasetBase` と `resultBase` で変更可能です。
