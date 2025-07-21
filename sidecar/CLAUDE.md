# DBUnit CLI Sidecar プロジェクト

## プロジェクト概要
DBUnit CLI Sidecar は、DBUnit CLI コマンドラインツールのためのREST APIサーバーです。Micronaut フレームワークをベースとして構築されており、Webアプリケーションやタウリアプリケーションから DBUnit CLI の機能を利用できるようにする HTTP API を提供します。

## 主要機能

### 1. REST APIサーバー
- **DBUnit CLI のラップ**: 全てのコアコマンド（Compare, Convert, Generate, Run, Parameterize）をHTTP APIとして提供
- **Micronaut フレームワーク**: 高速で軽量なWebサーバー
- **CORS対応**: Tauri アプリケーションからのアクセスに対応
- **JSON ベース**: 統一されたJSON形式でのリクエスト・レスポンス

### 2. ワークスペース管理
- **プロジェクト指向**: ワークスペースディレクトリベースの作業環境
- **ファイル管理**: パラメータファイル、設定ファイル、リソースファイルの統合管理
- **コンテキスト切り替え**: 動的なワークスペース切り替え機能

### 3. リソース管理
- **JDBC設定**: データベース接続設定の管理
- **テンプレート**: StringTemplate 4 テンプレートファイルの管理
- **Excel スキーマ**: 複雑な Excel ファイル処理のためのスキーマ定義
- **データセット設定**: メタデータとフィルタリング設定

### 4. パラメータ管理
- **コマンド別管理**: 各DBUnit CLIコマンド用のパラメータセット管理
- **CRUD操作**: パラメータセットの作成、読み込み、更新、削除
- **名前管理**: 自動的な一意名生成とリネーム機能

## アーキテクチャ構成

### パッケージ構成

#### `yo.dbunitcli.sidecar`
- **Application.java**: メインエントリーポイント、Micronaut アプリケーション起動
- **Dual Mode**: CLI モード（-cli フラグ）とサーバーモードの両対応

#### `yo.dbunitcli.sidecar.controller`
- **抽象基底クラス**:
  - `AbstractCommandController`: DBUnit CLI コマンド実行の共通機能
  - `AbstractResourceFileController`: リソースファイル管理の共通機能

- **コマンドコントローラー**: 各DBUnit CLIコマンドのREST APIエンドポイント
  - `CompareController` (`/compare`): データ比較機能
  - `ConvertController` (`/convert`): データ変換機能
  - `GenerateController` (`/generate`): データ生成機能
  - `RunController` (`/run`): スクリプト実行機能
  - `ParameterizeController` (`/parameterize`): バッチ処理機能

- **リソースコントローラー**: 設定ファイル管理
  - `DatasetSettingsController` (`/dataset-setting`): データセット設定
  - `JdbcResourceFileController` (`/jdbc`): JDBC設定
  - `TemplateResourceFileController` (`/template`): テンプレート管理
  - `XlsxSchemaController` (`/xlsx-schema`): Excelスキーマ管理

- **専用コントローラー**:
  - `QueryDatasourceController` (`/query-datasource`): データソース管理
  - `WorkspaceController` (`/workspace`): ワークスペース管理

#### `yo.dbunitcli.sidecar.domain.project`
- **Workspace**: ワークスペース全体を管理するドメインオブジェクト
- **Options**: コマンドパラメータファイルの管理
- **Resources**: リソースファイル（JDBC, テンプレート等）の管理
- **ResourceFile**: 低レベルファイル操作の抽象化
- **Datasource**: データソース設定の管理
- **CommandType**: コマンドタイプの列挙型

#### `yo.dbunitcli.sidecar.dto`
- **API用DTO**: REST APIのリクエスト・レスポンス用データ転送オブジェクト
- **主要DTO**:
  - `WorkspaceDto`: ワークスペース全体の状態
  - `ContextDto`: ワークスペースコンテキスト情報
  - `CommandRequestDto`: コマンド実行リクエスト
  - `DatasetSettingDto`: データセット設定の詳細
  - `JsonXlsxSchemaDto`: Excel スキーマ定義

### REST API エンドポイント

#### 共通コマンド操作 (全コマンドタイプ)
```
GET  /{command}/add              - 新しいパラメータセット作成
POST /{command}/copy             - パラメータセットコピー
POST /{command}/delete           - パラメータセット削除
POST /{command}/rename           - パラメータセットリネーム
POST /{command}/load             - パラメータセット読み込み
GET  /{command}/reset            - パラメータリセット
POST /{command}/refresh          - パラメータ状態更新
POST /{command}/save             - パラメータ保存
POST /{command}/exec             - コマンド実行
```

#### リソース管理操作
```
GET  /{resource}/list            - リソースファイル一覧
POST /{resource}/load            - リソースファイル読み込み
POST /{resource}/save            - リソースファイル保存
POST /{resource}/delete          - リソースファイル削除
```

#### ワークスペース管理
```
GET  /workspace/resources        - ワークスペースリソース取得
POST /workspace/update           - ワークスペースコンテキスト更新
```

