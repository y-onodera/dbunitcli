# generateType 詳細（出典: tauri/public/help/generate.html）

## 一覧

| generateType | 分類 | unit（固定/デフォルト） | `GenerateType`内の主なoverride | テンプレート/設定リソース |
|---|---|---|---|---|
| txt | テンプレート指定 | record/table/dataset（可変・デフォルトrecord） | `getTemplateString()` | （ユーザー指定 `-template`） |
| xlsx / xls | テンプレート指定 | 同上 | `write()`（jxls、`JxlsTemplateRender`） | （ユーザー指定 `-template`） |
| settings | 固定成果物 | dataset固定 | `isFixedTemplate`, `getFixedUnit` | `settings/settingTemplate.stg,.txt` |
| sql | 固定成果物 | table固定 | 上記 + `getTemplateString()`（`-op`別に分岐） | `sql/sqlTemplate.stg` + `sql/{insert,delete,update,cleanInsert,deleteInsert}Template.txt` |
| ddl | 固定成果物 | table固定 | 上記 + `defaultSettingsPath` | `sql/ddlTemplate.stg,.txt`, 既定settings `sql/ddlSettings.json` |
| xlsxSchema | 固定成果物 | table固定 | 上記 + `write()`（テーブル毎に`dataSet`をrows/dataset.PK/dataset.CELLS形へ変換） | `xlsxschema/xlsxSchemaTemplate.stg,.txt` |
| javaBean | 固定成果物 | table固定 | ddlと同様（`defaultSettingsPath`） | `javabean/javaBeanTemplate.stg,.txt`, 既定settings `javabean/javaBeanSettings.json` |
| fixedColumnDef | 固定成果物 | table固定 | 上記 + `write()`（`FixedColumnDef`リスト生成） | `fixedcolumndef/fixedColumnDefTemplate.stg,.txt` |
| xlsxTemplate | 固定成果物 | dataset固定 | 上記 + `write()`（`JxlsTemplateGenerator.createTemplate`） | （なし、コード生成のみ） |

固定成果物タイプはすべて`-template`での差し替えを持たない（組み込みテンプレート専用）。`ddl`/`javaBean`相当の内容を組み込み以外のテンプレートで生成したい場合は、`generateType=txt`＋`unit=table`＋`unitSetting`（既定値は各`defaultSettingsPath()`と同じ設定ファイルを流用可）を使う。

`xlsxSchema`は`ddl`/`javaBean`/`fixedColumnDef`と同じくテーブル毎に1ファイル出力する（`GenerateOption.resultPath()`が`<resultPath>/<tableName>.json`を自動生成）。`unit=table`なので`-unitSetting`でsrcデータセットの列を絞り込み/フィルタできる（`xlsxSchema`の`defaultSettingsPath()`は未定義なので既定は素通し）。Scaffoldの`xlsxSchema`ターゲットは`GenerateType.xlsxSchema.getStgPath()/getTemplatePath()`（`xlsxschema/xlsxSchemaTemplate.stg,.txt`）を無改造でコピーして再利用しており、Scaffold専用の`*ScaffoldTemplate.*`は存在しない。

## unit

`ParameterUnit`（`application/ParameterUnit.java`）は record / table / dataset の3値。`GenerateOption.parameterStream()` が `generateType.isExcel()` と `lazyLoad` に応じて `dataSetToStream` / `lazyLoadStream` / `templateStream` を呼び分ける。`generateType.isFixedTemplate()` が true の場合、`unit` は `getFixedUnit()` に強制される（txt/xlsx/xlsのみユーザー指定可、デフォルトrecord）。

`-unitSetting` / `-unitSettingEncoding` は `unit=table` かつ非Excel系タイプで有効。`GenerateOption.unitTableSeparators()` が `FromJsonTableSeparatorsBuilder` でテーブル区切り設定を構築し、未指定時は `generateType.defaultSettingsPath()`（ddl/javaBeanのみ定義）のクラスパス既定値を使う。

## オプションの所在

| オプション | `GenerateDto` フィールド | `GenerateOption` での用途 |
|---|---|---|
| `template` / `result` / `resultPath` / `outputEncoding` | 共通 | 全タイプ共通（`resultPath()`はsql/ddl/fixedColumnDef/javaBeanで拡張子・命名を分岐） |
| `commit` / `op` / `sqlFilePrefix` / `sqlFileSuffix` | sql/ddl用 | `GenerateType.sql/ddl` の `toParametersBuilder()` 分岐 |
| `includeAllColumns` | settings用 | `dataSetParam()` で `ComparableDataSetParam.Builder` に反映 |
| `lazyLoad` | xlsx/xls用 | `parameterStream()` の分岐 |
| `fixedLength` / `defaultLength` / `align` | fixedColumnDef用 | `GenerateType.fixedColumnDef.write()` 内で `FixedColumnDef` 生成 |

## テストデータの場所

- 統合テストパラメータ: `core/src/test/resources/yo/dbunitcli/application/command/paramGenerate*.txt`
- 入力フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/src/generate/`
- unitSetting等の設定フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/settings/generate/`
- 期待値フィクスチャ: `core/src/test/resources/yo/dbunitcli/application/expect/generate/`
