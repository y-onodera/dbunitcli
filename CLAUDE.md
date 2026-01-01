# Claude Code プロジェクト設定

## 言語設定
このプロジェクトでは日本語でやりとりを行います。

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