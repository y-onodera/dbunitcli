---
description: scaffoldコマンド（core）修正時の変更箇所ガイド。target追加・雛型生成ロジック変更時に参照。
TRIGGER when: command/Scaffold*.java の変更、またはscaffoldのtarget追加を伴う実装タスクの開始時。
DO NOT TRIGGER when: 読み取りのみ、または他コマンド（Compare/Convert/Generate/Run/Parameterize）のみの変更。
---

Scaffoldは、template指定なしの固定成果物generateType（settings/sql/ddl/xlsxSchema/javaBean/fixedColumnDef/xlsxTemplate）を`generateType=txt/xlsx/xls`でカスタマイズ生成できるよう、templateとdatasetの雛型を書き出すコマンド。`-target`対応は現状`ddl`/`javaBean`/`xlsxSchema`/`fixedColumnDef`/`parameter`のみ（`settings`/`sql`/`xlsxTemplate`は未対応）。

## target追加/修正時の変更箇所

| ファイル（`application/`配下） | 内容 |
|---|---|
| `command/ScaffoldDto.java` | `-target` の説明文（対応target一覧）を更新 |
| `command/ScaffoldOption.java` | `execute()` に新規target分岐を追加。ddl/javaBeanは`-template`指定時、組み込み`.stg/.txt`をそのままコピーし`.param`は`generateType=txt`＋`unitSetting`で組み込みと同内容を再現する（`writeGenericParamFile()`） |
| `src/main/resources/{typeName}/{typeName}ScaffoldTemplate.stg,.txt` | 組み込み`.stg/.txt`を無改造流用できない場合のみ必要な専用雛型（判定基準は`references/scaffold-targets.md`） |
| `command/ScaffoldTest.java` | targetごとの`@Nested`テストクラス。setting/unitSetting/template/parameterの単体・組み合わせ・カスタムファイル名パターン |

target・出力ファイルの詳細対応、専用ScaffoldTemplate要否の判定基準は `references/scaffold-targets.md` 参照。

## 関連スキル

固定成果物のunit/write()/テンプレート形は `generate-command` 参照。変更時は同名targetのScaffold側が壊れないか要確認。
