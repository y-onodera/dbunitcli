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

`parallel`: ソースの並列読込数（`ComparableDataSetParam.parallel`へ渡す。Compareはnew/oldの読込に適用）。未指定はCPUコア数、`1`は逐次。

- 表示: 並列化できる組み合わせのときだけ。Convertは`DataSourceType.isParallelizable()`かつ`DataSetConverterOption.isParallelizable()`（xls/xlsxはexcelTableが環境変数等を含む自由入力のため、`SHEET`と確定しているときだけ非表示）、Compareはnew/oldのどちらかが`DataSourceType.isParallelizable()`
- 実行: `ParallelTableMapping`。producerの`isParallelizableSource()`（`DataSourceType.isParallelizable()`と一致させる。`ParallelTableMappingTest`で検証）、converterの`isParallelizable()`（FlatFile系・xls/xlsxのBOOK・table）、join・件数分割なしのとき、出力テーブルを共有するソースを同じレーン（同一スレッド・元の順）にまとめて作業スレッドで処理する
- 出力テーブルの先読み: タスクの`outputTableCandidates()`（xls/xlsxはシート名とxlsxSchemaから）
- スレッド間で共有できない資源: DB接続は作業スレッドごとに張る（入力は`ComparableTableMappingTask.forWorker()`、出力は`IDataSetConverter.splitForParallel()`。どちらも`ParallelTableMapping.WorkerResources`から取得し終了時に閉じる）。作業スレッドへの文脈の引き継ぎは`ThreadContexts.propagate`（`FileResources`のContextは常に、それ以外は`META-INF/services`に登録した`ThreadContextPropagator`。sidecarは`JobContextPropagator`でログ振り分け先を引き継ぐ）

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
