---
description: generateコマンド（core）修正時の変更箇所ガイド。generateType追加・オプション追加時に参照。
TRIGGER when: command/Generate*.java の変更、またはgenerateType追加を伴う実装タスクの開始時。
DO NOT TRIGGER when: 読み取りのみ、または他コマンド（Compare/Convert/Run/Parameterize）のみの変更。
---

## generateType 追加/修正時の変更箇所

| ファイル（`application/`配下） | 内容 |
|---|---|
| `command/GenerateType.java` | enum定数追加。`write()`/`isFixedTemplate()`/`getFixedUnit()`/`defaultSettingsPath()`/`getTemplateString()` をoverride。固定成果物タイプに`-template`での差し替えは持たせない（組み込みテンプレート以外にしたい場合は利用者が`generateType=txt`＋`unitSetting`を使う） |
| `command/GenerateDto.java` | `@CommandLine.Option` フィールド追加（getter/setter） |
| `command/GenerateOption.java` | recordフィールド追加。コンストラクタ/`toParametersBuilder()`/`dataSetParam()`/`resultPath()` に反映 |
| `src/main/resources/{typeName}/*.stg,*.txt,*.json` | テンプレート/設定リソース（同名ディレクトリの`*ScaffoldTemplate.*`は別コマンド`Scaffold`用、対象外） |
| `command/GenerateOptionTest.java` / `GenerateTest.java` | 単体（toParameters往復）/統合（`paramGenerate*.txt`+`expect/generate/**`）テスト |
| `ParameterUnit.java` | record/table/dataset のストリーム生成（unit挙動を変える場合のみ） |

generateType一覧・unit対応表・オプション所在は `references/generate-types.md` 参照。

## 関連スキル

tauri側は `tauri:update-help`、sidecar/tauri影響確認は `check-application-impact`。
