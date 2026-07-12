---
description: scaffoldコマンド（core）修正時の設計ガイド。target追加・雛型生成ロジック変更時に参照。
TRIGGER when: command/Scaffold*.java の変更、またはscaffoldのtarget追加を伴う実装タスクの開始時。
DO NOT TRIGGER when: 読み取りのみ、または他コマンド（Compare/Convert/Generate/Run/Parameterize）のみの変更。
---

Scaffoldは、Generateの固定成果物型（`isFixedTemplate()=true`）を`generateType=txt/xlsx/xls`でカスタマイズ生成できるよう、setting/template/dataset/parameterの雛型を書き出すコマンド。`-target`対応は現状`ddl`/`javaBean`/`xlsxSchema`/`fixedColumnDef`/`parameter`のみ。target追加はGenerate側の設計（`generate-command`スキルの4軸）に従属する。

## target追加可否を左右する2条件

1. **`getFixedUnit()`がtable**: `-unitSetting`分割は`unit=table`でのみ効く。`dataset`固定（settings/xlsxTemplate）はまずGenerate側の変更を検討（`xlsxSchema`の`dataset`→`table`変更が前例）
2. **組み込みマクロの引数がスカラーのみ**: リスト値を要求するマクロは無改造流用不可

両方満たせば組み込み`.stg`/`.txt`を無改造流用できる。満たせなければ専用`{typeName}ScaffoldTemplate`が要る（判定基準・前例は`references/scaffold-targets.md`）。

## target1つが持つ4種の雛型

setting（`defaultSettingsPath()`がある型のみ意味を持つ）/ template（組み込みor専用stg,txtをコピー）/ dataset（列名からダミー行、形はテンプレートに合わせる）/ parameter（基本`generateType=txt -unit=table`で再現、形が変わる場合のみ専用ロジック）。詳細は`references/scaffold-targets.md`。

## 実装チェックリスト

変更ファイル一覧・target別対応表は `references/scaffold-targets.md` 参照。

## 関連スキル

Generate側の設計は`generate-command`参照。固定成果物のunit/write()/テンプレート形を変更する際は同名targetのScaffold側が壊れないか要確認。
