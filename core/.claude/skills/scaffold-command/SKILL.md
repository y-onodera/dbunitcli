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
| `command/ScaffoldOption.java` | `execute()` に新規target分岐を追加。ddl/javaBeanは組み込みテンプレート固定のため`-template`スキャフォールドを持たない。Java側precomputation依存タイプは`writeSchemaTemplate()`同様、専用`{typeName}ScaffoldTemplate.stg/.txt`が必要 |
| `src/main/resources/{typeName}/{typeName}ScaffoldTemplate.stg,.txt` | txt向け雛型（既存Generateテンプレートとは別物。precomputationなしでrows/unitSetting属性のみで完結） |
| `command/ScaffoldTest.java` | targetごとの`@Nested`テストクラス。setting/unitSetting/template/parameterの単体・組み合わせ・カスタムファイル名パターン |

target・出力ファイルの詳細対応は `references/scaffold-targets.md` 参照。

## 関連スキル

generateType定義は `generate-command` を参照。
