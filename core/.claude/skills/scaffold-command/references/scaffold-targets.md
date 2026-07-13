# scaffold target 詳細

## 実装チェックリスト（target追加/修正時の変更箇所）

`SKILL.md`の判断フローで設計を決めたら、以下のファイルに反映する。

| ファイル（`application/`配下） | 内容 |
|---|---|
| `command/ScaffoldDto.java` | `-target` の説明文（対応target一覧）を更新 |
| `command/ScaffoldOption.java` | `execute()` に新規target分岐を追加。ddl/javaBeanは`-template`指定時、組み込み`.stg/.txt`をそのままコピーし`.param`は`generateType=txt`＋`unitSetting`で組み込みと同内容を再現する（`writeGenericParamFile()`） |
| `src/main/resources/{typeName}/{typeName}ScaffoldTemplate.stg,.txt` | 組み込み`.stg/.txt`を無改造流用できない場合のみ必要な専用雛型（判定基準は本ファイル「専用ScaffoldTemplate要否の判定」節） |
| `command/ScaffoldTest.java` | targetごとの`@Nested`テストクラス。setting/unitSetting/template/parameterの単体・組み合わせ・カスタムファイル名パターン |

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

`hasDataset()`（`-dataset.srcType`かつ`-dataset.src`が両方指定）の場合のみダミーdataset書き出しが動く。実データそのものではなく、実データの列名（と一部の実メタデータ）から1テーブルにつき1行/カラムのダミー行を生成し、`-datasetType`（デフォルトcsv）の形式で`src/`配下に書き出す。列の形はtargetごとに異なる（各targetの`.stg`/`.txt`が消費する形に合わせてある）:

- `ddl`/`javaBean`（`writeDatasetSrcFiles()`）: `DDL_SCHEMA_COLUMNS`（COLUMN_NAME/TYPE_NAME/COLUMN_SIZE/DECIMAL_DIGITS/NULLABLE/IS_PK/PK_NAME/REMARKS/TABLE_REMARKS/TABLE_NAME/PACKAGE の11列）。値はすべて空/機械生成のダミー（`buildSchemaRow()`）
- `xlsxSchema`/`fixedColumnDef`（`writeWrappedDatasetSrcFiles()`）: `ComparableXlsxSchemaMetaDataProducer`/`ComparableFixedColumnDefMetaDataProducer`（`dataset/producer/`、Generate側`wrapProducer()`と共有— 詳細は次節）が実メタデータから直接算出した値。
  - `xlsxSchema`: COLUMN_NAME/SHEET_NAME/DATA_START/COLUMN_INDEX/CELL_ADDRESS/IS_PKの6列。SHEET_NAME/DATA_START/COLUMN_INDEX/CELL_ADDRESSは組み込みgenerateType=xlsxSchemaと同じ計算式（テーブル名/1/連番/POI CellReference計算値）。`IS_PK`は委譲先の`ITableMetaData.getPrimaryKeys()`由来の実値（実DB接続＋`-dataset.useJdbcMetaData=true`等でPKメタデータが取れれば`true`、取れなければ`false`）
  - `fixedColumnDef`: name/length/align/padの4列。`length`/`align`はScaffold側が渡す固定デフォルト値（10/left、`ScaffoldOption.FIXED_COLUMN_DEF_DEFAULT_LENGTH/ALIGN`）で、実カラムサイズ由来ではない（Generate側`-fixedLength`と同じ仕組みだが、Scaffoldは列ごとの上書きリストを渡さないため）

いずれもユーザーはこのダミーcsvの各行を編集してテンプレートの入力データとして使う想定で、値は「編集の出発点として合理的な初期値」であり最終値の保証ではない。

## 専用ScaffoldTemplate要否の判定

Scaffoldはtargetを`generateType=txt -unit=table`で駆動し、dataset(1行=1列)を`unitSetting`の`separate`でPK等のサブテーブル（`dataset.PK.rows`等）に分けてテンプレートに渡す。組み込みの`generateType.getStgPath()/getTemplatePath()`を無改造でコピーして使えるのは、次の2条件を両方満たす時のみ：

1. **組み込みマクロの引数がスカラーのみか**: 例えば`fixedColumnDef`の`columnEntry(col)`が使う`col.name/length/align/pad`はスカラーのみなので、Scaffoldの1行=1列datasetの各行がそのままマクロ引数になり`.stg`は無改造で流用できる。逆に旧`xlsxSchema`の`row.header`（`List<String>`）のようなリスト値フィールドを要求するマクロは、ST4のテンプレート構文だけでは組み立てられない（`[k:v]`のようなマップ/集約リテラルは実行時の式としては存在せず、`.stg`ファイル冒頭のdictionary宣言でしか使えない）。この場合は組み込み側の`write()`をリファクタリングし、`row.rows`（列毎の生行リスト）／`row.dataset.<name>.rows`（`separate`と同じ形の派生サブテーブル）のような、Scaffoldのdatasetがunit=tableで自然に提供する形へ寄せることで無改造流用に持ち込める（現`xlsxSchema`はこの形。列ごとの行合成自体は`GenerateType.xlsxSchema.wrapProducer()`が返す`ComparableXlsxSchemaMetaDataProducer`が担い、`write()`はPK/CELLS分割のみを行う薄いアダプタ）。
2. **固定unitが`table`か**: `isFixedTemplate()=true`で`getFixedUnit()`が`table`以外（`dataset`等）だと`-unitSetting`が一切効かない（`GenerateOption.parameterStream()`は`unit==table`の時のみ`unitTableSeparators()`を評価するため）。この場合、組み込み側の`getFixedUnit()`自体を`table`に変更できないか検討する（旧`xlsxSchema`は`dataset`固定だったが`table`固定に変更した。`resultPath()`のtypeName別自動命名分岐も要追従、`generate-command`スキル参照）。

