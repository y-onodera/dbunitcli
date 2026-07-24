---
description: runコマンド（core）修正時の設計ガイド。scriptType追加・スクリプト実行ロジック変更時に参照。
TRIGGER when: command/Run*.java・fileprocessor/ の変更、またはrunのscriptType追加を伴う実装タスクの開始時。
DO NOT TRIGGER when: 読み取りのみ、または他コマンド（Compare/Convert/Generate/Parameterize/Scaffold）のみの変更。
---

Runは、`-src`で集めたスクリプトファイル群を`scriptType`ごとのRunnerで実行するコマンド（`Run.exec()` → `option.runner().run(option.targetFiles())`）。入力は常に「実行対象ファイル集合」で、`-src`はsrcType=file・loadData=true・includeMetaData=false・拡張子=`scriptType.getExtension()`に強制される（`targetFiles()`。この4つは`toParametersBuilder()`で出力から除外＝内部固定）。以下の3軸で設計する。

## 設計の3軸

1. **scriptType（実行方式）**: `RunOption.ScriptType` enum。この1択がRunner実装・対象拡張子・必要オプションを決める:
   - `sql`（既定）: `SqlRunner`。`-jdbc.*`でDB接続、`-template.*`（ST4）で各SQLを`Parameter`展開してから実行。`.plsql`は区切り`/`の特別扱い、SQL*Plus構文（SET/SPOOL/PROMPT/EXIT/COMMIT）は除去
   - `ant`: `AntRunner`。拡張子`xml`、`-antTarget`＋`-baseDir`基準
   - `cmd`/`bat`: `CmdRunner`。拡張子はenum名、`-baseDir`基準でOSコマンド実行
2. **固有オプションの出し分け**: `toParametersBuilder()`がscriptTypeで分岐（sql→template/jdbc、ant→antTarget＋baseDir、cmd/bat→baseDir。`-baseDir`はsql以外でのみ出力）
3. **Runner実装（`fileprocessor/`）**: `Runner`インタフェース（`run()`がstderrをログ転送でラップし`runScript(Stream<File>)`を呼ぶ）。実装はSql/Ant/Cmdの3つ

## 実装の骨格：scriptTypeを足す

`ScriptType`定数追加＋`createRunner(RunOption)`（必要なら`getExtension()`も）オーバーライド＋`fileprocessor/`にRunner実装。固有オプションを持つなら`toParametersBuilder()`の分岐も追加。変更箇所チェックリスト・注意点・テスト場所は `references/run-internals.md`。

## 関連スキル

Parameterizeが`-cmd=run`でRunを駆動するため`parameterize-command`も参照。影響確認は`check-application-impact`、tauriヘルプは`tauri:update-help`。
