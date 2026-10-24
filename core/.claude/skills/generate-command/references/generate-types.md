# generateType 詳細（出典: tauri/public/help/commands/generate.html）

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
| xlsxTemplate | dataset固定 | false / false | true | 単一ファイル・コード生成専用（テンプレート不要、`writer()`が`JxlsTemplateGenerator`でExcelを直接組み立てる） | （なし） |

loadData/useJdbcMetaDataは`GenerateType`の`loadData()`/`useJdbcMetaData()`をオーバーライドして決まる（デフォルトは`loadData()=true`, `useJdbcMetaData()=false`）。`GenerateOption.dataSetParam()`はこの2メソッドを呼ぶ他、`defaultSettingsPath()`のロード時適用を`loadData()=true`型に限定する（wrapProducer型に適用すると変換ルールが合成前の元ソース列に評価され記述子を汚染するため）。

`ddl`/`javaBean`/`xlsxSchema`/`fixedColumnDef`は追加で`wrapProducer(option, producer)`をoverrideし、`ComparableDataSetProducerWrapper`のサブクラス（`ComparableDdlMetaDataProducer`/`ComparableJdbcMetaDataProducer`/`ComparableXlsxSchemaMetaDataProducer`/`ComparableFixedColumnDefMetaDataProducer`、`dataset/producer/`配下）でproducerをラップする。元ソースの`ITableMetaData`から「列ごとに1行」の記述子行を合成する型で、入力は記述子CSVではなく任意のデータソース。`ddl`/`javaBean`は`ComparableDdlMetaDataProducer.forSource()`でソースにより切り替え、DBソース（`srcType=table`）では`ComparableJdbcMetaDataProducer`が`DatabaseMetaData`からCOLUMN_SIZE/DECIMAL_DIGITS/REMARKS/TABLE_REMARKS/PK_NAMEの実値を補完する（両producerの出力スキーマは同一）。非DBソースは`ComparableDdlMetaDataProducer`が合成し、JDBC＋`useJdbcMetaData`で型・PK・NULL制約の実値が入り、CSV等では列名のみ（dbunitメタデータに無い値は空）。記述子データを人手で補って生成したい場合は`generateType=txt -unit=table`＋unitSettingを使う（Scaffoldのtemplate駆動`.param`がその形）。ラップされたproducerは`GenerateOption.forEachParameter()`の非Excel・`unit=table`経路（`ParameterUnit.tableTemplates()`→`producer.lazyLoad(true)`）でのみ消費されるため、`unit=table`固定タイプ以外でoverrideしても反映されない点に注意。サブクラスはコンストラクタで出力スキーマを渡し、`convertTable(metaData, conversion)`で`conversion.writeColumnRows()`を呼ぶ。ラッパーはテーブル一覧の作成（`LazyloadTaskMapper`がloadData=falseで`with()`したタスク。タスクの`param().loadData()`で表す）では変換せず、テーブル名と出力スキーマだけを返し、変換（DB入力ではメタデータ照会）はテーブルごとの出力で1回だけ行う（ソースごとに結果を保持）。並列化は包んだproducerの`isParallelizableSource()`に従い、作業スレッドでは`forWorker`を包んだタスクへ伝え、`ComparableJdbcMetaDataProducer`は`conversion.workerResources()`から作業スレッド専用の接続で照会する。これらの`ComparableDataSetProducerWrapper`サブクラスはGenerate専用ではなく、`ScaffoldTarget`（`scaffold-command`スキル参照）も`wrapProducer()`を経由せず直接インスタンス化して、記述子dataset雛型の初期値算出に再利用している。

固定成果物タイプはすべて`-template`での差し替えを持たない（組み込みテンプレート専用）。`ddl`/`javaBean`相当の内容を組み込み以外のテンプレートで生成したい場合は、`generateType=txt`＋`unit=table`＋`unitSetting`（既定値は各`defaultSettingsPath()`と同じ設定ファイルを流用可）を使う。

