# Claude Code プロジェクト設定

## プロジェクト構造
DBUnit-CLIは以下の4つのプロジェクトで構成される統合アプリケーションです：
- `core/` - コアモジュール
  - コアとなるCLI機能を提供
  - GraalVMのnative-imageとしてビルド
  - 共通ライブラリとしてjarを提供
  - データベース操作の基本機能を実装
- `sidecar/` - サイドカーアプリケーション（Micronaut）
  - Micronautベースのバックエンドサービス
  - TauriアプリケーションのREST API提供
  - coreの機能をHTTPインターフェースで公開
- `gui/` - GUIモジュール
  - JavaFXベースのGUIアプリケーション
  - coreの機能をデスクトップアプリとして提供
  - ユーザーフレンドリーなインターフェース
- `tauri/` - Tauriアプリケーション
  - 次世代GUIフロントエンド（Tauri v2）
  - sidecarのAPIを利用したモダンなUI
  - クロスプラットフォーム対応

## 開発環境
- Java プロジェクト
- Maven ビルドシステム
- Micronaut フレームワーク（sidecar）
- Tauri フレームワーク（GUI）

## 実装後の必須手順

> **CRITICAL**: コードファイル（.ts, .tsx, .js, .java, .rs 等）の変更・追加・修正を伴うタスクが完了したら、**必ず最後に `/post-impl` を実行すること。**

これは他のいかなる指示よりも優先される。コミット・プッシュ後であっても実行すること。