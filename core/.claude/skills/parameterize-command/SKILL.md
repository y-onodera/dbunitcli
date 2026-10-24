---
description: parameterizeコマンド（core）修正時の設計ガイド。データ駆動でのコマンド連鎖・テンプレート引数展開の変更時に参照。
TRIGGER when: command/Parameterize*.java・application/ParameterUnit.java の変更、またはparameterizeの駆動仕様変更を伴う実装タスクの開始時。
DO NOT TRIGGER when: 読み取りのみ、または他コマンド（Compare/Convert/Generate/Run/Scaffold）のみの変更。
---

Parameterizeは、`-param`の各単位を引数へ展開して別コマンドを繰り返し実行するオーケストレータ。`loadParams()`が`unit`でParameterストリームを作り、各Parameterで`createCommand()`（`-cmd`をST4展開）＋`createArgs()`でサブコマンドを`exec()`する。

## 設計の5軸

1. **unit**: `ParameterUnit`（record/table/dataset。Generateと共有）
2. **引数テンプレート供給元**: `-cmdParam`（ファイル名をParameterで動的決定）＞`-template`（固定）。どちらも無ければ`-arg`のみ
3. **展開（`-parameterize`）**: true（既定）はST4展開してから引数化。`-arg`/`-A`はキー一致で上書きマージ
4. **失敗継続（`-ignoreFail`）**: `CommandFailException`時に記録継続か即中断。失敗総数>0で全体失敗
5. **並列（`-parallel`）**: 既定1＝逐次。2以上は`ParallelTableMapping.forEachUntilFailure`で並列（順不定。失敗時は未着手を始めず実行中の完了を待つ）

## 実装の骨格

- 駆動対象の追加: `createCommand(String)`のswitchに分岐追加し、`command/Type.java`と同期
- unit変更はGenerateへ波及
- `srcType=none`は`NONE_PARAM_MAPPER`が`src=.`/`parameterize=false`を補い1回実行

駆動フロー・引数マージ・変更箇所・テスト場所は `references/parameterize-internals.md`。

## 関連スキル

駆動対象は各`*-command`スキル。影響確認は`check-application-impact`、tauriヘルプは`tauri:update-help`。