`xlsxSchema`は`ddl`/`javaBean`/`fixedColumnDef`と同じくテーブル毎に1ファイル出力する（`GenerateType.xlsxSchema.resultPathTemplate()`が共有helper`tableFileResultPath()`経由で`<resultPath>/<tableName>.json`を自動生成）。既定unitSetting（`xlsxschema/xlsxSchemaSettings.json`）のseparateルールがPK/CELLSサブテーブルを合成し、テンプレートの`subTable.PK.rows`/`subTable.CELLS.rows`参照はこれが前提（ddlの`sql/ddlSettings.json`と同じ方式。Scaffoldがコピーする雛型とも同一ファイル）。`-unitSetting`を明示指定すると既定が丸ごと置き換わるため、PK/CELLS形状を保つにはseparateルールを含めること。

## Scaffoldとの結合

固定成果物型（`isFixedTemplate()=true`）の`getFixedUnit()`/`wrapProducer()`/テンプレート形は、`Scaffold`コマンドが`-target`で同名targetを持つ場合に直接影響する（`ScaffoldTarget`が同じ`.stg`/`.txt`をbyte-for-byteコピーし、同じproducerサブクラスを直接インスタンス化するため）。テンプレートはunit=tableの自然な形（`rows`/`tableName`/`subTable.<name>.rows`）だけを読むよう保ち、末尾改行を付けないこと（classpath読込は末尾改行を落とすがファイル読込は保持するため、両駆動のバイト一致が崩れる）。変更時は`scaffold-command`スキルの`references/scaffold-targets.md`と`ScaffoldTest.ScaffoldToGenerate`のバイト一致検証を確認すること。

## unit

`ParameterUnit`（`application/ParameterUnit.java`）は record / table / dataset の3値。`Generate.exec()`は`GenerateOption.forEachParameter()`で出力単位の出力先とパラメータを受け取る。dataset（とsrcのないrecord）は`parameterStream()`が `generateType.isExcel()` と `lazyLoad` に応じて `dataSetToStream` / `lazyLoadStream` を呼び分ける。recordは`RecordConverter`をsrcのconverterにして読込中の行をその場で出力する（データセット全体を保持しない。includeMetaDataはテーブル単位で溜めてendDataSetで出す）。パラメータはinputParam・tableName・rowNumber（テーブルごとの0始まり）と列の値。srcの読込はレーンごとに並列、描画・書込は並列数が2以上なら`ParallelTableMapping.KeyedWorkers`（出力先ごとに同じ作業スレッド・上限付きの待ち行列）で並列にする。別レーンから同じ出力先へ書く場合の順は不定（制限事項）。srcType=noneとdatasetは`parameterStream()`で全件読込後に出力する。tableは`ParameterUnit.TableTemplate`（テキスト=`tableTemplates`、Excel lazyLoad=`streamingTableTemplates`、Excel全件ロード=`loadedTableTemplates`）を列挙し、並列化できればテーブルごとに`ParallelTableMapping.forEach`で別スレッド出力する（読込タスクまたは出力先を共有するテーブルは`groupByShared`で同じレーンにまとめて元の順に処理。出力先は行を読む前のパラメータで1回だけ描画する。詳細は`forEachParameter()`のjavadoc）。テンプレートは`GenerateType.writer()`で1回だけ読込・コンパイルし、出力ごとに使い回す（戻り値の`Writer`は複数スレッドから呼ばれる）。`generateType.isFixedTemplate()` が true の場合、`unit` は `getFixedUnit()` に強制される（txt/xlsx/xlsのみユーザー指定可、デフォルトrecord）。

