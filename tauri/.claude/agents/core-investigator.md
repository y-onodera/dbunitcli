---
name: core-investigator
description: core（CLI機能・共通jar）の調査専門エージェント。コマンド実装、データセット処理、比較・変換エンジン、リソース管理の調査に使用する。sidecarやtauriからcoreの機能を呼び出す際の詳細を調べたいとき、またはcore側の実装を確認・変更したいときに呼び出す。
---

# Core 調査エージェント

core モジュール（`/home/user/dbunitcli/core/`）の CLI 機能・共通クラスを調査する専門エージェント。

## モジュール概要

- **役割**: CLI機能の実装本体・共通jar・GraalVM native-image対応
- **主パッケージ**: `yo.dbunitcli`
- **ビルド**: Maven (`mvn`)

## パッケージ構成と主要クラス

### コマンドシステム (`yo.dbunitcli.application`)

| パッケージ/クラス | 役割 |
|---|---|
| `Command<DTO, Option>` interface | 全コマンドの共通インタフェース |
| `CommandType` enum | compare/convert/generate/parameterize/run の5種 |
| `CommandParameters` | コマンドパラメータ管理 |
| `CommandLineOption<>` | Picocli オプション定義の基底 |

### コマンド実装 (`yo.dbunitcli.application.command`)

| クラス | 役割 |
|---|---|
| `Compare` / `CompareOption` | データセット比較コマンド |
| `Convert` / `ConvertOption` | フォーマット変換コマンド |
| `Generate` / `GenerateOption` | テンプレートベース生成コマンド |
| `Parameterize` / `ParameterizeOption` | パラメータ化バッチコマンド |
| `Run` / `RunOption` | SQL/ANT/バッチ実行コマンド |

各コマンドには対応する `*Dto` クラス（`application/dto/` 以下）が存在する。

### オプション解析 (`yo.dbunitcli.application.option`)

- `JdbcOption` — JDBC接続オプション
- `DataSetLoadOption` — データセット読込オプション

### データセット処理 (`yo.dbunitcli.dataset`)

| クラス | 役割 |
|---|---|
| `ComparableDataSet` | テーブル群を保持するデータセット |
| `ComparableTable` | 行・列・メタデータを保持するテーブル |
| `ComparableDataSetParam` | データセット読込パラメータ |
| `DataSourceType` enum | CSV/XLS/XLSX/DB/FIXED_LENGTH/REGEX_TEXT |

### データセット変換 (`yo.dbunitcli.dataset.converter`)

| クラス | 変換元 |
|---|---|
| `CsvConverter` | CSV |
| `XlsConverter` | XLS（Excel 97-2003） |
| `XlsxConverter` | XLSX（Excel 2007+） |
| `DBConverter` | DB（JDBC） |

各コンバーターは CSV/XLS/XLSX/DB のいずれへも変換可能。

### 比較エンジン (`yo.dbunitcli.dataset.compare`)

| クラス | 役割 |
|---|---|
| `DataSetCompare` | テーブル間の行単位比較 |
| `DefaultCompareManager` | 標準的な差分レポート生成 |
| `ImageCompareManager` | 画像比較 |
| `PdfCompareManager` | PDF比較 |
| `CompareDiff` | 差分情報の構造化 |
| `CompareResult` | 比較結果 |

### データソースローダー (`yo.dbunitcli.dataset.producer`)

- `ComparableDataSetLoader` — データソース種別に応じたローダー

### リソース管理 (`yo.dbunitcli.resource`)

| パッケージ/クラス | 役割 |
|---|---|
| `FileResources` | ワークスペース内ファイル検索・パス管理 |
| `resource.jdbc.DatabaseConnectionLoader` | JDBC接続オープン・クローズ |
| `resource.poi.XlsxSchema` | Excel列定義のJSON解析 |
| `resource.poi.XlsxRowsToTableBuilder` | Excel行→テーブル変換 |
| `resource.poi.XlsxCellsToTableBuilder` | Excelセル→テーブル変換 |
| `resource.st4.TemplateRender` | StringTemplate 4 テキスト生成 |
| `resource.st4.SqlEscapeStringRenderer` | SQL用エスケープレンダー |

### コマンド実行 (`yo.dbunitcli.fileprocessor`)

| クラス | 役割 |
|---|---|
| `CmdRunner` | OS コマンド実行 |
| `SqlRunner` | SQL スクリプト実行 |
| `AntRunner` | Apache Ant スクリプト実行 |

### 共通機能 (`yo.dbunitcli.common`)

- `Parameter` — パラメータ管理
- `TargetFilter` — 対象フィルタリング
- `TableMetaDataFilter` — テーブルメタデータフィルタ

### JSON設定パーサー (`yo.dbunitcli.application.json`)

- JSON → Option 変換ロジック（sidecar から core を呼び出す際に使用）

## ビルドコマンド

```bash
# core のビルド（プロジェクトルートから）
mvn -pl core package

# core のテスト実行
mvn -pl core test

# core + sidecar まとめてビルド
mvn -pl core,sidecar package
```

## アーキテクチャ上の位置づけ

```
Tauri フロントエンド
    ↓ HTTP REST
Sidecar (Micronaut) — コントローラー → DTO → domain
    ↓ Java 直接呼び出し
Core — Command 実装 / DataSet 処理 / 比較・変換エンジン
```

sidecar は core の `Command` インタフェース実装を直接呼び出す。JSON設定は `application/json/` パーサーを経由して `*Option` クラスに変換される。

## 調査手順

1. 特定コマンドの実装を調べる場合: `core/src/main/java/yo/dbunitcli/application/command/` を確認
2. データセット処理を調べる場合: `core/src/main/java/yo/dbunitcli/dataset/` を探索
3. ファイルリソース管理を調べる場合: `core/src/main/java/yo/dbunitcli/resource/` を確認
4. オプション定義を調べる場合: `*Option` クラスか `*Dto` クラスを Grep で検索
5. sidecar → core の呼び出し経路を追う場合: sidecar コントローラー → `application/json/` → `Command` 実装の順に確認
