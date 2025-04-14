# Sidecarプロジェクト 技術スタック

## ビルドツール
- maven
  - 依存関係の管理
  - ビルドプロセスの自動化
  - テスト実行の制御

## アプリケーションフレームワーク
- micronaut 4.3.11
  - netty (HTTPサーバー)
    - 非同期I/Oベースの高性能HTTPサーバー
    - WebSocketサポート
  - jackson (シリアライゼーション)
    - JSONデータの効率的な処理
    - カスタムシリアライザ/デシリアライザのサポート

## テストフレームワーク
- JUnit5 (micronaut-test 4.2.1経由)
  - ユニットテスト
  - 統合テスト
  - パラメータ化テスト
  - モック/スタブの活用

## ランタイム
- Java 21
  - 最新のJava言語機能を活用
  - 改善されたガベージコレクション
  - 拡張されたモジュールシステム

- graalvm (native-maven-plugin 0.10.0経由)
  - ネイティブイメージのビルド
  - 高速な起動時間
  - 低メモリフットプリント

## 主要な依存関係
- dbunit-cli 1.1-SNAPSHOT (coreプロジェクト)
  - データセット操作の基本機能
  - CLIコマンドの実装
  - ファイルフォーマット変換

## システム要件

### 開発環境
- Java 21以上のJDK
- Maven 3.8以上
- GraalVM Native Build Tools

### 実行環境
- Java 21以上のJRE
- 最小メモリ要件: 256MB
- 推奨メモリ要件: 512MB以上

### サポートOS
- Windows 10/11
- macOS 11以降
- Linux (主要ディストリビューション)

## 関連ドキュメント
- [プロジェクト概要](./01-overview.md)
- [アーキテクチャ詳細](../architecture/01-overview.md)