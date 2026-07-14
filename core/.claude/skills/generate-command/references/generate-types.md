# generateType 詳細（出典: tauri/public/help/generate.html）

## 一覧（設計4軸での対応表）

| generateType | unit | loadData / useJdbcMetaData | isFixedTemplate | 出力形状 | テンプレート/設定リソース |
|---|---|---|---|---|---|
| txt | record/table/dataset（可変・デフォルトrecord） | true / false（両方デフォルト） | false | 単一ファイル | （ユーザー指定`-template`） |
| xlsx / xls | 同上 | 同上 | false | 単一ファイル（`-lazyLoad`で都度ストリーム書き込みに切替可） | （ユーザー指定`-template`） |
| settings | dataset固定 | false / true | true | 単一ファイル | `settings/settingTemplate.stg,.txt` |
| sql | table固定 | true / true | true | テーブル毎（`<sqlFilePrefix><table><sqlFileSuffix>.sql`） | `sql/sqlTemplate.stg` + `sql/{insert,delete,update,cleanInsert,deleteInsert}Template.txt`（`-op`で分岐） |
| ddl | table固定 | false / true | true | テーブル毎（`<sqlFilePrefix><table><sqlFileSuffix>.sql`） | `sql/ddlTemplate.stg,.txt`、既定settings`sql/ddlSettings.json` |
| xlsxSchema | table固定 | false / false | true | テーブル毎（`<table>.json`） | `xlsxschema/xlsxSchemaTemplate.stg,.txt`、既定settings`xlsxschema/xlsxSchemaSettings.json` |
| javaBean | table固定 | false / true | true | テーブル毎（`<Table（snakeToUpperCamel）>.java`） | `javabean/javaBeanTemplate.stg,.txt`、既定settings`javabean/javaBeanSettings.json` |
| fixedColumnDef | table固定 | false / false | true | テーブル毎（`<table>.json`） | `fixedcolumndef/fixedColumnDefTemplate.stg,.txt` |
| xlsxTemplate | dataset固定 | false / false | true | 単一ファイル・コード生成専用（テンプレート不要、`write()`が`JxlsTemplateGenerator`でExcelを直接組み立てる） | （なし） |

loadData/useJdbcMetaDataは`GenerateType`の`loadData()`/`useJdbcMetaData()`をオーバーライドして決まる（デフォルトは`loadData()=true`, `useJdbcMetaData()=false`）。`GenerateOption.dataSetParam()`はこの2メソッドを呼ぶ他、`defaultSettingsPath()`のロード時適用を`loadData()=true`型に限定する（wrapProducer型に適用すると変換ルールが合成前の元ソース列に評価され記述子を汚染するため）。

`ddl`/`javaBean`/`xlsxSchema`/`fixedColumnDef`は追加で`wrapProducer(option, producer)`をoverrideし、`ComparableDataSetProducerWrapper`のサブクラス（`ComparableDdlMetaDataProducer`/`ComparableJdbcMetaDataProducer`/`ComparableXlsxSchemaMetaDataProducer`/`ComparableFixedColumnDefMetaDataProducer`、`dataset/producer/`配下）でproducerをラップする。元ソースの`ITableMetaData`から「列ごとに1行」の記述子行を合成する型で、入力は記述子CSVではなく任意のデータソース。`ddl`/`javaBean`は`ComparableDdlMetaDataProducer.forSource()`でソースにより切り替え、DBソース（`srcType=table`）では`ComparableJdbcMetaDataProducer`が`DatabaseMetaData`からCOLUMN_SIZE/DECIMAL_DIGITS/REMARKS/TABLE_REMARKS/PK_NAMEの実値を補完する（両producerの出力スキーマは同一）。非DBソースは`ComparableDdlMetaDataProducer`が合成し、JDBC＋`useJdbcMetaData`で型・PK・NULL制約の実値が入り、CSV等では列名のみ（dbunitメタデータに無い値は空）。記述子データを人手で補って生成したい場合は`generateType=txt -unit=table`＋unitSettingを使う（Scaffoldのtemplate駆動`.param`がその形）。ラップされたproducerは`ParameterUnit.table.templateStream()`（`producer.lazyLoad(true)`経由）でのみ消費されるため、`unit=table`固定タイプ以外でoverrideしても反映されない点に注意。これらの`ComparableDataSetProducerWrapper`サブクラスはGenerate専用ではなく、`ScaffoldTarget`（`scaffold-command`スキル参照）も`wrapProducer()`を経由せず直接インスタンス化して、記述子dataset雛型の初期値算出に再利用している。

固定成果物タイプはすべて`-template`での差し替えを持たない（組み込みテンプレート専用）。`ddl`/`javaBean`相当の内容を組み込み以外のテンプレートで生成したい場合は、`generateType=txt`＋`unit=table`＋`unitSetting`（既定値は各`defaultSettingsPath()`と同じ設定ファイルを流用可）を使う。

