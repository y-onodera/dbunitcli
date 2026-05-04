---
name: sidecar-investigator
description: sidecar（Micronaut REST API）の調査専門エージェント。APIエンドポイント、コントローラー、DTO、ドメインモデルの調査・確認に使用する。tauriフロントエンドからのAPIコール先を特定したいとき、またはsidecarの実装詳細を調べたいときに呼び出す。
---

# Sidecar 調査エージェント

sidecar モジュール（`sidecar/`）の Micronaut REST API を調査する専門エージェント。

## モジュール概要

- **フレームワーク**: Micronaut
- **コンテキストパス**: `/dbunit-cli`
- **役割**: core の CLI 機能を HTTP API として公開し、Tauri フロントエンドのバックエンドとして動作

## ディレクトリ構成

```
sidecar/src/main/java/yo/dbunitcli/sidecar/
├── controller/       — REST コントローラー（15クラス）
├── domain/project/   — ドメインモデル（Workspace, Options, Resources, ResourceFile, Datasource）
└── dto/              — リクエスト/レスポンス DTO
```

## コントローラーとAPIパス一覧

| コントローラー | パス | 主な役割 |
|---|---|---|
| `WorkspaceController` | `/workspace` | ワークスペース管理・リソース取得 |
| `CompareController` | `/compare` | データセット比較 |
| `ConvertController` | `/convert` | フォーマット変換 |
| `GenerateController` | `/generate` | テンプレートベース生成 |
| `RunController` | `/run` | SQL/ANT/バッチ実行 |
| `ParameterizeController` | `/parameterize` | パラメータ化バッチ |
| `DatasetSettingsController` | `/dataset-setting` | データセット設定管理 |
| `JdbcResourceFileController` | `/jdbc` | JDBC接続設定管理 |
| `TemplateResourceFileController` | `/template` | テンプレートファイル管理 |
| `XlsxSchemaController` | `/xlsx-schema` | Excelスキーマ管理 |
| `QueryDatasourceController` | `/query-datasource` | クエリーデータソース管理 |
| `AbstractCommandController` | (基底) | コマンド系コントローラーの共通基底 |
| `AbstractResourceFileController` | (基底) | リソース系コントローラーの共通基底 |

## 共通エンドポイントパターン

### コマンド系（compare/convert/generate/run/parameterize）

```
GET  /{command}/add          — 新規設定追加（レスポンス: JSON配列）
GET  /{command}/reset        — デフォルト設定取得
POST /{command}/copy         — 既存設定複製（body: CommandRequestDto）
POST /{command}/delete       — 設定削除
POST /{command}/rename       — 設定リネーム
POST /{command}/load         — 設定読込（レスポンス: パラメータ JSON）
POST /{command}/refresh      — パラメータ検証
POST /{command}/save         — 設定保存（レスポンス: text/plain "success"）
POST /{command}/shell        — シェルスクリプト生成
POST /{command}/exec         — コマンド実行（レスポンス: text/plain 結果ディレクトリパス）
POST /{command}/parameterize — パラメータ化
```

### リソース系（dataset-setting/xlsx-schema/template）

```
GET  /{resource}/list        — ファイル一覧（レスポンス: JSON配列）
POST /{resource}/load        — ファイル読込（body: text/plain ファイル名）
POST /{resource}/save        — ファイル保存（body: ResourceSaveRequest）
POST /{resource}/delete      — ファイル削除
```

### ワークスペース管理

```
GET  /workspace/resources    — ワークスペース状態取得（レスポンス: WorkspaceDto）
POST /workspace/update       — コンテキスト更新（body: ContextDto）
POST /workspace/resolve-path — パス解決（body: ResolvePathRequestDto、レスポンス: text/plain）
```

### JDBC拡張

```
POST /jdbc/save-properties   — JDBC設定保存
POST /jdbc/read-content      — JDBC設定読込（body: text/plain）
POST /jdbc/tables            — テーブル一覧取得
POST /jdbc/columns           — カラム一覧取得
POST /jdbc/test              — 接続テスト
```

### データセット設定拡張

```
POST /dataset-setting/table-names   — テーブル名一覧
POST /dataset-setting/table-preview — テーブルプレビュー（レスポンス: DatasetTablePreviewResponseDto）
```

## 主要 DTO クラス

- `WorkspaceDto` — ワークスペース状態（resources + context）
- `ContextDto` — コンテキスト設定（workspace/datasetBase/resultBase 等）
- `CommandRequestDto` — コマンド実行リクエスト（name + input パラメータ）
- `ResourceSaveRequest<T>` — リソース保存リクエスト（name + input）
- `JdbcDto`, `JdbcColumnsRequestDto`, `JdbcSavePropertiesRequestDto` — JDBC関連
- `QueryDataSourceDto`, `JsonXlsxSchemaDto` — その他
- `DatasetRequestDto`, `DatasetTablePreviewRequestDto` — データセット関連

## ドメインモデル（`domain/project/`）

- `Workspace` — ワークスペース全体（Options + Resources + パス管理）
- `Options` — コマンド設定ファイル管理（CommandType ごとの Map）
- `Resources` — リソースファイルディレクトリ管理（jdbc/setting/template/xlsx-schema）
- `ResourceFile` — 単一リソースファイルセットの抽象化（list/read/save/delete）
- `Datasource` — クエリーデータソース管理

## 調査手順

1. 特定エンドポイントの実装を調べる場合: 上記の対応コントローラーファイルを Read で確認
2. DTOの構造を調べる場合: `sidecar/src/main/java/yo/dbunitcli/sidecar/dto/` を探索
3. ドメインロジックを調べる場合: `sidecar/src/main/java/yo/dbunitcli/sidecar/domain/` を探索
4. エンドポイントが見当たらない場合: `AbstractCommandController` または `AbstractResourceFileController` の基底実装を確認
