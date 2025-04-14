# DBUnit CLI Sidecar API エンドポイント概要

このドキュメントでは、DBUnit CLI Sidecarが提供するREST APIのエンドポイントについて説明します。

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
   - データソース設定の管理
   - クエリー実行用の接続情報管理

## 3. エンドポイント詳細

### 3.1. Workspace

#### GET /workspace/resources
- ワークスペースの状態（設定ファイルリスト、ディレクトリパスなど）を取得
- レスポンス: WorkspaceDto（JSON）

#### POST /workspace/update
- ワークスペースのベースディレクトリ設定などを更新
- リクエスト: ContextDto（JSON）
- レスポンス: "success"（テキスト）

### 3.2. Command Settings

各コマンド用のエンドポイント（{command}は compare, convert, generate, parameterize, run のいずれか）

#### GET /{command}/add
- 新規コマンド設定を追加
- レスポンス: 設定名リスト（JSON配列）

#### POST /{command}/copy
- 既存設定をコピー
- リクエスト: コピー元設定名
- レスポンス: 設定名リスト（JSON配列）

#### POST /{command}/delete
- 設定を削除
- リクエスト: 削除する設定名
- レスポンス: 設定名リスト（JSON配列）

#### POST /{command}/rename
- 設定名を変更
- リクエスト: 古い名前と新しい名前
- レスポンス: 設定名リスト（JSON配列）

#### POST /{command}/load
- 設定内容を読み込み
- リクエスト: 設定名
- レスポンス: パラメータMap（JSON）

#### GET /{command}/reset
- デフォルト設定を取得
- レスポンス: デフォルトパラメータMap（JSON）

#### POST /{command}/refresh
- パラメータ更新とバリデーション
- リクエスト: パラメータMap
- レスポンス: 検証済みパラメータMap（JSON）

#### POST /{command}/save
- 設定を保存
- リクエスト: CommandRequestDto（JSON）
- レスポンス: "success"（テキスト）

#### POST /{command}/exec
- コマンドを実行
- リクエスト: CommandRequestDto（JSON）
- レスポンス: 結果ディレクトリパス（テキスト）

### 3.3. Resource Files

以下のエンドポイントにより各種リソースファイルを管理します。
データセット設定用のエンドポイントは以下の通りです：

#### GET /dataset-setting/list
- データセット設定ファイル一覧を取得
- レスポンス: 設定ファイル名リスト（JSON配列）

#### POST /dataset-setting/load
- データセット設定を読み込み
- リクエスト: 設定ファイル名（テキスト）
- レスポンス: 設定内容（JSON）

#### POST /dataset-setting/save
- データセット設定を保存
- リクエスト: DatasetRequestDto（JSON）
- レスポンス: "success"（テキスト）

#### POST /dataset-setting/delete
- データセット設定を削除
- リクエスト: 設定ファイル名（テキスト）
- レスポンス: "success"（テキスト）

その他のリソースファイル（Excelスキーマ、テンプレートなど）も同様のエンドポイントパターンで管理されます。

### 3.4. Query Datasource

#### GET /query-datasource/list
- データソース一覧を取得
- クエリパラメータ: type（DataSourceType）
- レスポンス: データソース名リスト（JSON配列）

#### POST /query-datasource/load
- データソース設定を読み込み
- リクエスト: データソース情報
- レスポンス: 設定内容（テキスト）
