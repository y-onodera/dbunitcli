# run 詳細（scriptType別・注意点・変更箇所・テスト）

## scriptType別対応表

| scriptType | Runner | 拡張子 | 固有オプション |
|---|---|---|---|
| `sql`（既定） | `SqlRunner` | `sql`（`.plsql`は区切り`/`＋keepformat） | `-jdbc.*`（`JdbcOption`）/ `-template.*`（`TemplateRenderOption`＝ST4） |
| `ant` | `AntRunner` | `xml` | `-antTarget`（`AntOption`）/ `-baseDir` |
| `cmd` | `CmdRunner` | `cmd` | `-baseDir` |
| `bat` | `CmdRunner` | `bat` | `-baseDir` |

`getExtension()`は既定でenum名、antのみ`xml`をオーバーライド。`createRunner()`は`cmd`/`bat`が共通の`CmdRunner`（enum既定実装）、`sql`/`ant`が個別オーバーライド。

## 注意点

- `-src`の内部固定4項目（srcType=file / loadData=true / includeMetaData=false / extension）は`toParametersBuilder()`で`remove()`され出力されない。ユーザーが触るのは実質`-src.src`（探索ルート）と`-baseDir`
- `-baseDir`は`sql`では出力されない（`scriptType != sql`のときのみ出力）。sqlの実行はJDBC接続に依存
- SQLは実行前にST4レンダリングされるため`$param.xxx$`でParameter（Parameterize等由来）を埋め込める
- Runnerは`runScript(File)`でファイル単位に実行し、失敗時は`AssertionError`。Parameterize経由なら`-ignoreFail`で継続可
- `-parallel`（全scriptType共通）: 未指定/1は逐次（convert/generateと違い既定は自動並列にしない＝スクリプトは実行順に依存しやすいため）。2以上は`Runner.run()`が`ParallelTableMapping.forEachUntilFailure`でファイルを並列実行（順不定。1件失敗したら未着手は始めず、実行中の完了を待って失敗）。stderrのログ転送は`StdErrToLog`が参照数で管理し、Runnerが同時に動いても（Parameterize並列）差し替え・復元が1回になる

## 実装チェックリスト（run修正時の変更箇所）

| ファイル（`application/`・`fileprocessor/`配下） | 内容 |
|---|---|
| `command/RunOption.java` | `ScriptType`定数＋`createRunner()`/`getExtension()`オーバーライド。固有オプションは`toParametersBuilder()`分岐＋recordフィールド |
| `command/RunDto.java` | `-scriptType`ほか`@CommandLine.Option`フィールド |
| `fileprocessor/{Type}Runner.java` | `Runner`実装（`runScript(File)`＝1ファイル分。並列/逐次の切替は`Runner.run()`）。Sql/Ant/Cmdが参考 |
| `option/JdbcOption.java`・`AntOption.java`・`TemplateRenderOption.java` | 実行方式固有オプションの追加時 |
| `command/RunOptionTest.java`・`RunTest.java` | 単体（toParameters往復）/統合（`paramRun*.txt`） |

## テストの場所

- 統合テストパラメータ: `core/src/test/resources/yo/dbunitcli/application/command/paramRun*.txt`（AntNoProperty/AntWithProperty/Bat/Cmd）
- スクリプトフィクスチャ: `src/sql/`・`src/ant/`・`src/cmd/`・`src/bat/`
- 期待値: `expect/ant/`・`expect/bat/`・`expect/cmd/`
