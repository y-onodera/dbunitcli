---
description: parameterizeコマンド（core）修正時の設計ガイド。データ駆動でのコマンド連鎖・テンプレート引数展開の変更時に参照。
TRIGGER when: command/Parameterize*.java・application/ParameterUnit.java の変更、またはparameterizeの駆動仕様変更を伴う実装タスクの開始時。
DO NOT TRIGGER when: 読み取りのみ、または他コマンド（Compare/Convert/Generate/Run/Scaffold）のみの変更。
---

Parameterizeは、パラメータデータセット（`-param`）の各単位を引数へ展開し、別コマンド（compare/convert/generate/run/parameterize）を繰り返し実行するデータ駆動オーケストレータ。`loadParams()`が`unit`でParameterストリームを作り、各Parameterごとに`createCommand()`（`-cmd`をST4展開して種別決定）＋`createArgs()`（引数構築）でサブコマンドを`exec()`する。以下の4軸で設計する。

## 設計の4軸

1. **unit（粒度）**: `ParameterUnit`（record/table/dataset。Generateと共有）。`-param`を何単位で1実行に落とすか。record=行毎/table=テーブル毎/dataset=全体1回
2. **引数テンプレート供給元**: `-cmdParam`（各Parameterでファイル名自体を展開し動的選択）＞ `-template`（固定既定）。どちらも無ければ`-arg`のみ
3. **展開の有無（`-parameterize`）**: `true`（既定）は供給テンプレートをST4展開してから引数化、`false`は生のまま。`-arg`/`-A`（Map）はキー一致で最終引数を上書きマージ
4. **失敗継続（`-ignoreFail`）**: サブコマンドの`CommandFailException`時、`true`で記録継続/`false`で即中断。最後に失敗総数>0で全体失敗

## 実装の骨格

- **駆動対象コマンドの追加**: `createCommand(String)`のswitch（compare/convert/generate/run/parameterize）に分岐追加。`command/Type.java`（CLIサブコマンド一覧）と同期が要る
- **unit挙動の変更**: `ParameterUnit`はGenerateと共有のため両コマンドへ波及
- **パラメータなし駆動**: `NONE_PARAM_MAPPER`が`srcType=none`検出時に`src=.`/`parameterize=false`を補い、データ無しで1回実行

`-cmd`もST4展開されるため`-param`の値でサブコマンドを動的切替できる。駆動フロー・引数マージ・変更箇所チェックリスト・テスト場所は `references/parameterize-internals.md`。

## 関連スキル

駆動対象の内部仕様は`compare-command`/`convert-command`/`generate-command`/`run-command`。unit意味論はGenerateと共通。影響確認は`check-application-impact`、tauriヘルプは`tauri:update-help`。
