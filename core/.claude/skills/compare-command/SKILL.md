---
description: compareコマンド（core）修正時の設計ガイド。targetType追加・比較ロジック/差分レポート変更時に参照。
TRIGGER when: command/Compare*.java・command/ImageCompareOption.java・dataset/compare/ の変更、またはcompareの比較仕様変更を伴う実装タスクの開始時。
DO NOT TRIGGER when: 読み取りのみ、または他コマンド（Convert/Generate/Run/Parameterize/Scaffold）のみの変更。
---

Compareは、2データセット（`-new`/`-old`）を突き合わせ差分レポートを出力し、差分の有無で成否を返すコマンド。以下の5軸で設計する。

## 設計の5軸

1. **targetType**: `data`/`image`/`pdf`（`CompareOption.Type`）。この1択が入力ロード強制と比較エンジンの両方を決める。`getDataSetCompareBuilder()`が`DefaultCompareManager`かimage/pdf用Managerを選び、image/pdfは`toDto()`でsrcTypeを`file`＋extension（png/pdf）に強制し、テーブル名を`TARGET`にリネーム＋比較キー`NAME`を付与
2. **比較キー・整形（`-setting`）**: `FromJsonTableSeparatorsBuilder`が`TableSeparators`（比較キー・列フィルタ・ソート・分割/リネーム）を構築。`data`のみJSONをそのまま使い、image/pdfは`NAME`固定
3. **差分レポート出力（`-result`）**: `DataSetConverterOption`（Convertと共有）。`resultType`で出力形式、`-createPatchSql`で行差分時にパッチSQL生成
4. **期待差分の回帰（`-expect`）**: 指定時は「実差分レポート出力」と「期待差分データセット」を再Compareし一致で成否判定（レポート自体のリグレッション用途）。未指定は`srcType=none`で無効
5. **image/pdf詳細（`-image.*`）**: `ImageCompareOption`（threshold・除外領域`[x,y,w,h]`・矩形描画色等）。image/pdf時のみ有効

## 実装の骨格

入力（軸1・2）は`DataSetLoadOption`、出力（軸3）は`DataSetConverterOption`（共にConvert等と共通基盤で、比較軸の多くはこの共通側の変更になる）。比較エンジン本体は`dataset/compare/`。targetType追加＝`Type`定数＋`toDto()`のsrcType強制＋`getDataSetCompareBuilder()`分岐＋（必要なら）Manager新実装。変更箇所チェックリスト・オプション所在・比較エンジン構造・テスト場所は `references/compare-internals.md`。

## 関連スキル

出力側はConvertと共通のため`convert-command`。影響確認は`check-application-impact`、tauriヘルプは`tauri:update-help`。
