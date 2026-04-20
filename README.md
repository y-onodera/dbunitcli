# dbunitcli

DBUnit CLI はデータセットの変換・比較・生成・SQL 実行・パラメータ化バッチ処理を行う統合ツール群です。

## ドキュメント

- [ヘルプページ（オンラインプレビュー）](https://htmlpreview.github.io/?https://github.com/y-onodera/dbunitcli/blob/master/tauri/public/help/index.html)

ヘルプには以下のトピックを含みます:

### コマンド
- Convert — データ形式を変換する
- Compare — データセットを比較して差分を検出する
- Generate — テンプレートからファイルを生成する
- Run — SQL やスクリプトを実行する
- Parameterize — パラメータでバッチ処理する

### リファレンス
- Dataset Load Form — データセット入力フォームの構成
- Dataset Settings — Dataset Settings ファイルの書式
- Xlsx Schema — Xlsx 入力のマッピング定義

## モジュール構成

- `core/` — コア CLI 機能（GraalVM native-image ビルド）
- `sidecar/` — Micronaut ベースの REST API サーバー
- `gui/` — JavaFX GUI
- `tauri/` — Tauri v2 デスクトップアプリ（React フロントエンド）
