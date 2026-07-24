# convert 詳細（resultType別オプション・変更箇所・テスト）

## resultType別の有効オプション

`DataSetConverterOption.toParametersBuilder()`の分岐に対応（`-result.*`プレフィックス）:

| resultType | 有効オプション | converterの書き出し先 |
|---|---|---|
| `csv`（既定） | `outputEncoding` / `outputExtension` | ファイル |
| `xls` / `xlsx` | `excelTable`（既定`SHEET`） | Excelブック |
| `format` | `outputEncoding` / `format`（ST4テンプレート） / `outputExtension` | ファイル |
| `fixed` | `outputEncoding` / `outputExtension` / `fixedColumnDef` / `fixedLengthType`（既定`char`） | 固定長ファイル |
| `table` | `op`（`DbOperation`、既定`CLEAN_INSERT`） / `jdbc.*` | DBテーブル（JDBC書込） |

全resultType共通: `resultDir`（`-result`）/ `resultPath` / `exportEmptyTable` / `exportHeader`（`table`除く）。

## 実装チェックリスト（変換仕様の変更箇所）

| ファイル（`application/`・`dataset/`配下） | 内容 |
|---|---|
| `dataset/DataSourceType.java`・`dataset/producer/*`・`option/DataSourceTypeOptionFactory.java`・`option/DataSetLoadOption.java` | 入力形式（srcType）の追加・変更 |
| `dataset/ResultType.java`・`dataset/converter/*`・`dataset/converter/DataSetConverterLoader.java`・`option/DataSetConverterOption.java` | 出力形式（resultType）の追加・変更 |
| `command/ConvertOption.java`・`ConvertDto.java` | `-src`/`-result`の束ね直しが要る場合のみ |
| `command/ConvertTest.java` | 統合テスト |

## テストの場所

- 統合テストパラメータ: `core/src/test/resources/yo/dbunitcli/application/command/paramConvert*.txt`（入出力形式マトリクスの実例集。全コマンド中最多）
- 入力フィクスチャ: `src/csv/`・`src/xlsx/`・`src/fixed/`等（srcType別）
- 期待値: `expect/csv2xlsx/`・`expect/xlsx2csv/`・`expect/filter/`・`expect/sort/`等
- DB絡みは`DBIntegrationTest`/`H2IntegrationTest`
