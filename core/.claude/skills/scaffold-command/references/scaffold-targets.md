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

## 専用ScaffoldTemplate要否の判定

Scaffoldはtargetを`generateType=txt -unit=table`で駆動し、dataset(1行=1列)を`unitSetting`の`separate`でPK等のサブテーブル（`dataset.PK.rows`等）に分けてテンプレートに渡す。組み込みの`generateType.getStgPath()/getTemplatePath()`を無改造でコピーして使えるのは、次の2条件を両方満たす時のみ：

1. **組み込みマクロの引数がスカラーのみか**: 例えば`fixedColumnDef`の`columnEntry(col)`が使う`col.name/length/align/pad`はスカラーのみなので、Scaffoldの1行=1列datasetの各行がそのままマクロ引数になり`.stg`は無改造で流用できる。逆に旧`xlsxSchema`の`row.header`（`List<String>`）のようなリスト値フィールドを要求するマクロは、ST4のテンプレート構文だけでは組み立てられない（`[k:v]`のようなマップ/集約リテラルは実行時の式としては存在せず、`.stg`ファイル冒頭のdictionary宣言でしか使えない）。この場合は組み込み側の`write()`をリファクタリングし、`row.rows`（列毎の生行リスト）／`row.dataset.<name>.rows`（`separate`と同じ形の派生サブテーブル）のような、Scaffoldのdatasetがunit=tableで自然に提供する形へ寄せることで無改造流用に持ち込める（現`xlsxSchema`はこの形。`GenerateType.xlsxSchema.write()`の`toSchemaTable()`/`toColumnRow()`参照）。
2. **固定unitが`table`か**: `isFixedTemplate()=true`で`getFixedUnit()`が`table`以外（`dataset`等）だと`-unitSetting`が一切効かない（`GenerateOption.parameterStream()`は`unit==table`の時のみ`unitTableSeparators()`を評価するため）。この場合、組み込み側の`getFixedUnit()`自体を`table`に変更できないか検討する（旧`xlsxSchema`は`dataset`固定だったが`table`固定に変更した。`resultPath()`のtypeName別自動命名分岐も要追従、`generate-command`スキル参照）。

両方を満たせない場合のみ`{typeName}ScaffoldTemplate.stg`および/または`.txt`を新設する。現状: `ddl`/`javaBean`/`xlsxSchema`はいずれも組み込みを完全流用（専用ファイルなし）、`fixedColumnDef`のみ`.stg`は流用・`.txt`のみ専用。

## 未対応target

`settings` / `sql` / `xlsxTemplate` は `ScaffoldOption.execute()` にこれらを判定する分岐が存在せず、`-target`にこれらを指定しても何も出力されない。追加する場合は本ファイル冒頭の表と同じ観点（setting/template/dataset/parameter雛型それぞれ何が必要か）を設計してから実装する。

## テストの場所

- テストクラス: `core/src/test/java/yo/dbunitcli/application/command/ScaffoldTest.java`（`DdlTarget`等の`@Nested`クラスで target 別に整理）
- 入力フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/command/scaffold/src/SAMPLE.csv`
