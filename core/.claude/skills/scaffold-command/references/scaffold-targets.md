# scaffold target 詳細

## 実装チェックリスト（target追加/修正時の変更箇所）

`SKILL.md`の判断基準で設計を決めたら、以下のファイルに反映する。

| ファイル（`application/`配下） | 内容 |
|---|---|
| `command/ScaffoldTarget.java` | enum定数追加。フック: `sampleUnitSettingPath()`（サンプルunitSettingのclasspathパス）/`datasetSchema()`（記述子列定義）/`wrapProducer()`（記述子dataset合成producer）/`customTemplateResultPath()`（txt駆動時の`-resultPath`）/`putBuiltinExtraParams()`（builtin駆動時のtarget固有オプション、必要な場合のみ） |
| `command/ScaffoldDto.java` | `-target`の説明文（対応target一覧）を更新。target固有オプションがあれば追加 |
| `command/ScaffoldOption.java` | 通常変更不要（`executeTarget()`は4target共通フロー）。target固有オプションを追加する場合のみrecordフィールド追加 |
| `dataset/producer/Comparable{TypeName}MetaDataProducer.java` | `GenerateType.wrapProducer()`と共有する記述子行合成producer（`ComparableDataSetProducerWrapper`継承、`outputSchema()`静的メソッド、親の`writeColumnRows()`利用） |
| `src/main/resources/{typeName}/{typeName}Settings.json` | `defaultSettingsPath()`を持たないtargetのみ必要なScaffold専用サンプルunitSetting |
| `command/ScaffoldTest.java` | targetごとの`@Nested`テストクラス＋`ScaffoldToGenerate`のE2E（builtin駆動とtxt駆動のバイト一致検証を含める） |

## 対応target

| target | 対応する`GenerateType` | サンプルunitSetting | 記述子dataset用producer | 記述子列 | txt駆動時の`-resultPath` |
|---|---|---|---|---|---|
| `ddl` | `GenerateType.ddl` | `sql/ddlSettings.json`（=`defaultSettingsPath()`） | `ComparableDdlMetaDataProducer` | 11列（COLUMN_NAME/TYPE_NAME/COLUMN_SIZE/DECIMAL_DIGITS/NULLABLE/IS_PK/PK_NAME/REMARKS/TABLE_REMARKS/TABLE_NAME/PACKAGE） | `$param.tableName$.sql` |
| `javaBean` | `GenerateType.javaBean` | `javabean/javaBeanSettings.json`（同上） | 同上 | 同上 | `$param.tableName; format="snakeToUpperCamel"$.java` |
| `xlsxSchema` | `GenerateType.xlsxSchema` | `xlsxschema/xlsxSchemaSettings.json`（=`defaultSettingsPath()`。PK/CELLSへのseparateルール） | `ComparableXlsxSchemaMetaDataProducer` | 6列（COLUMN_NAME/SHEET_NAME/DATA_START/COLUMN_INDEX/CELL_ADDRESS/IS_PK） | `$param.tableName$.json` |
| `fixedColumnDef` | `GenerateType.fixedColumnDef` | `fixedcolumndef/fixedColumnDefSettings.json`（空のプレースホルダー） | `ComparableFixedColumnDefMetaDataProducer`（`-fixedLength`/`-defaultLength`/`-align`で値を変更可、デフォルト10/left） | 4列（name/length/align/pad） | `$param.tableName$.json` |
| `parameter` | なし（`-commandType`必須） | — | — | — | — |

補足:
- 出力先はunitSetting=`resources/setting/`、template=`resources/template/`、parameter=`option/`、dataset=`src/`。`.param`内の相対パスはこの配置を前提に書き出されるため、ディレクトリ構成を変える場合は`.param`生成側も追従が必要
- `parameter` targetは`CommandParameters(Type, commandInput).shrink()`で任意コマンドの`.param`を生成。出力ファイル名は`{commandType}.param`固定で`-parameter`の名前は使われない（他targetは`-parameter`の名前で出力）
- `-datasetType`（記述子datasetの出力形式、デフォルトcsv）に`format`は指定不可（読み戻し先の`DataSourceType`が存在しないためコンストラクタで即エラー）