#### データソース管理
```
GET  /query-datasource/list      - データソース一覧（タイプ別）
POST /query-datasource/load      - データソース読み込み
POST /query-datasource/save      - データソース保存
POST /query-datasource/delete    - データソース削除
```

### 技術スタック

#### 主要依存関係
- **Micronaut 4.8.9**: マイクロサービスフレームワーク
- **Micronaut Netty**: HTTPサーバー実装
- **Micronaut Serde Jackson**: JSON シリアライゼーション
- **DBUnit CLI Core**: コア機能の依存関係
- **Java 21**: 最新のJava LTS版

#### ビルド設定
- **Maven**: プロジェクト管理
- **GraalVM Native Image**: ネイティブコンパイル対応
- **Micronaut AOT**: Ahead-of-Time コンパイル最適化

### ネイティブイメージ対応

#### GraalVM 設定
- **リフレクション設定**: `reflect-config.json`
- **リソース設定**: `resource-config.json`
- **JNI設定**: `jni-config.json`
- **シリアライゼーション設定**: `serialization-config.json`
- **Oracle DB 対応**: Oracle JDBC ドライバーの初期化設定

## 実行方法

### サーバーモード
```bash
# 通常の実行
java -jar dbunit-cli-sidecar-jar-with-dependencies.jar

# ネイティブイメージ実行
./dbunit-cli-sidecar

# ワークスペース指定
java -Dworkspace=/path/to/workspace -jar dbunit-cli-sidecar.jar
```

### CLIモード
```bash
# CLIモードでの実行
java -jar dbunit-cli-sidecar.jar -cli [dbunit-cli arguments]
```

### サーバー設定

#### application.properties
- **アプリケーション名**: dbunit-cli-sidecar
- **コンテキストパス**: /dbunit-cli
- **CORS有効**: tauri://localhost, http://tauri.localhost からのアクセス許可
- **デフォルトポート**: 8080

#### 環境変数
- `workspace`: ワークスペースディレクトリパス
- `dataset.base`: データセットベースディレクトリ
- `result.base`: 結果出力ベースディレクトリ

## 開発・デバッグ情報

### ビルドコマンド
```bash
# 通常のビルド
mvn clean compile

# テスト実行
mvn test

# 統合テスト実行
mvn test -PIntegrationTest

# ネイティブイメージビルド
mvn clean package -Pnative

# 全依存関係を含むJARビルド
mvn clean package
```

### テスト構成
- **JUnit 5**: テストフレームワーク
- **Micronaut Test**: Micronaut統合テスト
- **HTTP Client**: REST API テスト
- **ワークスペーステスト**: 実際のファイル操作テスト

### ログ設定
- **Logback**: ログフレームワーク
- **設定ファイル**: `src/main/resources/logback.xml`
- **構造化ログ**: JSON形式での出力対応

## 設計パターン

### 1. Controller-Service-Domain Pattern
- **Controller**: HTTP エンドポイントとリクエスト処理
- **Domain**: ビジネスロジックとデータ管理
- **DTO**: API データ転送

### 2. Template Method Pattern
- **AbstractCommandController**: 共通のコマンド実行フロー
- **AbstractResourceFileController**: 共通のリソース管理操作

### 3. Builder Pattern
- **Workspace.Builder**: 複雑なワークスペース構築
- **Options.Builder**: パラメータファイル初期化
- **Resources.Builder**: リソースディレクトリ設定

### 4. Repository Pattern (ファイルベース)
- **ResourceFile**: ファイルシステムアクセスの抽象化
- **Options**: パラメータファイルのCRUD操作
- **Resources**: リソースファイルの管理

## 主要な設計判断

### 1. Micronaut選択理由
- **軽量**: 高速起動と低メモリ使用量
- **ネイティブイメージ対応**: GraalVM との優れた互換性
- **マイクロサービス指向**: REST API に最適

### 2. ワークスペース中心設計
- **プロジェクト管理**: ファイルベースのプロジェクト構造
- **コンテキスト分離**: 複数プロジェクトの並行作業
- **設定管理**: プロジェクト固有の設定とリソース

### 3. 統合API設計
- **一貫性**: 全コマンドで統一されたAPI パターン
- **RESTful**: HTTP標準に準拠したエンドポイント設計
- **エラーハンドリング**: 統一されたエラーレスポンス

### 4. ファイルシステム抽象化
- **ResourceFile**: 低レベルファイル操作の統一
- **型安全性**: コマンドタイプとデータソースタイプの強い型付け
- **キャッシング**: ファイル一覧のメモリキャッシュ

## 連携アプリケーション

### Tauri アプリケーション
- **CORS設定**: Tauri Webview からのアクセス対応
- **ローカルAPI**: デスクトップアプリケーションのバックエンド
- **ファイルシステム**: ローカルファイルシステムへの安全なアクセス

### CLI統合
- **デュアルモード**: サーバーとCLIの両方の機能
- **設定共有**: 同一の設定ファイルとワークスペース構造
- **一貫性**: CLI とAPI での同一の機能セット

このサイドカーサーバーは、DBUnit CLI の強力な機能を Web アプリケーションやデスクトップアプリケーションから利用可能にする、堅牢で高性能な REST API プラットフォームです。