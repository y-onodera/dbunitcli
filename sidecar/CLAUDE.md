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
- 専用: `QueryDatasourceController`(`/query-datasource`), `WorkspaceController`(`/workspace`), `JobController`(`/job`)
- 全コントローラに `@ExecuteOn(TaskExecutors.BLOCKING)`（仮想スレッド）を付与する。新規コントローラも同様

### `yo.dbunitcli.sidecar.domain.project`
`Workspace`, `Options`, `Resources`, `ResourceFile`, `Datasource`, `CommandType`

### `yo.dbunitcli.sidecar.dto`
`WorkspaceDto`, `ContextDto`, `CommandRequestDto`, `DatasetSettingDto`, `JsonXlsxSchemaDto`

## REST API パターン

コンテキストパス: `/dbunit-cli`

コマンド系 `/{cmd}`: `GET /add`, `GET /reset`, `POST /load`, `POST /refresh`, `POST /save`, `POST /delete`, `POST /copy`, `POST /rename`, `POST /exec`
リソース系 `/{res}`: `GET /list`, `POST /load`, `POST /save`, `POST /delete`
ワークスペース: `GET /workspace/resources`, `POST /workspace/update`

### ジョブ（`<uri>/job`、同期版は残す）
- 対象: `/{cmd}/exec`, `scaffold/exec`, `{dataset-setting,xlsx-schema,fixed-column-def,template}/generate`, `dataset-setting/{table-names,table-preview}`, `jdbc/{tables,columns,test}`
- `POST <uri>/job`→`{"jobId"}`、`GET /job/{id}/events`→SSE（`log` 行、最後に `done`）、`POST /job/{id}/cancel`→interrupt
- 失敗時の値は `ControllerExceptionHandler` と揃える

## 技術スタック
- Micronaut 4.x + Netty + Serde Jackson
- Java 21、GraalVM Native Image 対応

## 環境変数
- `workspace`: ワークスペースディレクトリパス
- `dataset.base`: データセットベースディレクトリ
- `result.base`: 結果出力ベースディレクトリ

## ログ
`src/main/resources/logback.xml`（Logback + SLF4J）
