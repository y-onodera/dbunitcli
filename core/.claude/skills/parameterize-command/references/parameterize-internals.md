# parameterize 詳細（駆動フロー・引数マージ・変更箇所）

## 1件あたりの実行フロー（`Parameterize.exec()`）

`loadParams()`のParameterストリームを`map`で回し、各Parameterについて:

1. `createCommand(param)`: `-cmd`をST4で`render(cmd, param)`→文字列でサブコマンド決定（`createCommand(String)`のswitch）
2. `createArgs(param)`: 引数リスト（下記）を組み立て
3. `command.exec(args, param)`: サブコマンドを実行。`CommandFailException`時は`-ignoreFail`で継続/中断を分岐（継続時は失敗カウント+1）

最後に失敗カウント>0なら全体を`CommandFailException`で失敗させる。

## 引数リストの組み立て（`createArgs()`）

1. **供給テンプレート決定**（`getTemplateArgs()`）: `-cmdParam`があればそのファイル名を`render`して`FileResources.searchTemplate()`で取得、無ければ`-template`、どちらも無ければ空文字
2. **展開**: `-parameterize=true`なら`templateRender.render(templateArgs, param)`でST4展開、`false`なら生のまま
3. **行分割**: 結果を`\r?\n`でsplitして引数配列に
4. **`-arg`/`-A`マージ**: `-arg`が空でなければ、引数配列を`key=`→`key=value`のMapにし、`-arg`のエントリ（`key`→`key=value`）で上書きマージ。キー抽出は`ARGS_IGNORE_FILTER`（`-P`/`-A`/`-arg`を無視）

`-P`/`-A`/`-arg`は`ARGS_IGNORE_FILTER`によりParameterize自身のDTOへ流れず、サブコマンドへ渡す値として扱われる。

## unit別の落とし込み（`ParameterUnit`、Generateと共有）

| unit | 1回の実行に渡るParameter | 用途の目安 |
|---|---|---|
| `record`（既定） | `-param`の1行（`mapIncludeMetaData`時は`row`も付与、行番号付き） | 行ごとに1コマンド（データ駆動の基本） |
| `table` | テーブル毎に1件（テーブルの先頭行相当のマップ） | テーブル単位のバッチ |
| `dataset` | データセット全体で1件 | 全体で1回だけ実行（固定連鎖） |

`-param.srcType=none`（または未指定）は`NONE_PARAM_MAPPER`が`srcType=none`/`src=.`/`parameterize=false`を補完し、パラメータデータ無しでも1回実行する経路にする。

## 駆動可能なサブコマンド（`createCommand(String)`）

`compare` / `convert` / `generate` / `run` / `parameterize`（自己再帰可）。ここに無い文字列は`IllegalArgumentException("no executable command : …")`。

**コマンドを増やす/名前を変える場合の同期先**: `ParameterizeOption.createCommand(String)`のswitch と `command/Type.java`（`CommandType`。CLIサブコマンド一覧）。両者は同じコマンド集合を表すため揃える。Scaffoldの`parameter` target（`scaffold-command`スキル）も`CommandParameters`経由で同じコマンド集合の`.param`を生成するので、コマンド追加時は要確認。

## オプションの所在

| オプション | `ParameterizeDto`フィールド | 用途 |
|---|---|---|
| `-cmd` | `cmd` | 軸: 実行するサブコマンド種別（ST4展開される） |
| `-cmdParam` | `cmdParam` | 各Parameterで動的にファイル名決定する引数テンプレート |
| `-template` | `template` | 既定の引数テンプレート（`-cmdParam`があれば無視） |
| `-parameterize` | `parameterize` | 供給テンプレートをST4展開するか（既定true） |
| `-unit` | `unit` | `ParameterUnit`（record/table/dataset） |
| `-arg` / `-A` | `arg`（Map） | 最終引数のキー上書きマージ |
| `-ignoreFail` | `ignoreFail` | サブコマンド失敗時の継続可否 |
| `-param.*` | `paramData` | `DataSetLoadOption`（パラメータデータの読み込み。共通ロード基盤） |
| `-template.*` | `templateOption` | `TemplateRenderOption`（ST4レンダリング設定。encoding等） |

## 実装チェックリスト（parameterize修正時の変更箇所）

| ファイル | 内容 |
|---|---|
| `command/ParameterizeOption.java` | 駆動フロー本体。サブコマンド追加は`createCommand(String)`、パラメータなし駆動は`NONE_PARAM_MAPPER`、引数マージは`createArgs()` |
| `command/ParameterizeDto.java` | `@CommandLine.Option`フィールド追加 |
| `command/Type.java` | サブコマンド集合を変える場合の同期（CLIサブコマンド一覧） |
| `application/ParameterUnit.java` | unit挙動の変更（Generateと共有。両コマンドへ波及） |
| `command/ParameterizeTest.java` | 統合テスト |

## テストの場所

- テストクラス: `core/src/test/java/yo/dbunitcli/application/command/ParameterizeTest.java`
- パラメータ・引数テンプレート等のフィクスチャは`core/src/test/resources/yo/dbunitcli/application/`配下（`src/param/`等）
