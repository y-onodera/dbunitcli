---
description: compareコマンド（core）修正時の設計ガイド。targetType追加・比較ロジック/差分レポート変更時に参照。
TRIGGER when: command/Compare*.java・command/ImageCompareOption.java・dataset/compare/ の変更、またはcompareの比較仕様変更を伴う実装タスクの開始時。
DO NOT TRIGGER when: 読み取りのみ、または他コマンド（Convert/Generate/Run/Parameterize/Scaffold）のみの変更。
---

Compareは、2データセット（`-new`/`-old`）を突き合わせて差分レポートを出力し、差分の有無で成否を返す。以下の5軸で設計する。

## 設計の5軸

1. **targetType**: `data`/`image`/`pdf`（`CompareOption.Type`）。入力ロードの強制と比較エンジンを決める。`getDataSetCompareBuilder()`がManagerを選び、image/pdfは`toDto()`でsrcType=`file`＋拡張子を強制し、テーブル名`TARGET`・比較キー`NAME`を付与
2. **比較キー・整形（`-setting`）**: `FromJsonTableSeparatorsBuilder`が`TableSeparators`を構築（`data`のみJSONを使用）。outputFilter/importは references 参照
3. **差分レポート出力（`-result`）**: `DataSetConverterOption`（Convertと共有）。`-createPatchSql`で行差分時にパッチSQL生成
4. **期待差分の回帰（`-expect`）**: 実差分レポートと期待差分を再Compareし一致で成功
5. **image/pdf詳細（`-image.*`）**: `ImageCompareOption`。image/pdf時のみ有効

## 実装の骨格

入力は`DataSetLoadOption`、出力は`DataSetConverterOption`（Convert等と共通基盤）、比較エンジンは`dataset/compare/`。targetType追加＝`Type`定数＋`toDto()`のsrcType強制＋`getDataSetCompareBuilder()`分岐＋必要ならManager実装。変更箇所・オプション所在・テスト場所は `references/compare-internals.md`。

## 関連スキル

出力側は`convert-command`。影響確認は`check-application-impact`、tauriヘルプは`tauri:update-help`。