## dataset雛型（記述子dataset）の中身

`hasDataset()`（`-dataset.srcType`かつ`-dataset.src`が両方指定）の場合のみ書き出される。全targetが上表のproducer（`GenerateType.wrapProducer()`と共有）を直接インスタンス化し、元ソースの`ITableMetaData`から「1テーブルにつき1行/カラム」の記述子行を合成して`-datasetType`形式で`src/`配下に書き出す。

- 実メタデータの取れるソースでは実値が入る: TYPE_NAME/NULLABLE/IS_PK（ddl/javaBean、JDBC＋`-dataset.useJdbcMetaData=true`等）、SHEET_NAME/DATA_START/COLUMN_INDEX/CELL_ADDRESS/IS_PK（xlsxSchema、位置系は計算値）
- dbunitメタデータに存在しない値は常に空: COLUMN_SIZE/DECIMAL_DIGITS/PK_NAME/REMARKS/TABLE_REMARKS/PACKAGE
- fixedColumnDefのlength/alignはScaffoldの`-fixedLength`/`-defaultLength`/`-align`由来（実カラムサイズ由来ではない）

いずれもユーザーがこの記述子csvの各行を編集してtxt駆動テンプレートの入力データとして使う想定で、値は「編集の出発点として合理的な初期値」であり最終値の保証ではない。

## builtin駆動とtxt駆動の関係

- **builtin駆動**（`.param`の`-generateType=<target>`）: 元ソースを`-src`に取り、`GenerateType.wrapProducer()`が生成時に記述子行を合成する。メタデータに無い値（カラムサイズ・コメント等）は空のまま
- **txt駆動**（`.param`の`generateType=txt -unit=table`）: scaffoldが書き出した記述子dataset（編集済み）を`-src`に取り、コピーした組み込みテンプレート＋unitSettingで生成する。メタデータに無い値を人手で補える
- 同じ元ソース・未編集の記述子datasetなら両駆動の出力はバイト一致する（`ScaffoldTest.ScaffoldToGenerate.assertScaffoldWithTemplateMatchesBuiltIn`が4target全てで検証）

この一致が成り立つ前提: 組み込みテンプレートがunit=tableの自然な形（`rows`/`tableName`/`dataset.<name>.rows`）だけを読むこと。前例: xlsxSchemaはテンプレートを`dataSet.values`読みへ、fixedColumnDefは`$columns$`→`$rows$`（`write()`のキー改名を削除）へリファクタして達成した。組み込み`.txt`の末尾改行にも注意（classpath読込（builtin）は末尾改行を落とすがファイル読込（txt駆動）は保持するため、テンプレートは末尾改行なしで統一）。

## 未対応target・非対応の理由

`settings` / `sql` / `xlsxTemplate` は`ScaffoldTarget`に定数が無く、`-target`に指定しても何も出力されない:

- `settings` / `xlsxTemplate`: `getFixedUnit()`が`dataset`固定（`-unitSetting`分割が効かない）。table固定への変更が前提
- `sql`: `getFixedUnit()`は`table`だが、テンプレート（`sqlTemplate.stg`の`value(row,column)`マクロ）が`rows`を「対象テーブルの実データレコード」として読み値をSQLへ埋め込む。記述子行（列定義1件=1行）を渡しても意味のあるSQLにならない

## テストの場所

- テストクラス: `core/src/test/java/yo/dbunitcli/application/command/ScaffoldTest.java`（target別の`@Nested`クラス。`ScaffoldToGenerate`がE2E: scaffold出力の`.param`を`Generate.main()`に渡して検証、builtin/txt両駆動のバイト一致を4target全てで検証）
- producer単体テスト: `core/src/test/java/yo/dbunitcli/dataset/producer/ComparableDdlMetaDataProducerTest.java`等
- 入力フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/command/scaffold/src/SAMPLE.csv`
