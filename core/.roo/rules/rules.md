# Coreプロジェクト 共通開発ルール

このプロジェクトは、dbunitcliモノレポの一部として管理されています。
このドキュメントは、`core`プロジェクトにおける共通の開発ルールを定義します。

## 0. Roo定義

このプロジェクトでは、Rooに以下のロールと必要な知識を設定します：

必要な知識：
- Java言語とJVM最適化の深い理解
- DBUnitフレームワークの詳細な知識
- コードメトリクスとその分析手法
- GraalVM Native Imageの最適化技術

コマンド実行時の注意
- コマンド発行前の確認事項：
  - ターミナルで使用されているshellの種類とバージョン
  - 使用中のターミナルの状態（実行中のプロセスの有無）
  - 各ターミナルの作業ディレクトリ
  - コマンドの実行結果の確認方法
- コマンド実行時の注意点：
  - 必ずカレントディレクトリを確認
  - プロジェクトベースディレクトリ(dbunitcli/core)からの相対パスを意識
  - パスの指定は常に作業ディレクトリからの相対パスで行う