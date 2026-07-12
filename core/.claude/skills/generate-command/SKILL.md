---
description: generateコマンド（core）修正時の設計ガイド。generateType追加・オプション追加時に参照。
TRIGGER when: command/Generate*.java の変更、またはgenerateType追加を伴う実装タスクの開始時。
DO NOT TRIGGER when: 読み取りのみ、または他コマンド（Compare/Convert/Run/Parameterize）のみの変更。
---

Generateは、データセットをテンプレート（またはJavaロジック）で成果物に変換するコマンド。新しいgenerateTypeは以下の4軸で設計してから実装する。

## 設計の4軸

1. **データロード**: 実データ行が要るか、スキーマ（列定義・型）だけで足りるか。`GenerateOption.dataSetParam()`の`loadData`/`useJdbcMetaData`で制御
2. **テンプレート差し替え可否**: ユーザーが`-template`で形式を選ぶ汎用型か、Java側で形を決め打ちする固定成果物型（`isFixedTemplate()=true`）か
3. **unit（データ粒度）**: record（レコード毎）/ table（テーブル毎、`-unitSetting`分割やScaffold連携と相性がよい）/ dataset（全体で1回）。固定成果物型は`getFixedUnit()`で強制
4. **出力ファイル形状**: 単一ファイル / テーブル毎に複数ファイル（`resultPath()`で分岐）/ コード生成専用（テンプレート不要）

補助軸: Excel出力限定の`-lazyLoad`（都度ストリーム書き込み vs 全ロード後書き込み）。

## 詳細

既存generateTypeの軸別対応表・オプション所在・変更箇所チェックリストは `references/generate-types.md` 参照。

## 関連スキル

固定成果物型（unit固定）を変更する際は、同名targetを持つ`Scaffold`側が壊れないか`scaffold-command`スキルで確認。tauri側は `tauri:update-help`、sidecar/tauri影響確認は `check-application-impact`。