両方を満たせない場合のみ`{typeName}ScaffoldTemplate.stg`および/または`.txt`を新設する。現状: `ddl`/`javaBean`/`xlsxSchema`はいずれも組み込みを完全流用（専用ファイルなし）、`fixedColumnDef`のみ`.stg`は流用・`.txt`のみ専用。

## setting雛型の再利用パターン: JSON駆動 vs write()駆動

上記2条件を満たしテンプレートが流用できても、「1行=1カラム」のdatasetを最終形（PK/CELLSサブテーブル等）へ変換するロジックの所在によって、setting雛型の作り方・実装コストが変わる。

- **`defaultSettingsPath()`の宣言的JSON（ddl/javaBean）**: `sql/ddlSettings.json`・`javabean/javaBeanSettings.json`は、列名キーの`separate`/`string`/`boolean`変換ルールだけで構成され、値が実DBメタデータ由来でもScaffoldのダミー空値でも同じルールで変換できる。そのため`copySettingResource()`で組み込みJSONをそのまま`-unitSetting`としてコピーするだけで済み、Scaffold側にJava変換コードは要らない
- **`wrapProducer()`側のJavaコード（xlsxSchema/fixedColumnDef）**: `defaultSettingsPath()`を持たず、`GenerateType.xxx.wrapProducer()`が返す`ComparableDataSetProducerWrapper`サブクラス（`ComparableXlsxSchemaMetaDataProducer`/`ComparableFixedColumnDefMetaDataProducer`）が変換を担う。`generateType=txt`は`wrapProducer()`/`write()`を経由しないため、Scaffold側は同じロジックを複製する代わりに、これらの producer クラスを`writeWrappedDatasetSrcFiles()`から直接インスタンス化して再利用している（`ScaffoldOption.sourceProducer()`で実producerを取得し、`new ComparableXlsxSchemaMetaDataProducer(sourceProducer)`のように包む）。専用のサンプルunitSetting（`xlsxSchemaSettings.json`等、`defaultSettingsPath()`とは別物）は引き続き必要

新target追加時、対応するGenerateTypeが`defaultSettingsPath()`を持つなら実装コストは低い（JSONコピーのみ）。`wrapProducer()`側にロジックがあるなら、そのproducerクラスをScaffold側からも直接インスタンス化できないか検討する（`write()`内に埋め込まれたJavaコードとして複製する必要はない）。

## 未対応target・非対応の理由

`settings` / `sql` / `xlsxTemplate` は `ScaffoldOption.execute()` にこれらを判定する分岐が存在せず、`-target`にこれらを指定しても何も出力されない。理由はtargetごとに異なる:

- `settings` / `xlsxTemplate`: `getFixedUnit()`が`dataset`固定（上記条件2を満たさない）。table固定への変更が前提（`xlsxSchema`の前例を参照）
- `sql`: `getFixedUnit()`は`table`だが、テンプレート（`sqlTemplate.stg`の`insert()`/`update()`/`delete()`にある`value(row,column)`マクロ）が`rows`を「テーブル定義」ではなく「対象テーブルの実データレコード」として読み、`row.(column.columnName)`の値をそのままSQLへ埋め込む。Scaffoldが作れるのは列名だけのダミー行（値は空・機械生成、列定義1件=1行）なので、`-unitSetting`分割を通しても実データが必要な箇所は空のままで意味のあるSQLにならない。ddl/javaBean/xlsxSchema/fixedColumnDefが`rows`を「列定義の一覧」として読む（例: `ddlTemplate.stg`の`$row.COLUMN_NAME$`/`$row.TYPE_NAME$`）のとは対照的（`GenerateOption.dataSetParam()`の`loadData`/`useJdbcMetaData`はddl/javaBean/sqlで同一設定であり、この違いを生む要因ではない点に注意）

追加する場合は本ファイル冒頭の表と同じ観点（setting/template/dataset/parameter雛型それぞれ何が必要か）に加え、上記の非対応理由を解消できるかを設計してから実装する。

## テストの場所

- テストクラス: `core/src/test/java/yo/dbunitcli/application/command/ScaffoldTest.java`（`DdlTarget`等の`@Nested`クラスで target 別に整理）
- 入力フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/command/scaffold/src/SAMPLE.csv`
