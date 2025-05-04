# Workspace エンドポイント

このドキュメントでは、ワークスペース管理に関連するエンドポイントについて説明します。

## エンドポイント一覧

### GET /workspace/resources

ワークスペースの状態（設定ファイルリスト、ディレクトリパスなど）を取得します。

- リクエスト
  - メソッド: GET
  - パス: /workspace/resources

- レスポンス
  - Content-Type: application/json
  - 本文: WorkspaceDto（JSON）
    ```json
    {
      "parameterList": {
        // コマンドパラメータ情報
      },
      "resources": {
        // リソースファイル情報
      },
      "context": {
        "workspace": "ワークスペースディレクトリ",
        "datasetBase": "データセットベースディレクトリ",
        "resultBase": "結果出力ディレクトリ",
        "settingBase": "設定ファイルディレクトリ",
        "templateBase": "テンプレートディレクトリ",
        "jdbcBase": "JDBC設定ディレクトリ",
        "xlsxSchemaBase": "Excelスキーマディレクトリ"
      }
    }
    ```

### POST /workspace/update

ワークスペースのベースディレクトリ設定などを更新します。

- リクエスト
  - メソッド: POST
  - パス: /workspace/update
  - Content-Type: application/json
  - 本文: ContextDto（JSON）
    ```json
    {
      "workspace": "ワークスペースディレクトリ",
      "datasetBase": "データセットベースディレクトリ",
      "resultBase": "結果出力ディレクトリ",
      "settingBase": "設定ファイルディレクトリ",
      "templateBase": "テンプレートディレクトリ",
      "jdbcBase": "JDBC設定ディレクトリ",
      "xlsxSchemaBase": "Excelスキーマディレクトリ"
    }
    ```

- レスポンス
  - Content-Type: application/json
  - 本文: WorkspaceDto（/resourcesと同じ形式）