`-unitSetting` / `-unitSettingEncoding` は `unit=table` かつ非Excel系・非固定成果物タイプ（`isFixedTemplate()==false`）のみ有効。固定成果物ではコンストラクタで`unitSetting`を無視し（フォームにも出ない）、`unitTableSeparators()`は組み込み設定（`defaultSettingsPath()`）を使う。`unit=table`の行解決（`ComparableTableMappingTask.WithTargetTable#resolveRows/resolveDataSet`）は、反復単位（`lazyLoad`）をsrcの`-setting`だけで決めた後、`param().tableSeparators().then(unitTableSeparators(), 対象テーブル名)`を1回の走査で適用する。`TableSeparators#getAddSettingTableMetaData`は自段の結果のうち`stageTableName`のものだけに`next`の`addSetting`を`flatMap`で連鎖させ、結果は`AddSettingTableMetaData#preset`に前段を持つ（反復単位のテーブル(`stageTableName`)の系統だけを後段へ流すので、`dataset`にはsrcで分けた兄弟テーブルが入らない。別名を付けたかどうかは関係しない。同名テーブルの合流による行幅ずれも副次的に防ぐ）。srcの`string/number/boolean/sqlFunction`・`filter`・`order`・`distinct`・サブテーブルは`unit=table`でもそのまま参照でき、unitSettingは「srcの適用後の状態」に対する列追加・削除・絞り込み・`dataset`サブテーブル追加に使う。`lazyLoad`はnextを見ないため、2段目の`separate`は別ファイルにならず`dataset`に入る。連鎖実行するタスク（`ComparableTableMapperSingle`のchain）にもnextを引き継ぐ。srcの`split`は、`suffix`/`prefix`（例: `"suffix": "_%d"`）で分割後のテーブル名を分ければ`unit=table`でも使え、分割後のテーブル（`T_0`/`T_1`）がそれぞれ反復単位になりunitSettingが各テーブルに適用される（前段のsplitは後段がsplitしない限り`AddSettingTableMetaData#getTableSplitter`が引き継ぐ）。ただしsplitするテーブルは分割後の名前が実行時に決まるため絞り込みの対象外で、`dataset`には分割された兄弟テーブルも含まれる。名前を分けない`split`（全て同名）は行が1つのテーブルにまとまる。したがってsrcの`string/number/boolean/sqlFunction`・`filter`・サブテーブルは`unit=table`でもそのまま参照でき、unitSettingは「srcの適用後の状態」に対する列追加・削除・絞り込み・`dataset`サブテーブル追加に使う。

## オプションの所在

| オプション | `GenerateDto` フィールド | `GenerateOption` での用途 |
|---|---|---|
| `template` / `result` / `resultPath` / `outputEncoding` | 共通 | 全タイプ共通（`GenerateType.resultPathTemplate()`をsql/ddl/fixedColumnDef/xlsxSchema/javaBeanがoverrideし拡張子・命名を分岐） |
| `commit` / `op` / `sqlFilePrefix` / `sqlFileSuffix` | sql/ddl用 | `GenerateType.sql/ddl` の `toParametersBuilder()` 分岐 |
| `includeAllColumns` | settings用 | `dataSetParam()` で `ComparableDataSetParam.Builder` に反映 |
| `lazyLoad` | xlsx/xls用 | `parameterStream()` / `forEachParameter()` の分岐 |
| `parallel` | srcが並列読込できる形式か、unit=recordのとき表示（srcType=noneを除く） | `dataSetParam()`で`setParallel`に渡し、srcの並列読込と`unit=record`・`unit=table`の並列出力の並列数になる（値の検証・解釈は`DataSetConverterOption.parallel(String)`/`parallelism(String)`を共用） |
| `fixedLength` / `defaultLength` / `align` | fixedColumnDef用 | `GenerateType.fixedColumnDef.wrapProducer()` が `ComparableFixedColumnDefMetaDataProducer` に渡す |

## 実装チェックリスト（generateType追加/修正時の変更箇所）

上記4軸で設計を決めたら、以下のファイルに反映する。

| ファイル（`application/`配下） | 内容 |
|---|---|
| `command/GenerateType.java` | enum定数追加。4軸に対応する`loadData()`/`useJdbcMetaData()`/`isFixedTemplate()`+`getFixedUnit()`/`resultPathTemplate()`と、`writer()`/`defaultSettingsPath()`/`getTemplateString()` をoverride（`writer()`はテンプレート準備を1回だけ行い、出力処理を返す。戻り値は複数スレッドから呼ばれうる）。固定成果物タイプに`-template`での差し替えは持たせない。`unit=table`で列ごとの行変換が要る場合は`wrapProducer()`もoverride |
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
