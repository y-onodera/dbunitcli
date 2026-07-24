---
description: convertコマンド（core）修正時の設計ガイド。入力ロード/出力変換（resultType・srcType）の仕様変更時に参照。
TRIGGER when: command/Convert*.java・option/DataSetLoadOption.java・option/DataSetConverterOption.java・dataset/converter/ の変更、または変換仕様変更を伴う実装タスクの開始時。
DO NOT TRIGGER when: 読み取りのみ、または他コマンド（Compare/Generate/Run/Parameterize/Scaffold）のみの変更。
---

Convertは、1データソース（`-src`）をロードし別形式（`-result`）へ変換出力するだけの最小コマンド（`ConvertOption.convertDataset()`がloaderにconverterを差して流すのみ）。**Convert固有ロジックはほぼ無く、変更対象は共通の入力ロード基盤・出力変換基盤の側**になる。両基盤はCompare（`-new`/`-old`/`-result`）・Generate（`-src`）・Parameterize（`-param`）とも共有される。

## 変更対象は2つの共通オプション

1. **入力ロード（`-src.*` = `DataSetLoadOption`）**: `srcType`（`DataSourceType`: csv/csvq/xls/xlsx/fixed/reg/table/sql/dir/file/none）ごとのproducerで読み込み、`-src.setting`で`TableSeparators`（分割・リネーム・列フィルタ・ソート・列追加）を適用。入力形式追加＝`DataSourceType`＋`dataset/producer/`＋`DataSourceTypeOptionFactory`
2. **出力変換（`-result.*` = `DataSetConverterOption`）**: `resultType`（csv/xls/xlsx/table/format/fixed）ごとの`IDataSetConverter`で書き出す（`DataSetConverterLoader`が生成）。`table`はJDBC書込（`-result.op`/`-result.jdbc.*`）、`format`はST4、`fixed`は固定長（`-result.fixedColumnDef`）。出力形式追加＝`ResultType`＋converter実装＋`DataSetConverterLoader`

変換はこの2軸の直積（`DataSourceType`⇔`ResultType`は`ResultType.toDataSourceType()`で対応）。`ConvertOption`/`ConvertDto`自体は`-src`と`-result`を束ねるだけで直接いじる場面は少ない。resultType別の有効オプション・変更箇所・テスト場所は `references/convert-internals.md`。

## 関連スキル

同じ`-result`基盤はCompare（`compare-command`）、同じ`-src`基盤はGenerate（`generate-command`）と共有。影響確認は`check-application-impact`、tauriヘルプは`tauri:update-help`。
