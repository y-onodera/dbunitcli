# scaffold target 詳細

## 対応target と出力ファイル

`-target` の値ごとに `ScaffoldOption.execute()` 内のフラグ（`generateDdl`/`generateJavaBean`/`generateXlsxSchema`/`generateFixedColumnDef`/`generateParameter`）で分岐する。

| target | 対応する`GenerateType` | setting雛型 | template雛型 | dataset雛型 | parameter雛型 |
|---|---|---|---|---|---|
| `ddl` | `GenerateType.ddl` | `-setting`指定時、`generateType.defaultSettingsPath()`（`sql/ddlSettings.json`）をコピー | 非対応（`-template`は無視される。組み込みテンプレート固定） | `-dataset.*`指定時、DDL_SCHEMA_COLUMNS形状のダミーテーブルを`src/`配下に出力 | `-parameter`指定時、`-generateType=ddl`を含む`.param`ファイルを`writeGenericParamFile()`で生成 |
| `javaBean` | `GenerateType.javaBean` | 同上（`javabean/javaBeanSettings.json`） | 同上（非対応） | 同上 | 同上（`-generateType=javaBean`） |
| `xlsxSchema` | `GenerateType.xlsxSchema` | `dataset`指定時、`xlsxschema/xlsxSchemaSettings.json`（サンプル固定パス）をunitSetting名でコピー | `writeSchemaTemplate()`で`xlsxschema/xlsxSchemaScaffoldTemplate.stg,.txt`をコピー（既存の`xlsxSchemaTemplate.*`とは別物、Java precomputationなしでrows属性のみから出力） | 同上 | `writeSchemaParamFile()`で`-generateType=txt -unit=table`の`.param`ファイルを生成（xlsxSchema自体ではなくtxt経由で同内容を再現する点に注意） |
| `fixedColumnDef` | `GenerateType.fixedColumnDef` | `fixedcolumndef/fixedColumnDefSettings.json`（サンプル固定パス） | `fixedcolumndef/fixedColumnDefTemplate.stg`（stgはそのまま）+ `fixedcolumndef/fixedColumnDefScaffoldTemplate.txt`（txt側のみScaffold専用） | 同上 | 同上（`-generateType=txt -unit=table`） |
| `parameter` | なし（`-commandType`必須） | — | — | — | `CommandParameters(Type, commandInput).shrink()`で任意コマンドの`.param`ファイルを生成 |

## dataset雛型の中身

`hasDataset()`（`-dataset.srcType`かつ`-dataset.src`が両方指定）の場合のみ`writeDatasetSrcFiles()`が動く。実データではなく、実データの列名から `DDL_SCHEMA_COLUMNS`（COLUMN_NAME/TYPE_NAME/COLUMN_SIZE/DECIMAL_DIGITS/NULLABLE/IS_PK/PK_NAME/REMARKS/TABLE_REMARKS/TABLE_NAME/PACKAGE の11列）形状のダミー行を1テーブルにつき1行/カラムで生成し、`-datasetType`（デフォルトcsv）の形式で`src/`配下に書き出す。ユーザーはこのダミーcsvの各行を編集してテンプレートの入力データとして使う。

## 未対応target

`settings` / `sql` / `xlsxTemplate` は `ScaffoldOption.execute()` にこれらを判定する分岐が存在せず、`-target`にこれらを指定しても何も出力されない。追加する場合は本ファイル冒頭の表と同じ観点（setting/template/dataset/parameter雛型それぞれ何が必要か）を設計してから実装する。

## テストの場所

- テストクラス: `core/src/test/java/yo/dbunitcli/application/command/ScaffoldTest.java`（`DdlTarget`等の`@Nested`クラスで target 別に整理）
- 入力フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/command/scaffold/src/SAMPLE.csv`
