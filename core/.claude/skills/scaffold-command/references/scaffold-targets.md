# scaffold target 詳細

## 対応target と出力ファイル

`-target` の値ごとに `ScaffoldOption.execute()` 内のフラグ（`generateDdl`/`generateJavaBean`/`generateXlsxSchema`/`generateFixedColumnDef`/`generateParameter`）で分岐する。

| target | 対応する`GenerateType` | setting雛型 | template雛型 | dataset雛型 | parameter雛型 |
|---|---|---|---|---|---|
| `ddl` | `GenerateType.ddl` | `-setting`指定時、`generateType.defaultSettingsPath()`（`sql/ddlSettings.json`）をコピー | `-template`指定時、`generateType.getStgPath()/getTemplatePath()`（`sql/ddlTemplate.stg,.txt`）をそのままコピー | `-dataset.*`指定時、DDL_SCHEMA_COLUMNS形状のダミーテーブルを`src/`配下に出力 | `-parameter`指定時、`.param`ファイルを`writeGenericParamFile()`で生成。`-template`未指定なら`-generateType=ddl`、指定時は組み込みテンプレートと同内容を`generateType=txt -unit=table`＋コピーしたtemplate/templateGroup＋unitSettingで再現し`-resultPath=$param.tableName$.sql`を付与 |
| `javaBean` | `GenerateType.javaBean` | 同上（`javabean/javaBeanSettings.json`） | 同上（`javabean/javaBeanTemplate.stg,.txt`） | 同上 | 同上。`-template`指定時は`-resultPath=$param.tableName; format="snakeToUpperCamel"$.java`（`-generateType=ddl`の代わりは`javaBean`） |
| `xlsxSchema` | `GenerateType.xlsxSchema` | `dataset`指定時、`xlsxschema/xlsxSchemaSettings.json`（サンプル固定パス、PK/CELLSサブテーブルを`separate`で作る）をunitSetting名でコピー | `writeSchemaTemplate()`で`GenerateType.xlsxSchema.getStgPath()/getTemplatePath()`（`xlsxschema/xlsxSchemaTemplate.stg,.txt`、組み込みと同一ファイル、Scaffold専用コピーは存在しない）をそのままコピー | 同上（列毎に1行、COLUMN_NAME/IS_PK/SHEET_NAME/DATA_START/COLUMN_INDEX/CELL_ADDRESSの6列。詳細は次節） | `writeSchemaParamFile()`で`-generateType=txt -unit=table`の`.param`ファイルを生成（xlsxSchema自体ではなくtxt経由で同内容を再現する点に注意） |
| `fixedColumnDef` | `GenerateType.fixedColumnDef` | `fixedcolumndef/fixedColumnDefSettings.json`（空のプレースホルダー） | `fixedcolumndef/fixedColumnDefTemplate.stg`（stgはそのまま）+ `fixedcolumndef/fixedColumnDefScaffoldTemplate.txt`（txt側のみScaffold専用） | 同上（列毎に1行、name/length/align/padの4列） | 同上（`-generateType=txt -unit=table`） |
| `parameter` | なし（`-commandType`必須） | — | — | — | `CommandParameters(Type, commandInput).shrink()`で任意コマンドの`.param`ファイルを生成 |

## dataset雛型の中身

`hasDataset()`（`-dataset.srcType`かつ`-dataset.src`が両方指定）の場合のみ`writeDatasetSrcFiles()`が動く。実データではなく、実データの列名から1テーブルにつき1行/カラムのダミー行を生成し、`-datasetType`（デフォルトcsv）の形式で`src/`配下に書き出す。列の形はtargetごとに異なる（各targetの`.stg`/`.txt`が消費する形に合わせてある）:

- `ddl`/`javaBean`: `DDL_SCHEMA_COLUMNS`（COLUMN_NAME/TYPE_NAME/COLUMN_SIZE/DECIMAL_DIGITS/NULLABLE/IS_PK/PK_NAME/REMARKS/TABLE_REMARKS/TABLE_NAME/PACKAGE の11列）
- `xlsxSchema`: COLUMN_NAME/IS_PK/SHEET_NAME/DATA_START/COLUMN_INDEX/CELL_ADDRESSの6列。SHEET_NAME/DATA_START/COLUMN_INDEX/CELL_ADDRESSは組み込みgenerateType=xlsxSchemaと同じデフォルト値（テーブル名/1/連番/POI CellReference計算値）で初期化されるが、dataset値を編集するだけで自由に上書きできる
- `fixedColumnDef`: name/length/align/padの4列（length=10/align=left/pad=半角スペースで初期化）

ユーザーはこのダミーcsvの各行を編集してテンプレートの入力データとして使う。

## 未対応target

`settings` / `sql` / `xlsxTemplate` は `ScaffoldOption.execute()` にこれらを判定する分岐が存在せず、`-target`にこれらを指定しても何も出力されない。追加する場合は本ファイル冒頭の表と同じ観点（setting/template/dataset/parameter雛型それぞれ何が必要か）を設計してから実装する。

## テストの場所

- テストクラス: `core/src/test/java/yo/dbunitcli/application/command/ScaffoldTest.java`（`DdlTarget`等の`@Nested`クラスで target 別に整理）
- 入力フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/command/scaffold/src/SAMPLE.csv`
