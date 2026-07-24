# compare 詳細（変更箇所・オプション所在・テスト）

## targetType別の挙動対応表

| targetType | 入力srcType | テーブル名/比較キー | 比較Manager | `-setting`の扱い |
|---|---|---|---|---|
| `data`（既定） | ユーザー指定（csv/xls/xlsx/db/…） | `-setting`のJSON通り | `DefaultCompareManager`（`-createPatchSql`でパッチSQL生成） | JSONをそのまま`TableSeparators`化 |
| `image` | `file`＋`extension=png`に強制 | 全テーブルを`TARGET`にリネーム＋比較キー`NAME` | `ImageCompareBuilder`（`-image.*`パラメータ） | `NAME`キー固定の合成setting |
| `pdf` | `file`＋`extension=pdf`に強制 | 同上 | `PdfCompareManager`（`-image.*`パラメータ） | 同上 |

image/pdfのsrcType強制は`CompareOption.IMAGE_TYPE_PARAM_MAPPER`/`PDF_TYPE_PARAM_MAPPER`（`toDto()`で適用、`srcType=`/`extension=`を除去して差し替え）。テーブルリネーム＋キー付与は`newDataSet()`/`oldDataSet()`の`ifMatch(targetType != data, …)`。

## 期待差分（`-expect`）の判定フロー

`compare()`は2段階:
1. `-new` vs `-old`を比較し、差分レポートを`-result`の`converter()`で出力（`resultDataSet()`が読み戻せる形）
2. `-expect.src`指定時のみ、出力した差分レポート（`resultDataSet()`）と期待差分（`expectDataSet()`）を再Compareし、**差分が無ければ成功**（差分レポートが期待通り＝OK）。`expectedDiffConverter()`は`expectedDiff`サブディレクトリへ書き出す

`-expect.srcType`は`csv/csvq/xls/xlsx/sql/none`のみ許可（`toParametersBuilder()`のFilter）。`-expect`未指定時は段階1の`existDiff()`否定がそのまま成否。

## オプションの所在

| オプション | `CompareDto`フィールド | 用途 |
|---|---|---|
| `-targetType` | `targetType` | 軸1。`CompareOption.Type` |
| `-setting` / `-settingEncoding` | `setting`/`settingEncoding` | 軸2。`getTableSeparators()`で`TableSeparators`化 |
| `-createPatchSql` | `createPatchSql` | `DefaultCompareManager`に渡す。行差分時にパッチSQL生成 |
| `-new.*` / `-old.*` / `-expect.*` | `newData`/`oldData`/`expectData` | `DataSetLoadOption`（共通ロード基盤。srcType・setting・列設定等） |
| `-result.*` | `convertResult` | `DataSetConverterOption`（Convertと共通。差分レポート出力形式） |
| `-image.*` | `imageOption` | 軸5。`ImageCompareOption`（image/pdf時のみ有効） |

## 比較エンジンの構造（`dataset/compare/`）

- `DataSetCompareBuilder` → `DataSetCompare`: new/old/converter/tableSeparatorsを受け`CompareResult`を返す
- `DataSetCompare.Manager`実装: `DefaultCompareManager`（データ比較。`getTableCompareStrategies()`が列数・行数・値差分等の戦略を合成）/ `ImageCompareManager` / `PdfCompareManager`
- 差分の後処理: `RowCompareResultHandler`実装（`DiffWriteRowCompareResultHandler`＝差分書き出し、`CreatePatchSqlRowCompareResultHandler`＝パッチSQL生成、`DataRowCompareResultHandler`）
- 比較ルールを足す→`DefaultCompareManager`の戦略追加。差分の出力表現を足す→`*CompareResultHandler`／`DiffTable`

## 実装チェックリスト（compare修正時の変更箇所）

| ファイル（`application/`配下） | 内容 |
|---|---|
| `command/CompareOption.java` | 比較フロー本体。targetType追加時は`Type`定数・srcType強制マッパー・`getDataSetCompareBuilder()`・`getTableSeparators()`の分岐を更新 |
| `command/CompareDto.java` | `@CommandLine.Option`フィールド追加 |
| `command/ImageCompareOption.java` / `dto/ImageCompareDto.java` | image/pdfパラメータの追加 |
| `dataset/compare/*.java` | 比較ルール・差分後処理・Manager実装 |
| `command/CompareOptionTest.java` / `CompareTest.java` | 単体（toParameters往復）/統合（`paramCompare*.txt`＋`expect/compare/**`） |

## テストの場所

- 統合テストパラメータ: `core/src/test/resources/yo/dbunitcli/application/command/paramCompare*.txt`
- 入力フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/src/compare/`
- 期待値（差分レポート）: `core/src/test/resources/yo/dbunitcli/application/expect/compare/`・`expect/multidiff/`・`expect/nodiff/`・`expect/pattern/`
- DB絡みは`DBIntegrationTest`/`H2IntegrationTest`
