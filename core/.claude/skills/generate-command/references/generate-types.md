# generateType 詳細（出典: tauri/public/help/generate.html）

## 一覧（設計4軸での対応表）

| generateType | unit | loadData / useJdbcMetaData | isFixedTemplate | 出力形状 | テンプレート/設定リソース |
|---|---|---|---|---|---|
| txt | record/table/dataset（可変・デフォルトrecord） | true / false（両方デフォルト） | false | 単一ファイル | （ユーザー指定`-template`） |
| xlsx / xls | 同上 | 同上 | false | 単一ファイル（`-lazyLoad`で都度ストリーム書き込みに切替可） | （ユーザー指定`-template`） |
| settings | dataset固定 | false / true | true | 単一ファイル | `settings/settingTemplate.stg,.txt` |
| sql | table固定 | true / true | true | テーブル毎（`<sqlFilePrefix><table><sqlFileSuffix>.sql`） | `sql/sqlTemplate.stg` + `sql/{insert,delete,update,cleanInsert,deleteInsert}Template.txt`（`-op`で分岐） |
| ddl | table固定 | true / true | true | テーブル毎（`<sqlFilePrefix><table><sqlFileSuffix>.sql`） | `sql/ddlTemplate.stg,.txt`、既定settings`sql/ddlSettings.json` |
| xlsxSchema | table固定 | false / false | true | テーブル毎（`<table>.json`） | `xlsxschema/xlsxSchemaTemplate.stg,.txt` |
| javaBean | table固定 | true / true | true | テーブル毎（`<Table（snakeToUpperCamel）>.java`） | `javabean/javaBeanTemplate.stg,.txt`、既定settings`javabean/javaBeanSettings.json` |
| fixedColumnDef | table固定 | false / false | true | テーブル毎（`<table>.json`） | `fixedcolumndef/fixedColumnDefTemplate.stg,.txt` |
| xlsxTemplate | dataset固定 | false / false | true | 単一ファイル・コード生成専用（テンプレート不要、`write()`が`JxlsTemplateGenerator`でExcelを直接組み立てる） | （なし） |

loadData/useJdbcMetaDataは`GenerateType`の`loadData()`/`useJdbcMetaData()`をオーバーライドして決まる（デフォルトは`loadData()=true`, `useJdbcMetaData()=false`）。`GenerateOption.dataSetParam()`はこの2メソッドを呼ぶだけで、switch分岐は持たない。

`xlsxSchema`/`fixedColumnDef`は追加で`wrapProducer(option, producer)`をoverrideし、`ComparableDataSetProducerWrapper`のサブクラス（`ComparableXlsxSchemaMetaDataProducer`/`ComparableFixedColumnDefMetaDataProducer`、`dataset/producer/`配下）でproducerをラップする。ラップされたproducerは`ParameterUnit.table.templateStream()`（`producer.lazyLoad(true)`経由）でのみ消費されるため、`unit=table`固定の2タイプ以外でoverrideしても反映されない点に注意。テーブルの実列メタデータから「列ごとに1行」を合成する変換（Excelセル位置計算・固定長定義生成など）が必要な場合はこのフックを使う。これら2つの`ComparableDataSetProducerWrapper`サブクラスはGenerate専用ではなく、`ScaffoldOption`（`scaffold-command`スキル参照）も`wrapProducer()`/`write()`を経由せず直接インスタンス化して、ダミーdataset生成の初期値算出に再利用している。

固定成果物タイプはすべて`-template`での差し替えを持たない（組み込みテンプレート専用）。`ddl`/`javaBean`相当の内容を組み込み以外のテンプレートで生成したい場合は、`generateType=txt`＋`unit=table`＋`unitSetting`（既定値は各`defaultSettingsPath()`と同じ設定ファイルを流用可）を使う。

`xlsxSchema`は`ddl`/`javaBean`/`fixedColumnDef`と同じくテーブル毎に1ファイル出力する（`GenerateType.xlsxSchema.resultPathTemplate()`が共有helper`tableFileResultPath()`経由で`<resultPath>/<tableName>.json`を自動生成）。`unit=table`なので`-unitSetting`でsrcデータセットの列を絞り込み/フィルタできる（`xlsxSchema`の`defaultSettingsPath()`は未定義なので既定は素通し）。

## Scaffoldとの結合

固定成果物型（`isFixedTemplate()=true`）の`getFixedUnit()`/`write()`/テンプレート形は、`Scaffold`コマンドが`-target`で同名targetを持つ場合に直接影響する（`ScaffoldOption`が同じ`.stg`/`.txt`を流用またはミラーするため）。これらを変更する際は`scaffold-command`スキルの`references/scaffold-targets.md`（専用ScaffoldTemplate要否の判定基準）を確認すること。

## unit

`ParameterUnit`（`application/ParameterUnit.java`）は record / table / dataset の3値。`GenerateOption.parameterStream()` が `generateType.isExcel()` と `lazyLoad` に応じて `dataSetToStream` / `lazyLoadStream` / `templateStream` を呼び分ける。`generateType.isFixedTemplate()` が true の場合、`unit` は `getFixedUnit()` に強制される（txt/xlsx/xlsのみユーザー指定可、デフォルトrecord）。

`-unitSetting` / `-unitSettingEncoding` は `unit=table` かつ非Excel系タイプで有効。`GenerateOption.unitTableSeparators()` が `FromJsonTableSeparatorsBuilder` でテーブル区切り設定を構築し、未指定時は `generateType.defaultSettingsPath()`（ddl/javaBeanのみ定義）のクラスパス既定値を使う。

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
| `src/main/resources/{typeName}/*.stg,*.txt,*.json` | テンプレート/設定リソース（同名ディレクトリの`{typeName}ScaffoldTemplate.*`は別コマンド`Scaffold`用、対象外） |
| `command/GenerateOptionTest.java` / `GenerateTest.java` | 単体（toParameters往復）/統合（`paramGenerate*.txt`+`expect/generate/**`）テスト |
| `ParameterUnit.java` | record/table/dataset のストリーム生成（unit挙動を変える場合のみ） |

## テストデータの場所

- 統合テストパラメータ: `core/src/test/resources/yo/dbunitcli/application/command/paramGenerate*.txt`
- 入力フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/src/generate/`
- unitSetting等の設定フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/settings/generate/`
- 期待値フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/expect/generate/`
