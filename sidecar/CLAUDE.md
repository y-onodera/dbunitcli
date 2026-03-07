# DBUnit CLI Sidecar

DBUnit CLI のREST APIサーバー。Micronaut ベースで、Tauri アプリケーションのバックエンドとして機能。

## 主要機能
- **コマンドAPI**: Compare / Convert / Generate / Run / Parameterize をHTTP APIとして提供
- **ワークスペース管理**: ディレクトリベースの作業環境管理・コンテキスト切り替え
- **リソース管理**: JDBC設定・テンプレート・Excelスキーマ・データセット設定の CRUD
- **パラメータ管理**: コマンド別パラメータセットの作成・読込・更新・削除・リネーム

## パッケージ構成

### `yo.dbunitcli.sidecar.controller`
- `AbstractCommandController` — コマンド実行の共通基底
- `AbstractResourceFileController` — リソースファイル管理の共通基底
- コマンド系: `CompareController`(`/compare`), `ConvertController`(`/convert`), `GenerateController`(`/generate`), `RunController`(`/run`), `ParameterizeController`(`/parameterize`)
- リソース系: `DatasetSettingsController`(`/dataset-setting`), `JdbcResourceFileController`(`/jdbc`), `TemplateResourceFileController`(`/template`), `XlsxSchemaController`(`/xlsx-schema`)
- 専用: `QueryDatasourceController`(`/query-datasource`), `WorkspaceController`(`/workspace`)

### `yo.dbunitcli.sidecar.domain.project`
`Workspace`, `Options`, `Resources`, `ResourceFile`, `Datasource`, `CommandType`

### `yo.dbunitcli.sidecar.dto`
`WorkspaceDto`, `ContextDto`, `CommandRequestDto`, `DatasetSettingDto`, `JsonXlsxSchemaDto`

## REST API パターン

コンテキストパス: `/dbunit-cli`

コマンド系 `/{cmd}`: `GET /add`, `GET /reset`, `POST /load`, `POST /refresh`, `POST /save`, `POST /delete`, `POST /copy`, `POST /rename`, `POST /exec`
リソース系 `/{res}`: `GET /list`, `POST /load`, `POST /save`, `POST /delete`
ワークスペース: `GET /workspace/resources`, `POST /workspace/update`

## 技術スタック
- Micronaut 4.x + Netty + Serde Jackson
- Java 21、GraalVM Native Image 対応

## 環境変数
- `workspace`: ワークスペースディレクトリパス
- `dataset.base`: データセットベースディレクトリ
- `result.base`: 結果出力ベースディレクトリ

## ログ
`src/main/resources/logback.xml`（Logback + SLF4J）