`xlsxSchema`は`ddl`/`javaBean`/`fixedColumnDef`と同じくテーブル毎に1ファイル出力する（`GenerateType.xlsxSchema.resultPathTemplate()`が共有helper`tableFileResultPath()`経由で`<resultPath>/<tableName>.json`を自動生成）。既定unitSetting（`xlsxschema/xlsxSchemaSettings.json`）のseparateルールがPK/CELLSサブテーブルを合成し、テンプレートの`dataset.PK.rows`/`dataset.CELLS.rows`参照はこれが前提（ddlの`sql/ddlSettings.json`と同じ方式。Scaffoldがコピーする雛型とも同一ファイル）。`-unitSetting`を明示指定すると既定が丸ごと置き換わるため、PK/CELLS形状を保つにはseparateルールを含めること。

## Scaffoldとの結合

固定成果物型（`isFixedTemplate()=true`）の`getFixedUnit()`/`wrapProducer()`/テンプレート形は、`Scaffold`コマンドが`-target`で同名targetを持つ場合に直接影響する（`ScaffoldTarget`が同じ`.stg`/`.txt`をbyte-for-byteコピーし、同じproducerサブクラスを直接インスタンス化するため）。テンプレートはunit=tableの自然な形（`rows`/`tableName`/`dataset.<name>.rows`）だけを読むよう保ち、末尾改行を付けないこと（classpath読込は末尾改行を落とすがファイル読込は保持するため、両駆動のバイト一致が崩れる）。変更時は`scaffold-command`スキルの`references/scaffold-targets.md`と`ScaffoldTest.ScaffoldToGenerate`のバイト一致検証を確認すること。

## unit

`ParameterUnit`（`application/ParameterUnit.java`）は record / table / dataset の3値。`GenerateOption.parameterStream()` が `generateType.isExcel()` と `lazyLoad` に応じて `dataSetToStream` / `lazyLoadStream` / `templateStream` を呼び分ける。`generateType.isFixedTemplate()` が true の場合、`unit` は `getFixedUnit()` に強制される（txt/xlsx/xlsのみユーザー指定可、デフォルトrecord）。

`-unitSetting` / `-unitSettingEncoding` は `unit=table` かつ非Excel系タイプで有効。`GenerateOption.unitTableSeparators()` が `FromJsonTableSeparatorsBuilder` でテーブル区切り設定を構築し、未指定時は `generateType.defaultSettingsPath()`（ddl/javaBean/xlsxSchemaで定義）のクラスパス既定値を使う。

## オプションの所在

| オプション | `GenerateDto` フィールド | `GenerateOption` での用途 |
|---|---|---|
| `template` / `result` / `resultPath` / `outputEncoding` | 共通 | 全タイプ共通（`GenerateType.resultPathTemplate()`をsql/ddl/fixedColumnDef/xlsxSchema/javaBeanがoverrideし拡張子・命名を分岐） |
| `commit` / `op` / `sqlFilePrefix` / `sqlFileSuffix` | sql/ddl用 | `GenerateType.sql/ddl` の `toParametersBuilder()` 分岐 |
| `includeAllColumns` | settings用 | `dataSetParam()` で `ComparableDataSetParam.Builder` に反映 |
| `lazyLoad` | xlsx/xls用 | `parameterStream()` の分岐 |
| `fixedLength` / `defaultLength` / `align` | fixedColumnDef用 | `GenerateType.fixedColumnDef.wrapProducer()` が `ComparableFixedColumnDefMetaDataProducer` に渡す |

## 実装チェックリスト（generateType追加/修正時の変更箇所）

上記4軸で設計を決めたら、以下のファイルに反映する。

| ファイル（`application/`配下） | 内容 |
|---|---|
| `command/GenerateType.java` | enum定数追加。4軸に対応する`loadData()`/`useJdbcMetaData()`/`isFixedTemplate()`+`getFixedUnit()`/`resultPathTemplate()`と、`write()`/`defaultSettingsPath()`/`getTemplateString()` をoverride。固定成果物タイプに`-template`での差し替えは持たせない。`unit=table`で列ごとの行変換が要る場合は`wrapProducer()`もoverride |
| `command/GenerateDto.java` | `@CommandLine.Option` フィールド追加（getter/setter） |
| `command/GenerateOption.java` | recordフィールド追加。コンストラクタ/`toParametersBuilder()`に反映（`dataSetParam()`/`resultPath(Parameter)`は`GenerateType`の軸1/軸4メソッドを呼ぶだけなので、通常はここを直接いじらない） |
| `src/main/resources/{typeName}/*.stg,*.txt,*.json` | テンプレート/設定リソース（Scaffoldがbyte-for-byteコピーして流用するため、変更時はScaffold側のバイト一致検証も確認） |
| `command/GenerateOptionTest.java` / `GenerateTest.java` | 単体（toParameters往復）/統合（`paramGenerate*.txt`+`expect/generate/**`）テスト |
| `ParameterUnit.java` | record/table/dataset のストリーム生成（unit挙動を変える場合のみ） |

## テストデータの場所

- 統合テストパラメータ: `core/src/test/resources/yo/dbunitcli/application/command/paramGenerate*.txt`
- 入力フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/src/generate/`
- unitSetting等の設定フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/settings/generate/`
- 期待値フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/expect/generate/`
