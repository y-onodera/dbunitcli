---
description: runコマンド（core）修正時の設計ガイド。scriptType追加・スクリプト実行ロジック変更時に参照。
TRIGGER when: command/Run*.java・fileprocessor/ の変更、またはrunのscriptType追加を伴う実装タスクの開始時。
DO NOT TRIGGER when: 読み取りのみ、または他コマンド（Compare/Convert/Generate/Parameterize/Scaffold）のみの変更。
---

Runは、`-src`で集めたスクリプト群を`scriptType`ごとのRunnerで実行する（`option.runner().run(option.targetFiles())`）。`-src`はsrcType=file・loadData=true・includeMetaData=false・拡張子=`scriptType.getExtension()`に内部固定（`toParametersBuilder()`で出力から除外）。

## 設計の3軸

1. **scriptType**: `RunOption.ScriptType`。Runner・拡張子・必要オプションを決める（sql=`SqlRunner`で`Parameter`をST4展開して実行、ant=`AntRunner`、cmd/bat=`CmdRunner`）
2. **固有オプションの出し分け**: `toParametersBuilder()`がscriptTypeで分岐（`-baseDir`はsql以外のみ）
3. **Runner実装（`fileprocessor/`）**: `Runner.run()`がstderrをログ転送して`runScript(File)`を呼ぶ（`-parallel`が2以上ならファイルを並列実行、既定は逐次）

scriptType追加＝`ScriptType`定数＋`createRunner()`（必要なら`getExtension()`）＋Runner実装＋（固有オプションがあれば）`toParametersBuilder()`分岐。scriptType別対応表・注意点・テスト場所は `references/run-internals.md`。

## 関連スキル

Parameterizeが`-cmd=run`で駆動するため`parameterize-command`。影響確認は`check-application-impact`、tauriヘルプは`tauri:update-help`。
