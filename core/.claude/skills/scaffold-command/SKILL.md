---
description: scaffoldコマンド（core）修正時の設計ガイド。target追加・雛型生成ロジック変更時に参照。
TRIGGER when: command/Scaffold*.java の変更、またはscaffoldのtarget追加を伴う実装タスクの開始時。
DO NOT TRIGGER when: 読み取りのみ、または他コマンド（Compare/Convert/Generate/Run/Parameterize）のみの変更。
---

Scaffoldは、Generateの固定成果物型（`isFixedTemplate()=true`）を`generateType=txt`でカスタマイズ生成できるよう、unitSetting/template/dataset/parameterの雛型を書き出すコマンド。`-target`対応は`ScaffoldTarget` enum（`command/ScaffoldTarget.java`）の定数＋`parameter`のみ。target追加はGenerate側の設計（`generate-command`スキルの4軸）に従属する。

## 統一された4target共通仕様

`ScaffoldOption.executeTarget()`が全targetで同一フローを実行し、target差分は`ScaffoldTarget`のフックに集約。各雛型は**対応オプションを明示指定した時のみ独立出力**（名前フォールバック・target間の非対称なし）:

- `-unitSetting=U` → サンプルunitSettingを`resources/setting/U.json`へコピー
- `-template=T` → 組み込みstg/txtを`resources/template/`へ無改造コピー
- `-dataset.src`+`-dataset.srcType` → `src/`に記述子dataset（1カラム=1行。`GenerateType.wrapProducer()`と同じproducerを直接インスタンス化し実メタデータから合成）
- `-parameter=P` → `option/P.param`。`-template`指定時はtxt駆動（記述子dataset入力）、未指定時はbuiltin駆動（元datasetの絶対パス入力）を書き出す

`.param`の詳細・target別対応表・記述子の中身は`references/scaffold-targets.md`。

## target追加の判断基準

1. **テンプレートが`rows`を「列定義」として読むか**: `sql`のように「実データレコード」として読む型は対象外
2. **`getFixedUnit()`がtable**: `dataset`固定（settings/xlsxTemplate）はまずGenerate側の変更を検討
3. **テンプレートがunit=tableの自然な形（`rows`/`tableName`/`dataset.<name>.rows`）を読むか**: 読まない場合はGenerate側テンプレート/write()のリファクタで寄せる（前例: xlsxSchema・fixedColumnDef。これにより全targetが組み込みテンプレートを無改造コピーできている）

## 関連スキル

Generate側の設計は`generate-command`参照。固定成果物型のunit/テンプレート形/`wrapProducer()`変更時は、同名targetのScaffold側（`ScaffoldTarget`と`ScaffoldTest`のE2Eバイト一致検証）が壊れないか要確認。
