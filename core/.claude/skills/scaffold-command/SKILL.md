---
description: scaffoldコマンド（core）修正時の設計ガイド。target追加・雛型生成ロジック変更時に参照。
TRIGGER when: command/Scaffold*.java の変更、またはscaffoldのtarget追加を伴う実装タスクの開始時。
DO NOT TRIGGER when: 読み取りのみ、または他コマンド（Compare/Convert/Generate/Run/Parameterize）のみの変更。
---

Scaffoldは、Generateの固定成果物型（`isFixedTemplate()=true`）を`generateType=txt`でカスタマイズ生成できるよう、unitSetting/template/dataset/parameterの雛型を書き出すコマンド。`-target`は`ScaffoldTarget` enumの定数＋`parameter`。target追加はGenerate側の設計（`generate-command`の4軸）に従属する。

## 共通仕様

`ScaffoldOption.executeTarget()`が全targetで同一フローを実行し、target差分は`ScaffoldTarget`のフックに集約。各雛型は対応オプション（`-unitSetting`/`-template`/`-dataset.*`/`-parameter`）を明示指定した時のみ出力する。出力先・`.param`・target別対応表は`references/scaffold-targets.md`。

## target追加の判断基準

1. テンプレートが`rows`を「列定義」として読むか（`sql`のように実データとして読む型は対象外）
2. `getFixedUnit()`がtableか（`dataset`固定はまずGenerate側を変更）
3. テンプレートがunit=tableの自然な形（`rows`/`tableName`/`subTable.<name>.rows`）を読むか（読まなければGenerate側をリファクタ。前例: xlsxSchema・fixedColumnDef）

## 関連スキル

Generate側は`generate-command`。固定成果物型のunit/テンプレート形/`wrapProducer()`変更時は、`ScaffoldTarget`と`ScaffoldTest`のE2Eバイト一致検証が壊れないか確認。
